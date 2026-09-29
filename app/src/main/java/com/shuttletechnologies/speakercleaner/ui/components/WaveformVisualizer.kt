package com.shuttletechnologies.speakercleaner.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.shuttletechnologies.speakercleaner.data.WaveformType
import com.shuttletechnologies.speakercleaner.theme.LocalAppColors
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.floor
import kotlin.math.sin

@Composable
fun WaveformVisualizer(
    frequency: Float,
    waveform: WaveformType,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
    val phaseOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isPlaying) (2f * Math.PI.toFloat()) else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surfaceElevated)
            .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val centerY = h / 2f
            val amplitude = if (isPlaying) (h * 0.36f) else (h * 0.08f)

            // Draw center baseline
            drawLine(
                color = colors.border.copy(alpha = 0.4f),
                start = Offset(0f, centerY),
                end = Offset(w, centerY),
                strokeWidth = 1.dp.toPx()
            )

            // Number of cycles displayed
            val cycles = (frequency / 120f).coerceIn(2f, 9f)
            val path = Path()

            val step = 3f
            var first = true

            var x = 0f
            while (x <= w) {
                val progress = x / w
                val angle = (progress * cycles * 2f * PI.toFloat()) + phaseOffset

                val rawValue = when (waveform) {
                    WaveformType.SINE -> sin(angle)
                    WaveformType.SQUARE -> if (sin(angle) >= 0f) 0.85f else -0.85f
                    WaveformType.TRIANGLE -> (2f / PI.toFloat()) * asin(sin(angle).coerceIn(-1f, 1f))
                    WaveformType.SAWTOOTH -> {
                        val norm = (angle / (2f * PI.toFloat())) - floor(0.5f + angle / (2f * PI.toFloat()))
                        2f * norm
                    }
                }

                val y = centerY - (rawValue * amplitude)

                if (first) {
                    path.moveTo(x, y)
                    first = false
                } else {
                    path.lineTo(x, y)
                }

                x += step
            }

            // Draw glowing wave stroke
            drawPath(
                path = path,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        colors.accent.copy(alpha = 0.3f),
                        colors.accent,
                        colors.accentSecondary,
                        colors.accent.copy(alpha = 0.3f)
                    )
                ),
                style = Stroke(
                    width = if (isPlaying) 3.dp.toPx() else 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }
    }
}
