package com.kenkeremath.uselessui.waves

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Density
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Creates a wavy path between two points and adds it to an existing path.
 * 
 * This function generates a sine wave path between two arbitrary points in 2D space.
 * The wave will be properly oriented along the line connecting the two points.
 * If an existing path is provided, the wavy segment will be added to it; otherwise,
 * a new path will be created.
 *
 * @param animationProgress Progress of the wave animation (0f to 1f). Controls the phase shift
 *                          of the sine wave, creating the animation effect when varied over time.
 * @param crestHeightPx Height of the wave crests in pixels. Controls the amplitude of the sine wave.
 * @param waveLengthPx Length of each complete wave cycle in pixels. Controls the frequency of the sine wave.
 * @param startPoint Starting point of the wavy path segment.
 * @param endPoint Ending point of the wavy path segment.
 * @param existingPath Optional existing path to add the wavy segment to. If null, a new path will be created.
 * @return The path with the wavy segment added. If existingPath was null, returns a new path.
 */
fun wavyPathSegment(
    animationProgress: Float,
    crestHeightPx: Float,
    waveLengthPx: Float,
    startPoint: Offset,
    endPoint: Offset,
    existingPath: Path? = null
): Path {
    val path = existingPath ?: Path()
    
    // Calculate the distance between points
    val dx = endPoint.x - startPoint.x
    val dy = endPoint.y - startPoint.y
    val distance = sqrt(dx * dx + dy * dy)
    
    // Calculate the angle between points
    val angle = atan2(dy, dx)
    
    // Calculate wave parameters
    val stretch = 2f * PI / waveLengthPx
    val xShift = animationProgress * 2f * PI
    
    // Determine number of segments for smooth curve
    val segmentLength = waveLengthPx / 10f
    val numSegments = (distance / segmentLength).roundToInt().coerceAtLeast(1)
    
    // Create a matrix for transformations
    val matrix = Matrix()
    matrix.translate(startPoint.x, startPoint.y)
    matrix.rotateZ(angle * (180f / PI))
    
    // Create points for the wavy path
    val points = mutableListOf<Offset>()
    
    // Add the start point
    points.add(Offset(0f, 0f))
    
    // Add intermediate points
    for (i in 1..numSegments) {
        val x = min(i * segmentLength, distance)
        val y = crestHeightPx * sin(stretch * x - xShift)
        points.add(Offset(x, y))
    }
    
    // Ensure we reach the exact end point
    if (numSegments * segmentLength < distance) {
        val x = distance
        val y = crestHeightPx * sin(stretch * x - xShift)
        points.add(Offset(x, y))
    }
    
    // Transform all points
    val transformedPoints = points.map { point ->
        matrix.map(point)
    }
    
    // Add points to the path
    if (existingPath == null) {
        path.moveTo(transformedPoints[0].x, transformedPoints[0].y)
    }
    
    for (i in 1 until transformedPoints.size) {
        path.lineTo(transformedPoints[i].x, transformedPoints[i].y)
    }
    
    return path
}

// Helper extension function to convert Dp to pixels
private fun Dp.toPx(density: Float): Float {
    return this.value * density
}

private const val PI = Math.PI.toFloat()

/**
 * Convenience function that accepts Dp values and converts them to pixels before creating the wavy path.
 */
fun wavyPathSegment(
    animationProgress: Float,
    crestHeight: Dp,
    waveLength: Dp,
    startPoint: Offset,
    endPoint: Offset,
    existingPath: Path? = null,
    density: Density
): Path {
    return wavyPathSegment(
        animationProgress = animationProgress,
        crestHeightPx = with(density) { crestHeight.toPx() },
        waveLengthPx = with(density) { waveLength.toPx() },
        startPoint = startPoint,
        endPoint = endPoint,
        existingPath = existingPath
    )
}