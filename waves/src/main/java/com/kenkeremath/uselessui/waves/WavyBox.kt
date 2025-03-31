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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun WavyBox(
    spec: WavyBoxSpec,
    style: WavyBoxStyle,
    modifier: Modifier = Modifier,
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
            val path = createWavyBoxPath(
                size = size,
                spec = spec,
                wave = wave,
                crestHeight = spec.crestHeight,
                waveLength = spec.waveLength
            )

            if (style is WavyBoxStyle.FilledWithBrush) {
                drawPath(
                    path = path,
                    brush = style.brush
                )
            } else if (style is WavyBoxStyle.FilledWithColor) {
                drawPath(
                    path = path,
                    color = style.color
                )
            }

            // Draw stroke
            drawPath(
                path = path,
                color = style.strokeColor,
                style = Stroke(width = style.strokeWidth.toPx())
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(spec.crestHeight + style.strokeWidth),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

/**
 * Creates a path for a wavy box using the addWavyPathSegment utility
 */
private fun createWavyBoxPath(
    size: androidx.compose.ui.geometry.Size,
    spec: WavyBoxSpec,
    wave: Float,
    crestHeight: Dp,
    waveLength: Dp
): Path {
    val path = Path()

    // Pre-calculate corner points
    val topLeftCorner = Offset(0f, 0f)
    val topRightCorner = Offset(size.width, 0f)
    val bottomRightCorner = Offset(size.width, size.height)
    val bottomLeftCorner = Offset(0f, size.height)

    // Start at top-left corner
    path.moveTo(topLeftCorner.x, topLeftCorner.y)

    // Top edge (left to right)
    if (spec.topWavy) {
        wavyPathSegment(
            existingPath = path,
            animationProgress = wave,
            crestHeight = crestHeight,
            waveLength = waveLength,
            startPoint = topLeftCorner,
            endPoint = topRightCorner,
        )
    } else {
        path.lineTo(topRightCorner.x, topRightCorner.y)
    }

    // Right edge (top to bottom)
    if (spec.rightWavy) {
        wavyPathSegment(
            existingPath = path,
            animationProgress = wave,
            crestHeight = crestHeight,
            waveLength = waveLength,
            startPoint = topRightCorner,
            endPoint = bottomRightCorner,
        )
    } else {
        path.lineTo(bottomRightCorner.x, bottomRightCorner.y)
    }

    // Bottom edge (right to left)
    if (spec.bottomWavy) {
        wavyPathSegment(
            existingPath = path,
            animationProgress = wave,
            crestHeight = crestHeight,
            waveLength = waveLength,
            startPoint = bottomRightCorner,
            endPoint = bottomLeftCorner,
        )
    } else {
        path.lineTo(bottomLeftCorner.x, bottomLeftCorner.y)
    }

    // Left edge (bottom to top)
    if (spec.leftWavy) {
        wavyPathSegment(
            existingPath = path,
            animationProgress = wave,
            crestHeight = crestHeight,
            waveLength = waveLength,
            startPoint = bottomLeftCorner,
            endPoint = topLeftCorner,
        )
    } else {
        path.lineTo(topLeftCorner.x, topLeftCorner.y)
    }

    path.close()
    return path
}

/**
 * A simplified version of WavyBox that acts as a loading indicator with a gradient fill
 */
@Composable
fun WavyLoadingIndicator(
    modifier: Modifier = Modifier,
    crestHeight: Dp = 4.dp,
    waveLength: Dp = 80.dp,
    gradientColors: List<Color> = listOf(Color.Blue, Color.Cyan),
    animationDurationMillis: Int = 1000
) {
    val gradientBrush = Brush.linearGradient(
        colors = gradientColors,
        start = Offset(0f, Float.POSITIVE_INFINITY),
        end = Offset(0f, 0f)
    )

    WavyBox(
        spec = WavyBoxSpec(
            topWavy = true,
            rightWavy = false,
            bottomWavy = false,
            leftWavy = false,
            crestHeight = crestHeight,
            waveLength = waveLength,
        ),
        style = WavyBoxStyle.FilledWithBrush(
            brush = gradientBrush,
            strokeWidth = 0.dp,
            strokeColor = Color.Transparent
        ),
        modifier = modifier,
        animationDurationMillis = animationDurationMillis
    )
}

@Immutable
sealed class WavyBoxStyle {
    abstract val strokeWidth: Dp
    abstract val strokeColor: Color

    @Immutable
    data class FilledWithBrush(
        val brush: Brush,
        override val strokeWidth: Dp,
        override val strokeColor: Color
    ) : WavyBoxStyle()

    @Immutable
    data class FilledWithColor(
        val color: Color,
        override val strokeWidth: Dp,
        override val strokeColor: Color
    ) : WavyBoxStyle()

    @Immutable
    data class Outlined(
        override val strokeWidth: Dp,
        override val strokeColor: Color
    ) : WavyBoxStyle()
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
                leftWavy = false,
                crestHeight = 6.dp
            ),
            style = WavyBoxStyle.Outlined(
                strokeColor = Color.Black,
                strokeWidth = 2.dp
            ),
            modifier = Modifier
                .size(180.dp)
                .padding(16.dp),
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
                leftWavy = false,
                crestHeight = 6.dp,
            ),
            style = WavyBoxStyle.FilledWithColor(
                color = Color.Cyan,
                strokeWidth = 0.dp,
                strokeColor = Color.Transparent
            ),
            modifier = Modifier
                .size(180.dp)
                .padding(16.dp),
        ) {
            Text("Filled Wavy Box")
        }
    }
}

@Preview
@Composable
fun WavyBoxCornerFilledPreview() {
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
                leftWavy = false,
                crestHeight = 6.dp,
            ),
            style = WavyBoxStyle.FilledWithColor(
                color = Color.Cyan,
                strokeWidth = 0.dp,
                strokeColor = Color.Transparent
            ),
            modifier = Modifier
                .size(180.dp)
                .padding(16.dp),
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
                leftWavy = true,
                crestHeight = 6.dp,
            ),
            style = WavyBoxStyle.FilledWithColor(
                color = Color.Cyan,
                strokeWidth = 0.dp,
                strokeColor = Color.Transparent
            ),
            modifier = Modifier
                .size(180.dp)
                .padding(16.dp),
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
                leftWavy = false,
                crestHeight = 6.dp,
            ),
            style = WavyBoxStyle.FilledWithColor(
                color = Color.Cyan,
                strokeWidth = 0.dp,
                strokeColor = Color.Transparent
            ),
            modifier = Modifier
                .size(180.dp)
                .padding(16.dp),
        ) {
            Text("Wavy Box")
        }
    }
}

@Preview
@Composable
fun WavyLoadingIndicatorPreview() {
    Surface(
        modifier = Modifier
            .size(200.dp)
            .padding(16.dp),
        color = Color.White,
    ) {
        WavyLoadingIndicator(
            modifier = Modifier.fillMaxSize(),
            crestHeight = 8.dp
        )
    }
}

@Immutable
data class WavyBoxSpec(
    val topWavy: Boolean,
    val rightWavy: Boolean,
    val bottomWavy: Boolean,
    val leftWavy: Boolean,
    val crestHeight: Dp = 4.dp,
    val waveLength: Dp = 80.dp,
)