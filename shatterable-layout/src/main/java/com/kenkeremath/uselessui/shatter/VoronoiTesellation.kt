package com.kenkeremath.uselessui.shatter

import android.graphics.RectF
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.toComposeRect
import org.locationtech.jts.triangulate.VoronoiDiagramBuilder
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryCollection
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Polygon
import kotlin.random.Random

internal class VoronoiTessellation(points: List<Pair<Float, Float>>) {
    val cells: List<VoronoiCell>
    init {
        val factory = GeometryFactory()
        val voronoiBuilder = VoronoiDiagramBuilder()

        val coords = points.map { Coordinate(it.first.toDouble(), it.second.toDouble()) }

        voronoiBuilder.setSites(coords)
        val diagram = voronoiBuilder.getDiagram(factory) as GeometryCollection

        cells = (0 until diagram.numGeometries).map { i ->
            val poly = diagram.getGeometryN(i) as Polygon
            val edges = poly.coordinates.map { Pair(it.x.toFloat(), it.y.toFloat()) }
            VoronoiCell(edges)
        }
    }
}
internal data class VoronoiCell(val edges: List<Pair<Float, Float>>)

internal fun generateVoronoiShards(count: Int, width: Float, height: Float): List<PathWithVertices> {
    // Create a slightly larger area for the Voronoi diagram to ensure edge coverage
    val padding = 20f
    
    val points = mutableListOf<Pair<Float, Float>>().apply {
        // Add random points inside the actual image area
        repeat(count) {
            add(Random.nextFloat() * width to Random.nextFloat() * height)
        }
        
        // Add points at corners
        add(0f to 0f)
        add(width to 0f)
        add(0f to height)
        add(width to height)
        
        // Add points at edge midpoints
        add(width/2f to 0f)
        add(width/2f to height)
        add(0f to height/2f)
        add(width to height/2f)
        
        // Add points outside the image to create proper cells at the edges
        // These will be clipped later but help create better edge fragments
        add(-padding to -padding)
        add(width + padding to -padding)
        add(-padding to height + padding)
        add(width + padding to height + padding)
        
        // Add more points along the outside edges
        add(-padding to height/2f)
        add(width + padding to height/2f)
        add(width/2f to -padding)
        add(width/2f to height + padding)
    }
    
    // Create the bounds for clipping
    val bounds = RectF(0f, 0f, width, height)
    
    // Generate the Voronoi diagram with the expanded points
    val voronoi = VoronoiTessellation(points)
    
    // Process the cells and clip them to the image bounds
    return voronoi.cells.mapNotNull { cell ->
        val vertices = cell.edges.map { Offset(it.first, it.second) }
        
        // Skip empty cells
        if (vertices.isEmpty()) return@mapNotNull null
        
        // Create the path
        val path = Path().apply {
            moveTo(vertices.first().x, vertices.first().y)
            vertices.forEach { lineTo(it.x, it.y) }
            close()
        }
        
        // Clip the path to the image bounds
        val clipPath = Path().apply {
            addRect(bounds.toComposeRect())
        }
        
        val clippedPath = Path().apply {
            op(path, clipPath, androidx.compose.ui.graphics.PathOperation.Intersect)
        }
        
        // Check if the clipped path is empty (completely outside the bounds)
        val pathBounds = RectF()
        clippedPath.asAndroidPath().computeBounds(pathBounds, true)
        
        // Skip cells that don't intersect with the image bounds
        if (pathBounds.width() <= 0 || pathBounds.height() <= 0) {
            return@mapNotNull null
        }
        
        // Return the clipped path with its vertices
        PathWithVertices(clippedPath, vertices)
    }
}

internal data class PathWithVertices(
    val path: Path,
    val vertices: List<Offset>
)