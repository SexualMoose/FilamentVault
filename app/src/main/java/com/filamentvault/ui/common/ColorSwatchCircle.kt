package com.filamentvault.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ColorSwatchCircle(
    colorHex: String,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    @Suppress("UNUSED_PARAMETER") borderWidth: Dp = 1.5.dp
) {
    val filamentColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        Color.Gray
    }

    val filamentDark = darken(filamentColor, 0.6f)
    val filamentHighlight = lighten(filamentColor, 1.35f)

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // 3D tilt offset — back face sits higher than front
        val tilt = h * 0.13f

        val cx = w / 2f
        val frontY = h * 0.57f
        val backY = frontY - tilt

        // Ellipse radii for each concentric ring
        val outerRx = w * 0.46f
        val outerRy = h * 0.27f
        val midRx = w * 0.33f
        val midRy = h * 0.20f
        val innerRx = w * 0.12f
        val innerRy = h * 0.07f

        // Spool plastic colors
        val flangeColor = Color(0xFF4A4A4A)
        val flangeSide = Color(0xFF3A3A3A)
        val coreColor = Color(0xFF1A1A1A)
        val coreSide = Color(0xFF111111)

        // ── Barrel depth (the 3D side visible between back and front) ──

        // Outer flange side wall
        val outerSide = Path().apply {
            arcTo(
                rect = Rect(cx - outerRx, backY - outerRy, cx + outerRx, backY + outerRy),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 180f,
                forceMoveTo = true
            )
            lineTo(cx + outerRx, frontY)
            arcTo(
                rect = Rect(cx - outerRx, frontY - outerRy, cx + outerRx, frontY + outerRy),
                startAngleDegrees = 0f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
            close()
        }
        drawPath(outerSide, flangeSide)

        // Filament barrel side — horizontal gradient for cylindrical shading
        val filamentSide = Path().apply {
            arcTo(
                rect = Rect(cx - midRx, backY - midRy, cx + midRx, backY + midRy),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 180f,
                forceMoveTo = true
            )
            lineTo(cx + midRx, frontY)
            arcTo(
                rect = Rect(cx - midRx, frontY - midRy, cx + midRx, frontY + midRy),
                startAngleDegrees = 0f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
            close()
        }
        drawPath(
            filamentSide,
            Brush.horizontalGradient(
                colors = listOf(
                    filamentDark,
                    filamentColor,
                    filamentHighlight,
                    filamentColor,
                    filamentDark
                ),
                startX = cx - midRx,
                endX = cx + midRx
            )
        )

        // Core hub side wall
        val coreSidePath = Path().apply {
            arcTo(
                rect = Rect(cx - innerRx, backY - innerRy, cx + innerRx, backY + innerRy),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 180f,
                forceMoveTo = true
            )
            lineTo(cx + innerRx, frontY)
            arcTo(
                rect = Rect(cx - innerRx, frontY - innerRy, cx + innerRx, frontY + innerRy),
                startAngleDegrees = 0f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
            close()
        }
        drawPath(coreSidePath, coreSide)

        // ── Front face ──

        // Front flange disk
        drawOval(
            color = flangeColor,
            topLeft = Offset(cx - outerRx, frontY - outerRy),
            size = Size(outerRx * 2, outerRy * 2)
        )

        // Front filament ring — radial gradient for wound-filament shading
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(filamentHighlight, filamentColor, filamentDark),
                center = Offset(cx - midRx * 0.15f, frontY - midRy * 0.2f),
                radius = midRx * 1.3f
            ),
            topLeft = Offset(cx - midRx, frontY - midRy),
            size = Size(midRx * 2, midRy * 2)
        )

        // Subtle concentric winding lines on filament face
        for (i in 1..4) {
            val ratio = i / 5f
            val rx = innerRx + (midRx - innerRx) * ratio
            val ry = innerRy + (midRy - innerRy) * ratio
            drawOval(
                color = Color.White.copy(alpha = 0.07f),
                topLeft = Offset(cx - rx, frontY - ry),
                size = Size(rx * 2, ry * 2),
                style = Stroke(width = 0.8f)
            )
        }

        // Front core hole
        drawOval(
            color = coreColor,
            topLeft = Offset(cx - innerRx, frontY - innerRy),
            size = Size(innerRx * 2, innerRy * 2)
        )

        // ── Highlights & outline ──

        // Highlight arc on top of front flange
        drawArc(
            color = Color.White.copy(alpha = 0.12f),
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(cx - outerRx + 3, frontY - outerRy + 2),
            size = Size(outerRx * 2 - 6, outerRy * 2 - 4),
            style = Stroke(width = 1.5f, cap = StrokeCap.Round)
        )

        // Thin outline for visibility on dark backgrounds
        drawOval(
            color = Color.White.copy(alpha = 0.15f),
            topLeft = Offset(cx - outerRx, frontY - outerRy),
            size = Size(outerRx * 2, outerRy * 2),
            style = Stroke(width = 0.8f)
        )

        // Outline on the barrel top edge
        drawArc(
            color = Color.White.copy(alpha = 0.1f),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(cx - outerRx, backY - outerRy),
            size = Size(outerRx * 2, outerRy * 2),
            style = Stroke(width = 0.6f)
        )
    }
}

private fun darken(color: Color, factor: Float): Color = Color(
    red = (color.red * factor).coerceIn(0f, 1f),
    green = (color.green * factor).coerceIn(0f, 1f),
    blue = (color.blue * factor).coerceIn(0f, 1f),
    alpha = color.alpha
)

private fun lighten(color: Color, factor: Float): Color = Color(
    red = (color.red * factor).coerceIn(0f, 1f),
    green = (color.green * factor).coerceIn(0f, 1f),
    blue = (color.blue * factor).coerceIn(0f, 1f),
    alpha = color.alpha
)
