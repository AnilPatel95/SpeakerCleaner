package com.shuttletechnologies.speakercleaner.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shuttletechnologies.speakercleaner.theme.LocalAppColors

@Composable
fun AcousticGauge(
    progress: Float, // 0.0 to 1.0
    frequency: Float,
    isCleaning: Boolean,
    phaseText: String,
    modifier: Modifier = Modifier,
    size: Dp = 230.dp
) {
    val colors = LocalAppColors.current

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        label = "progress_anim"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "ripple_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = if (isCleaning) 1.25f else 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isCleaning) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = this.size.width
            val h = this.size.height
            val center = Offset(w / 2f, h / 2f)
            val radius = (w / 2f) - 16.dp.toPx()

            // Outer ripple waves if active
            if (isCleaning) {
                drawCircle(
                    color = colors.accent.copy(alpha = 0.12f),
                    radius = radius * pulseScale,
                    center = center
                )
                drawCircle(
                    color = colors.accentSecondary.copy(alpha = 0.08f),
                    radius = radius * (pulseScale * 1.15f).coerceAtMost(1.35f),
                    center = center
                )
            }

            // Track background arc (360 degrees)
            drawCircle(
                color = colors.surfaceElevated,
                radius = radius,
                center = center
            )
            drawCircle(
                color = colors.border.copy(alpha = 0.5f),
                radius = radius,
                center = center,
                style = Stroke(width = 8.dp.toPx())
            )

            // Progress Arc
            val sweep = animatedProgress * 360f
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        colors.accentSecondary,
                        colors.accent,
                        Color(0xFF80D8FF),
                        colors.accent
                    ),
                    center = center
                ),
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Central Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colors.accent.copy(alpha = 0.12f))
                    .padding(8.dp)
            ) {
                AppLogo(size = 38.dp)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${frequency.toInt()} Hz",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.accent
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surfaceElevated)
                    .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = phaseText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textMuted
                )
            }
        }
    }
}
