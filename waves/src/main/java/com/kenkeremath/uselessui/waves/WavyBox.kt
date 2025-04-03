package com.kenkeremath.uselessui.waves

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A composable that draws a wavy box with manual control over the animation progress.
 * This state-hoisted version allows for more control over the animation.
 *
 * @param progress Animation progress value between 0f and 1f
 * @param spec Configuration for which sides of the box should be wavy
 * @param style Style configuration for the box (filled, outlined, etc.)
 * @param modifier Modifier to be applied to the component
 * @param content Optional composable content to be displayed inside the box
 */
@Composable
fun WavyBox(
    progress: Float,
    spec: WavyBoxSpec,
    style: WavyBoxStyle,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    val density = LocalDensity.current
    
    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val path = createWavyBoxPath(
                size = size,
                spec = spec,
                progress = progress,
                crestHeight = spec.crestHeight,
                waveLength = spec.waveLength,
                density = density
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
 * A composable that draws an animated wavy box.
 *
 * @param spec Configuration for which sides of the box should be wavy
 * @param style Style configuration for the box (filled, outlined, etc.)
 * @param modifier Modifier to be applied to the component
 * @param animationDurationMillis Duration of one complete wave animation cycle in milliseconds
 * @param content Optional composable content to be displayed inside the box
 */
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

    WavyBox(
        progress = wave,
        spec = spec,
        style = style,
        modifier = modifier,
        content = content
    )
}

/**
 * Creates a path for a wavy box using the addWavyPathSegment utility
 */
private fun createWavyBoxPath(
    size: androidx.compose.ui.geometry.Size,
    spec: WavyBoxSpec,
    progress: Float,
    crestHeight: Dp,
    waveLength: Dp,
    density: Density
): Path {
    val path = Path()
    // Convert crest height to pixels for offset calculations
    val crestHeightPx = with(density) { crestHeight.toPx() }

    // If centerAlongBounds is false, offset the path by crestHeight
    // so the top of the crest touches the bounds
    val topYOffset = if (spec.topWavy && !spec.centerAlongBounds) crestHeightPx else 0f
    val bottomYOffset = if (spec.bottomWavy && !spec.centerAlongBounds) crestHeightPx else 0f
    val leftXOffset = if (spec.leftWavy && !spec.centerAlongBounds) crestHeightPx else 0f
    val rightXOffset = if (spec.rightWavy && !spec.centerAlongBounds) crestHeightPx else 0f

    // Pre-calculate corner points
    val topLeftCorner = Offset(leftXOffset, topYOffset)
    val topRightCorner = Offset(size.width - rightXOffset, topYOffset)
    val bottomRightCorner = Offset(size.width - rightXOffset, size.height - bottomYOffset)
    val bottomLeftCorner = Offset(leftXOffset, size.height - bottomYOffset)

    // Start at top-left corner
    path.moveTo(topLeftCorner.x, topLeftCorner.y)

    // Top edge (left to right)
    if (spec.topWavy) {
        wavyPathSegment(
            existingPath = path,
            animationProgress = progress,
            crestHeight = crestHeight,
            waveLength = waveLength,
            startPoint = topLeftCorner,
            endPoint = topRightCorner,
            density = density
        )
    } else {
        path.lineTo(topRightCorner.x, topRightCorner.y)
    }

    // Right edge (top to bottom)
    if (spec.rightWavy) {
        wavyPathSegment(
            existingPath = path,
            animationProgress = progress,
            crestHeight = crestHeight,
            waveLength = waveLength,
            startPoint = topRightCorner,
            endPoint = bottomRightCorner,
            density = density
        )
    } else {
        path.lineTo(bottomRightCorner.x, bottomRightCorner.y)
    }

    // Bottom edge (right to left)
    if (spec.bottomWavy) {
        wavyPathSegment(
            existingPath = path,
            animationProgress = progress,
            crestHeight = crestHeight,
            waveLength = waveLength,
            startPoint = bottomRightCorner,
            endPoint = bottomLeftCorner,
            density = density
        )
    } else {
        path.lineTo(bottomLeftCorner.x, bottomLeftCorner.y)
    }

    // Left edge (bottom to top)
    if (spec.leftWavy) {
        wavyPathSegment(
            existingPath = path,
            animationProgress = progress,
            crestHeight = crestHeight,
            waveLength = waveLength,
            startPoint = bottomLeftCorner,
            endPoint = topLeftCorner,
            density = density
        )
    } else {
        path.lineTo(topLeftCorner.x, topLeftCorner.y)
    }

    path.close()
    return path
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
fun WavyBoxAllWavesNotCenteredPreview() {
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
        Box(
            modifier = Modifier
                .size(200.dp)
                .padding(16.dp)
                .border(2.dp, Color.Black)
        )
    }
}

@Preview
@Composable
fun WavyBoxAllWavesCenteredPreview() {
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
                centerAlongBounds = true
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
        Box(
            modifier = Modifier
                .size(200.dp)
                .padding(16.dp)
                .border(2.dp, Color.Black)
        )
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

/**
 * Configuration specification for a WavyBox component.
 * 
 * This class defines which sides of the box should have a wavy appearance and
 * controls the visual properties of the waves.
 *
 * @property topWavy When true, the top edge of the box will have a wavy appearance
 * @property rightWavy When true, the right edge of the box will have a wavy appearance
 * @property bottomWavy When true, the bottom edge of the box will have a wavy appearance
 * @property leftWavy When true, the left edge of the box will have a wavy appearance
 * @property crestHeight The height of each wave crest in dp. Controls the amplitude of the waves.
 * @property waveLength The length of each complete wave cycle in dp. Controls the frequency of the waves.
 * @property centerAlongBounds When true, the waves are centered along the bounds (half of the way will be outside the bounds).
 *                            When false, the top of the waves will be aligned with the bounds of the view.
 */
@Immutable
data class WavyBoxSpec(
    val topWavy: Boolean,
    val rightWavy: Boolean,
    val bottomWavy: Boolean,
    val leftWavy: Boolean,
    val crestHeight: Dp = 4.dp,
    val waveLength: Dp = 80.dp,
    val centerAlongBounds: Boolean = false
)