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
 * Creates a wavy path between two points and adds it to an existing path.
 *
 * @param animationProgress Progress of the wave animation (0f to 1f)
 * @param crestHeight Height of the wave crests
 * @param waveLength Length of each wave
 * @param startPoint Starting point of the path
 * @param endPoint Ending point of the path
 * @param existingPath The path to add the wavy segment to (or null to create a new path)
 * @return The path with the wavy segment added
 */
fun wavyPathSegment(
    animationProgress: Float,
    crestHeight: Dp,
    waveLength: Dp,
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
    
    // Convert parameters to pixels
    val amplitudePx = crestHeight.value
    val waveLengthPx = waveLength.value
    
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
        val y = amplitudePx * sin(stretch * x - xShift)
        points.add(Offset(x, y))
    }
    
    // Ensure we reach the exact end point
    if (numSegments * segmentLength < distance) {
        val x = distance
        val y = amplitudePx * sin(stretch * x - xShift)
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

private const val PI = Math.PI.toFloat()