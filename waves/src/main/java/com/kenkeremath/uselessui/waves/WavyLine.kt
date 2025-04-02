package com.kenkeremath.uselessui.waves

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A composable that draws a wavy line with manual control over the animation progress.
 * This state-hoisted version allows for more control over the animation.
 *
 * @param progress Animation progress value between 0f and 1f
 * @param modifier Modifier to be applied to the component
 * @param crestHeight The height of each wave crest in dp
 * @param waveLength The length of each complete wave in dp
 * @param color The color of the wavy line
 * @param strokeWidth The width of the line stroke in dp
 * @param centerWave When true, the wave is centered vertically in the available space.
 *                   When false, the wave is positioned at the top with crestHeight as offset
 */
@Composable
fun WavyLine(
    progress: Float,
    modifier: Modifier = Modifier,
    crestHeight: Dp = 4.dp,
    waveLength: Dp = 80.dp,
    color: Color = Color.Blue,
    strokeWidth: Dp = 2.dp,
    centerWave: Boolean = false,
) {
    Canvas(
        modifier = modifier
    ) {
        val startPoint = Offset(0f, if (centerWave) size.height / 2 else crestHeight.toPx())
        val endPoint = Offset(size.width, if (centerWave) size.height / 2 else crestHeight.toPx())
        
        val path = wavyPathSegment(
            existingPath = null,
            animationProgress = progress,
            crestHeight = crestHeight,
            waveLength = waveLength,
            startPoint = startPoint,
            endPoint = endPoint,
        )

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth.toPx())
        )
    }
}

/**
 * A composable that draws an animated wavy line.
 *
 * @param modifier Modifier to be applied to the component
 * @param crestHeight The height of each wave crest in dp
 * @param waveLength The length of each complete wave in dp
 * @param color The color of the wavy line
 * @param strokeWidth The width of the line stroke in dp
 * @param centerWave When true, the wave is centered vertically in the available space.
 *                   When false, the wave is positioned at the top with crestHeight as offset
 * @param animationDurationMillis Duration of one complete wave animation cycle in milliseconds
 */
@Composable
fun WavyLine(
    modifier: Modifier = Modifier,
    crestHeight: Dp = 4.dp,
    waveLength: Dp = 80.dp,
    color: Color = Color.Blue,
    strokeWidth: Dp = 2.dp,
    centerWave: Boolean = false,
    animationDurationMillis: Int = 1000
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

    WavyLine(
        progress = wave,
        modifier = modifier,
        crestHeight = crestHeight,
        waveLength = waveLength,
        color = color,
        strokeWidth = strokeWidth,
        centerWave = centerWave
    )
}

@Preview
@Composable
fun WavyLinePreview() {
    Surface(
        modifier = Modifier
            .height(100.dp)
            .fillMaxWidth(),
        color = Color.White,
    ) {
        WavyLine(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(vertical = 20.dp),
            color = Color.Blue
        )
    }
}

@Preview
@Composable
fun WavyLineCenteredPreview() {
    Surface(
        modifier = Modifier
            .height(100.dp)
            .fillMaxWidth(),
        color = Color.White,
    ) {
        WavyLine(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            centerWave = true,
            color = Color.Red,
            crestHeight = 10.dp
        )
    }
}

@Preview
@Composable
fun WavyLineDiagonalPreview() {
    Surface(
        modifier = Modifier
            .height(100.dp)
            .fillMaxWidth(),
        color = Color.White,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            val startPoint = Offset(0f, 0f)
            val endPoint = Offset(size.width, size.height)
            
            val path = wavyPathSegment(
                existingPath = null,
                animationProgress = 0f,
                crestHeight = 8.dp,
                waveLength = 60.dp,
                startPoint = startPoint,
                endPoint = endPoint,
            )
            
            drawPath(
                path = path,
                color = Color.Red,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}