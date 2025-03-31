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
import androidx.compose.runtime.Immutable
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
    spec: WavyBoxSpec,
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

            // Pre-calculate corner points to ensure proper alignment
            val topLeftY = if (spec.topWavy) sinY(0f, true) else 0f
            val topLeftX = if (spec.leftWavy) sinX(0f, false) else 0f
            val topRightY = if (spec.topWavy) sinY(size.width, true) else 0f
            val topRightX = if (spec.rightWavy) sinX(0f, true) else size.width
            val bottomRightY = if (spec.bottomWavy) sinY(size.width, false) else size.height
            val bottomRightX = if (spec.rightWavy) sinX(size.height, true) else size.width
            val bottomLeftY = if (spec.bottomWavy) sinY(0f, false) else size.height
            val bottomLeftX = if (spec.leftWavy) sinX(size.height, false) else 0f

            val path = Path().apply {
                // Start at top-left
                moveTo(topLeftX, topLeftY)

                // Draw top edge (left to right)
                var x = topLeftX
                for (segment in 1..numHorizontalSegments) {
                    x = min(x + segmentLength, topRightX)
                    if (x < topRightX) {
                        val y = if (spec.topWavy) sinY(x, true) else 0f
                        lineTo(x, y)
                    }
                }
                
                // Ensure we connect exactly to the top-right corner
                lineTo(topRightX, topRightY)

                // Draw right edge (top to bottom)
                var y = topRightY
                val rightEdgePoints = mutableListOf<Pair<Float, Float>>()
                
                for (segment in 1..numVerticalSegments) {
                    y = min(y + segmentLength, bottomRightY)
                    if (y < bottomRightY) {
                        val rawX = sinX(y, true)
                        rightEdgePoints.add(Pair(rawX, y))
                    }
                }
                
                rightEdgePoints.forEach { (rawX, pointY) ->
                    if (spec.rightWavy) {
                        lineTo(rawX, pointY)
                    } else {
                        lineTo(size.width, pointY)
                    }
                }
                
                // Ensure we connect exactly to the bottom-right corner
                lineTo(bottomRightX, bottomRightY)

                // Draw bottom edge (right to left)
                x = bottomRightX
                for (segment in 1..numHorizontalSegments) {
                    x = max(x - segmentLength, bottomLeftX)
                    if (x > bottomLeftX) {
                        val y = if (spec.bottomWavy) sinY(x, false) else size.height
                        lineTo(x, y)
                    }
                }
                
                lineTo(bottomLeftX, bottomLeftY)

                // Draw left edge (bottom to top)
                y = bottomLeftY
                val leftEdgePoints = mutableListOf<Pair<Float, Float>>()
                
                for (segment in 1..numVerticalSegments) {
                    y = max(y - segmentLength, topLeftY)
                    if (y > topLeftY) {
                        val rawX = sinX(y, false)
                        leftEdgePoints.add(Pair(rawX, y))
                    }
                }
                
                leftEdgePoints.forEach { (rawX, pointY) ->
                    if (spec.leftWavy) {
                        lineTo(rawX, pointY)
                    } else {
                        lineTo(0f, pointY)
                    }
                }
                
                // Close the path by returning to the starting point
                lineTo(topLeftX, topLeftY)
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
            spec = WavyBoxSpec(
                topWavy = true,
                rightWavy = false,
                bottomWavy = true,
                leftWavy = false
            ),
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
            spec = WavyBoxSpec(
                topWavy = true,
                rightWavy = false,
                bottomWavy = true,
                leftWavy = false
            ),
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

@Preview
@Composable
fun WavyBoxAllWavesPreview() {
    Surface(
        modifier = Modifier
            .size(200.dp),
        color = Color.White,
    ) {
        WavyBox(
            spec = WavyBoxSpec(
                topWavy = true,
                rightWavy = true,
                bottomWavy = true,
                leftWavy = true
            ),
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
fun WavyBoxNoWavesPreview() {
    Surface(
        modifier = Modifier
            .size(200.dp),
        color = Color.White,
    ) {
        WavyBox(
            spec = WavyBoxSpec(
                topWavy = false,
                rightWavy = false,
                bottomWavy = false,
                leftWavy = false
            ),
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

@Immutable
data class WavyBoxSpec(
    val topWavy: Boolean,
    val rightWavy: Boolean,
    val bottomWavy: Boolean,
    val leftWavy: Boolean,
)

private const val PI = Math.PI.toFloat()
private fun Int.pi() = this * PI