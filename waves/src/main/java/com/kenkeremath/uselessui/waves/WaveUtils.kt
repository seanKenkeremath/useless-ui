package com.kenkeremath.uselessui.waves

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Creates a wavy path between two points.
 *
 * @param animationProgress Progress of the wave animation (0f to 1f)
 * @param crestHeight Height of the wave crests
 * @param waveLength Length of each wave
 * @param startPoint Starting point of the path
 * @param endPoint Ending point of the path
 * @return A Path object containing the wavy line
 */
fun createWavyPath(
    animationProgress: Float,
    crestHeight: Dp,
    waveLength: Dp,
    startPoint: Offset,
    endPoint: Offset
): Path {
    // Calculate the distance between points
    val dx = endPoint.x - startPoint.x
    val dy = endPoint.y - startPoint.y
    val distance = sqrt(dx * dx + dy * dy)
    
    // Calculate the angle between points
    val angle = atan2(dy, dx)
    
    // Convert parameters to pixels
    val amplitudePx = crestHeight.value
    val waveLengthPx = waveLength.value
    
    // Calculate wave parameters
    val stretch = 2f * PI / waveLengthPx
    val xShift = animationProgress * 2f * PI
    
    // Determine number of segments for smooth curve
    val segmentLength = waveLengthPx / 10f
    val numSegments = (distance / segmentLength).roundToInt()
    
    // Create a horizontal wavy path
    val path = Path()
    path.moveTo(0f, 0f)
    
    for (i in 1..numSegments) {
        val x = min(i * segmentLength, distance)
        val y = amplitudePx * sin(stretch * x - xShift)
        path.lineTo(x, y)
    }
    
    // Ensure we reach the exact distance
    if (numSegments * segmentLength < distance) {
        path.lineTo(distance, amplitudePx * sin(stretch * distance - xShift))
    }
    
    // Create a transformation matrix to rotate and translate the path
    val matrix = Matrix()
    // First translate to origin
    matrix.translate(startPoint.x, startPoint.y)
    // Then rotate around origin
    matrix.rotateZ(angle * (180f / PI))
    path.transform(matrix)
    return path
}

private const val PI = Math.PI.toFloat()