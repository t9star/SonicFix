package jp.tpp.t9s.sonicfix.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

enum class WaveformType {
    SINE,
    SQUARE,
    TRIANGLE
}

enum class AudioChannel {
    STEREO,
    LEFT_ONLY,
    RIGHT_ONLY
}

class AudioEngine {

    companion object {
        const val SAMPLE_RATE = 44100
        const val BUFFER_SIZE_FRAMES = 2048
    }

    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentFrequency = MutableStateFlow(165f)
    val currentFrequency: StateFlow<Float> = _currentFrequency.asStateFlow()

    private val _cleanProgress = MutableStateFlow(0f)
    val cleanProgress: StateFlow<Float> = _cleanProgress.asStateFlow()

    @Volatile
    private var targetFrequency: Float = 440f

    @Volatile
    private var currentWaveform: WaveformType = WaveformType.SINE

    @Volatile
    private var currentChannel: AudioChannel = AudioChannel.STEREO

    private var phase: Double = 0.0

    private fun initAudioTrack() {
        if (audioTrack != null) return

        val minBufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = maxOf(minBufferSize, BUFFER_SIZE_FRAMES * 4)

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()
    }

    /**
     * 自動水抜き・ホコリ排出モード（30秒〜45秒の共振スイープ＋パルス）
     */
    fun startAutoClean(durationSeconds: Int = 35, onCompleted: () -> Unit = {}) {
        stop()
        initAudioTrack()
        _isPlaying.value = true

        playbackJob = scope.launch {
            val totalSamples = durationSeconds * SAMPLE_RATE
            var sampleCounter = 0
            val buffer = ShortArray(BUFFER_SIZE_FRAMES * 2) // Stereo

            // スピーカー水抜き・ホコリ除去に最も効果的な周波数レンジ (150Hz ~ 1250Hz)
            val minHz = 165f
            val maxHz = 1200f

            while (isActive && sampleCounter < totalSamples) {
                val progress = sampleCounter.toFloat() / totalSamples.toFloat()
                _cleanProgress.value = progress

                // 非線形スイープ＋周期パルスでスピーカーコーンに激しい物理振動を与える
                val cycleTime = (sampleCounter.toDouble() / SAMPLE_RATE) % 3.0 // 3秒周期
                val cycleProgress = cycleTime / 3.0
                val freq = if (cycleProgress < 0.7) {
                    minHz + (maxHz - minHz) * (cycleProgress / 0.7).toFloat()
                } else {
                    // 低音パルス（共振バースト）
                    if (((cycleProgress - 0.7) * 20).toInt() % 2 == 0) 165f else 300f
                }

                _currentFrequency.value = freq

                for (i in 0 until BUFFER_SIZE_FRAMES) {
                    val angle = 2.0 * PI * freq / SAMPLE_RATE
                    phase += angle
                    if (phase > 2.0 * PI) phase -= 2.0 * PI

                    // パルスモジュレーション（振幅をリズミカルに変調させて水滴を弾き飛ばす）
                    val pulse = (sin(2.0 * PI * 4.0 * (sampleCounter + i) / SAMPLE_RATE) * 0.2 + 0.8)
                    val rawSample = sin(phase) * pulse
                    val sampleVal = (rawSample * Short.MAX_VALUE * 0.95).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()

                    buffer[i * 2] = sampleVal     // L
                    buffer[i * 2 + 1] = sampleVal // R
                }

                audioTrack?.write(buffer, 0, buffer.size)
                sampleCounter += BUFFER_SIZE_FRAMES
            }

            _cleanProgress.value = 1f
            stop()
            launch(Dispatchers.Main) {
                onCompleted()
            }
        }
    }

    /**
     * マニュアル周波数生成（20Hz〜20,000Hz）
     */
    fun startManualTone(frequency: Float, waveform: WaveformType = WaveformType.SINE) {
        targetFrequency = frequency
        currentWaveform = waveform
        currentChannel = AudioChannel.STEREO

        if (_isPlaying.value) {
            _currentFrequency.value = frequency
            return
        }

        initAudioTrack()
        _isPlaying.value = true
        _currentFrequency.value = frequency

        playbackJob = scope.launch {
            val buffer = ShortArray(BUFFER_SIZE_FRAMES * 2)

            while (isActive) {
                val freq = targetFrequency
                _currentFrequency.value = freq

                for (i in 0 until BUFFER_SIZE_FRAMES) {
                    val angle = 2.0 * PI * freq / SAMPLE_RATE
                    phase += angle
                    if (phase > 2.0 * PI) phase -= 2.0 * PI

                    val sampleFloat = when (currentWaveform) {
                        WaveformType.SINE -> sin(phase)
                        WaveformType.SQUARE -> if (phase < PI) 0.8 else -0.8
                        WaveformType.TRIANGLE -> (2.0 / PI) * (phase - PI)
                    }

                    val sampleVal = (sampleFloat * Short.MAX_VALUE * 0.9).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()

                    when (currentChannel) {
                        AudioChannel.STEREO -> {
                            buffer[i * 2] = sampleVal
                            buffer[i * 2 + 1] = sampleVal
                        }
                        AudioChannel.LEFT_ONLY -> {
                            buffer[i * 2] = sampleVal
                            buffer[i * 2 + 1] = 0
                        }
                        AudioChannel.RIGHT_ONLY -> {
                            buffer[i * 2] = 0
                            buffer[i * 2 + 1] = sampleVal
                        }
                    }
                }

                audioTrack?.write(buffer, 0, buffer.size)
            }
        }
    }

    fun updateFrequency(newFreq: Float) {
        targetFrequency = newFreq
        _currentFrequency.value = newFreq
    }

    fun updateWaveform(waveform: WaveformType) {
        currentWaveform = waveform
    }

    fun updateChannel(channel: AudioChannel) {
        currentChannel = channel
    }

    fun stop() {
        playbackJob?.cancel()
        playbackJob = null
        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
        _isPlaying.value = false
        phase = 0.0
    }
}
