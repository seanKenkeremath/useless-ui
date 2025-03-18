package com.seankenkeremath.uselessui.shatter

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
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
    val points = mutableListOf<Pair<Float, Float>>().apply {
        repeat(count) {
            add(Random.nextFloat() * width to Random.nextFloat() * height)
        }
        add(0f to 0f)
        add(width to 0f)
        add(0f to height)
        add(width to height)
    }

    return voronoiTessellate(points)
}

internal fun voronoiTessellate(
    points: List<Pair<Float, Float>>,
): List<PathWithVertices> {
    val shards = mutableListOf<PathWithVertices>()
    val voronoi = VoronoiTessellation(points)

    voronoi.cells.forEach { cell ->
        val vertices = cell.edges.map { Offset(it.first, it.second) }
        val path = Path().apply {
            moveTo(cell.edges.first().first, cell.edges.first().second)
            cell.edges.forEach { edge -> lineTo(edge.first, edge.second) }
            close()
        }
        shards.add(PathWithVertices(path, vertices))
    }

    return shards
}

internal data class PathWithVertices(
    val path: Path,
    val vertices: List<Offset>
)