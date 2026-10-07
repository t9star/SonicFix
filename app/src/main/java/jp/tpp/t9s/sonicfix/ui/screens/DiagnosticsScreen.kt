package jp.tpp.t9s.sonicfix.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SpatialAudio
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import jp.tpp.t9s.sonicfix.R
import jp.tpp.t9s.sonicfix.audio.AudioChannel
import jp.tpp.t9s.sonicfix.audio.AudioEngine
import jp.tpp.t9s.sonicfix.audio.AudioMicAnalyzer
import jp.tpp.t9s.sonicfix.audio.WaveformType
import jp.tpp.t9s.sonicfix.ui.theme.AccentRed
import jp.tpp.t9s.sonicfix.ui.theme.CyanPrimary
import jp.tpp.t9s.sonicfix.ui.theme.CyanSecondary
import jp.tpp.t9s.sonicfix.ui.theme.SurfaceCardBorder
import jp.tpp.t9s.sonicfix.ui.theme.SurfaceCardDark

@Composable
fun DiagnosticsScreen(
    audioEngine: AudioEngine,
    micAnalyzer: AudioMicAnalyzer,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPlaying by audioEngine.isPlaying.collectAsState()
    val decibels by micAnalyzer.decibels.collectAsState()
    val isRecording by micAnalyzer.isRecording.collectAsState()

    var activeChannel by remember { mutableStateOf(AudioChannel.STEREO) }

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
        if (granted) {
            micAnalyzer.startAnalyzing(true)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            micAnalyzer.stopAnalyzing()
            audioEngine.stop()
        }
    }

    val animatedDb by animateFloatAsState(targetValue = decibels, label = "db")
    val scrollState = rememberScrollState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.diag_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = stringResource(R.string.diag_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        // 1. ステレオセパレーションテスト
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceCardBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SpatialAudio, contentDescription = null, tint = CyanPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.stereo_test_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        stringResource(R.string.ch_left) to AudioChannel.LEFT_ONLY,
                        stringResource(R.string.ch_both) to AudioChannel.STEREO,
                        stringResource(R.string.ch_right) to AudioChannel.RIGHT_ONLY
                    ).forEach { (label, channel) ->
                        val isSelected = activeChannel == channel
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CyanPrimary else SurfaceCardDark)
                                .border(1.dp, if (isSelected) CyanPrimary else SurfaceCardBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    activeChannel = channel
                                    audioEngine.updateChannel(channel)
                                    if (!isPlaying) {
                                        audioEngine.startManualTone(440f, WaveformType.SINE)
                                    }
                                }
                        ) {
                            Text(
                                text = label,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. 音域チェック (Bass / Mid / High)
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceCardBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.frequency_test_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CyanSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        "Bass (80Hz)" to 80f,
                        "Mid (1kHz)" to 1000f,
                        "Treble (10kHz)" to 10000f
                    ).forEach { (label, hz) ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceCardDark)
                                .border(1.dp, SurfaceCardBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    audioEngine.stop()
                                    audioEngine.startManualTone(hz, WaveformType.SINE)
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
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. マイク音圧（dB）リアルタイムメーター
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCardDark),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceCardBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = CyanPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.mic_meter_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "${animatedDb.toInt()} dB",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { (animatedDb / 95f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = if (animatedDb > 80f) AccentRed else CyanPrimary,
                    trackColor = SurfaceCardBorder,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (!hasMicPermission) {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            if (isRecording) {
                                micAnalyzer.stopAnalyzing()
                            } else {
                                micAnalyzer.startAnalyzing(true)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) AccentRed else SurfaceCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (!hasMicPermission) {
                            stringResource(R.string.btn_enable_mic)
                        } else if (isRecording) {
                            stringResource(R.string.btn_stop_mic)
                        } else {
                            stringResource(R.string.btn_start_mic)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // サウンド停止ボタン
        if (isPlaying) {
            Button(
                onClick = { audioEngine.stop() },
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Stop, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.btn_stop_sound))
            }
        }
    }
}
