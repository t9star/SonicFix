package jp.tpp.t9s.sonicfix.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import jp.tpp.t9s.sonicfix.ui.theme.CyanPrimary
import jp.tpp.t9s.sonicfix.ui.theme.WaveGradientEnd
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun WaveVisualizer(
    isPlaying: Boolean,
    frequency: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f

        if (!isPlaying) {
            // アイドル時のフラットライン
            drawLine(
                color = CyanPrimary.copy(alpha = 0.3f),
                start = Offset(0f, centerY),
                end = Offset(width, centerY),
                strokeWidth = 2.dp.toPx()
            )
            return@Canvas
        }

        // 3本の重なり合う波線（奥行きとリアルタイム感）
        val waveCount = 3
        val colors = listOf(
            CyanPrimary,
            Color(0xFF29B6F6),
            WaveGradientEnd
        )

        for (w in 0 until waveCount) {
            val path = Path()
            val wavePhase = phase + (w * PI.toFloat() / 3f)
            val amplitude = (height * 0.38f) * (1f - w * 0.15f)
            val freqFactor = (frequency / 250f).coerceIn(1.5f, 5.5f)

            var first = true
            val step = 4f
            var x = 0f

            while (x <= width) {
                val normalizedX = x / width
                // 画面端に向かって振幅を収束させる（エンベロープ）
                val envelope = sin(normalizedX * PI.toFloat())
                val y = centerY + (sin(normalizedX * 2 * PI.toFloat() * freqFactor + wavePhase) * amplitude * envelope)

                if (first) {
                    path.moveTo(x, y.toFloat())
                    first = false
                } else {
                    path.lineTo(x, y.toFloat())
                }
                x += step
            }

            drawPath(
                path = path,
                brush = Brush.horizontalGradient(
                    colors = listOf(colors[w].copy(alpha = 0.4f), colors[w], colors[w].copy(alpha = 0.4f))
                ),
                style = Stroke(width = (3f - w * 0.5f).dp.toPx())
            )
        }
    }
}
