package com.kenkeremath.uselessui.waves

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun WavyBox(
    modifier: Modifier = Modifier,
    crestHeight: Dp = 4.dp,
    waveLength: Dp = 80.dp,
    color: Color = Color.Blue,
    strokeWidth: Dp = 2.dp,
    filled: Boolean = false,
    fillColor: Color = color.copy(alpha = 0.2f),
    animationDurationMillis: Int = 1000,
    content: @Composable () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition()
    val wave by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(animationDurationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val wl = waveLength.toPx()
            val amplitude = crestHeight.toPx()
            val stretch = 2.pi() / wl
            val xShift = wave * 2.pi()
            val stroke = strokeWidth.toPx()

            fun sinY(x: Float, isTop: Boolean): Float {
                val direction = if (isTop) -1 else 1
                return amplitude * direction * sin(stretch * x - xShift) +
                       (if (isTop) amplitude else size.height - amplitude)
            }

            fun sinX(y: Float, isRight: Boolean): Float {
                val direction = if (isRight) -1 else 1
                return amplitude * direction * sin(stretch * y - xShift) + 
                       (if (isRight) size.width - amplitude else amplitude)
            }

            val segmentLength = wl / 10f
            val numHorizontalSegments = (size.width / segmentLength).roundToInt()
            val numVerticalSegments = (size.height / segmentLength).roundToInt()

            val path = Path().apply {
                // Start at top-left
                var x = 0f
                var y = sinY(0f, true)
                moveTo(x, y)

                // Draw top edge (left to right)
                for (segment in 1..numHorizontalSegments) {
                    x = min(x + segmentLength, size.width)
                    y = sinY(x, true)
                    lineTo(x, y)
                }

                // Draw right edge (top to bottom)
                x = sinX(y, true)
                for (segment in 1..numVerticalSegments) {
                    y = min(y + segmentLength, size.height)
                    x = sinX(y, true)
                    lineTo(x, y)
                }

                // Draw bottom edge (right to left)
                y = sinY(x, false)
                for (segment in 1..numHorizontalSegments) {
                    x = max(x - segmentLength, 0f)
                    y = sinY(x, false)
                    lineTo(x, y)
                }

                // Draw left edge (bottom to top)
                for (segment in 1..numVerticalSegments) {
                    y = max(y - segmentLength, 0f)
                    x = sinX(y, false)
                    lineTo(x, y)
                }

                close()
            }

            if (filled) {
                drawPath(
                    path = path,
                    color = fillColor
                )
            }
            
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = stroke)
            )
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(crestHeight + strokeWidth),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Preview
@Composable
fun WavyBoxOutlinedPreview() {
    Surface(
        modifier = Modifier
            .size(200.dp),
        color = Color.White,
    ) {
        WavyBox(
            modifier = Modifier
                .size(180.dp)
                .padding(16.dp),
            color = Color.Blue,
            crestHeight = 6.dp
        ) {
            Text("Wavy Box")
        }
    }
}

@Preview
@Composable
fun WavyBoxFilledPreview() {
    Surface(
        modifier = Modifier
            .size(200.dp),
        color = Color.White,
    ) {
        WavyBox(
            modifier = Modifier
                .size(180.dp)
                .padding(16.dp),
            color = Color.Blue,
            filled = true,
            crestHeight = 6.dp
        ) {
            Text("Filled Wavy Box")
        }
    }
}

private const val PI = Math.PI.toFloat()
private fun Int.pi() = this * PI