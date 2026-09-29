package com.shuttletechnologies.speakercleaner.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val primaryCyan = Color(0xFF00E5FF)
        val secondaryBlue = Color(0xFF00B0FF)
        val deepBlue = Color(0xFF0077C2)

        // Draw speaker body
        val speakerPath = Path().apply {
            moveTo(w * 0.22f, h * 0.38f)
            lineTo(w * 0.38f, h * 0.38f)
            lineTo(w * 0.52f, h * 0.24f)
            lineTo(w * 0.52f, h * 0.76f)
            lineTo(w * 0.38f, h * 0.62f)
            lineTo(w * 0.22f, h * 0.62f)
            close()
        }

        drawPath(
            path = speakerPath,
            brush = Brush.linearGradient(
                colors = listOf(primaryCyan, secondaryBlue),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
        )

        // Inner speaker cone detail
        val innerCone = Path().apply {
            moveTo(w * 0.28f, h * 0.44f)
            lineTo(w * 0.38f, h * 0.44f)
            lineTo(w * 0.48f, h * 0.32f)
            lineTo(w * 0.48f, h * 0.68f)
            lineTo(w * 0.38f, h * 0.56f)
            lineTo(w * 0.28f, h * 0.56f)
            close()
        }
        drawPath(
            path = innerCone,
            color = deepBlue.copy(alpha = 0.4f)
        )

        // Wave 1
        drawArc(
            color = primaryCyan,
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(w * 0.42f, h * 0.30f),
            size = androidx.compose.ui.geometry.Size(w * 0.30f, h * 0.40f),
            style = Stroke(width = w * 0.08f, cap = StrokeCap.Round)
        )

        // Wave 2
        drawArc(
            color = secondaryBlue,
            startAngle = -50f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = Offset(w * 0.50f, h * 0.20f),
            size = androidx.compose.ui.geometry.Size(w * 0.42f, h * 0.60f),
            style = Stroke(width = w * 0.08f, cap = StrokeCap.Round)
        )

        // Water droplet ejection
        val dropPath = Path().apply {
            moveTo(w * 0.82f, h * 0.28f)
            cubicTo(
                w * 0.88f, h * 0.35f,
                w * 0.90f, h * 0.42f,
                w * 0.82f, h * 0.46f
            )
            cubicTo(
                w * 0.76f, h * 0.46f,
                w * 0.74f, h * 0.40f,
                w * 0.82f, h * 0.28f
            )
            close()
        }
        drawPath(
            path = dropPath,
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF80D8FF), primaryCyan),
                center = Offset(w * 0.82f, h * 0.38f),
                radius = w * 0.10f
            )
        )
    }
}
