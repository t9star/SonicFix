package jp.tpp.t9s.sonicfix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.tpp.t9s.sonicfix.R
import jp.tpp.t9s.sonicfix.audio.AudioEngine
import jp.tpp.t9s.sonicfix.audio.VibrationManager
import jp.tpp.t9s.sonicfix.audio.WaveformType
import jp.tpp.t9s.sonicfix.ui.components.WaveVisualizer
import jp.tpp.t9s.sonicfix.ui.theme.AccentRed
import jp.tpp.t9s.sonicfix.ui.theme.CyanPrimary
import jp.tpp.t9s.sonicfix.ui.theme.SurfaceCardBorder
import jp.tpp.t9s.sonicfix.ui.theme.SurfaceCardDark

@Composable
fun ManualGenScreen(
    audioEngine: AudioEngine,
    vibrationManager: VibrationManager,
    modifier: Modifier = Modifier
) {
    val isPlaying by audioEngine.isPlaying.collectAsState()
    var frequency by remember { mutableFloatStateOf(440f) }
    var selectedWaveform by remember { mutableStateOf(WaveformType.SINE) }
    var vibrationEnabled by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.manual_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = stringResource(R.string.manual_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // 周波数大表示
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceCardBorder, RoundedCornerShape(20.dp))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "${frequency.toInt()} Hz",
                    style = MaterialTheme.typography.displayLarge,
                    color = CyanPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 微調整ボタン (+/- 1Hz, 10Hz)
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            vibrationManager.tick()
                            frequency = (frequency - 10f).coerceAtLeast(20f)
                            if (isPlaying) audioEngine.updateFrequency(frequency)
                        }
                    ) {
                        Text("-10", color = CyanPrimary, fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = {
                            vibrationManager.tick()
                            frequency = (frequency - 1f).coerceAtLeast(20f)
                            if (isPlaying) audioEngine.updateFrequency(frequency)
                        }
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "-1", tint = CyanPrimary)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    IconButton(
                        onClick = {
                            vibrationManager.tick()
                            frequency = (frequency + 1f).coerceAtMost(20000f)
                            if (isPlaying) audioEngine.updateFrequency(frequency)
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "+1", tint = CyanPrimary)
                    }

                    IconButton(
                        onClick = {
                            vibrationManager.tick()
                            frequency = (frequency + 10f).coerceAtMost(20000f)
                            if (isPlaying) audioEngine.updateFrequency(frequency)
                        }
                    ) {
                        Text("+10", color = CyanPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // スライダー
                Slider(
                    value = frequency,
                    onValueChange = {
                        frequency = it
                        if (isPlaying) audioEngine.updateFrequency(it)
                    },
                    valueRange = 20f..20000f,
                    colors = SliderDefaults.colors(
                        thumbColor = CyanPrimary,
                        activeTrackColor = CyanPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 波形選択
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            WaveformType.values().forEach { wave ->
                val isSelected = selectedWaveform == wave
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) CyanPrimary else SurfaceCardDark)
                        .border(
                            1.dp,
                            if (isSelected) CyanPrimary else SurfaceCardBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            vibrationManager.tick()
                            selectedWaveform = wave
                            if (isPlaying) audioEngine.updateWaveform(wave)
                        }
                ) {
                    Text(
                        text = wave.name,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // プリセット
        Text(
            text = stringResource(R.string.presets_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(
                "Eject (165Hz)" to 165f,
                "Bass (80Hz)" to 80f,
                "A4 (440Hz)" to 440f,
                "Ultra (15kHz)" to 15000f
            ).forEach { (label, hz) ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceCardDark)
                        .border(1.dp, SurfaceCardBorder, RoundedCornerShape(8.dp))
                        .clickable {
                            vibrationManager.tick()
                            frequency = hz
                            if (isPlaying) audioEngine.updateFrequency(hz)
                        }
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = CyanPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 波形ビジュアライザー
        WaveVisualizer(
            isPlaying = isPlaying,
            frequency = frequency
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 再生/停止 ボタン
        Button(
            onClick = {
                vibrationManager.tick()
                if (isPlaying) {
                    audioEngine.stop()
                    vibrationManager.stop()
                } else {
                    audioEngine.startManualTone(frequency, selectedWaveform)
                    if (vibrationEnabled) vibrationManager.startCleanVibration()
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPlaying) AccentRed else CyanPrimary
            ),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(56.dp)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (isPlaying) stringResource(R.string.btn_stop) else stringResource(R.string.btn_play_tone),
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
