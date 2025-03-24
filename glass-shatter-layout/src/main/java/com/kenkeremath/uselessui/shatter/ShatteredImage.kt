package com.kenkeremath.uselessui.shatter


import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import kotlin.math.sqrt
import kotlin.random.Random


@Composable
private fun ShatteredPiece(
    shard: ShardData,
    originalBitmap: ImageBitmap,
    impactPoint: Offset,
    progress: Float,
    shatterSpec: ShatterSpec,
    showCenterPoint: Boolean = false,
) {
    if (shard.vertices.isEmpty()) return

    val randomVariations = remember(shard) {
        ShardRandomVariations(
            velocityVariation = Random.nextFloat() * shatterSpec.velocityVariation - shatterSpec.velocityVariation / 2,
            rotationXVariation = Random.nextFloat() * shatterSpec.rotationXVariation - shatterSpec.rotationXVariation / 2,
            rotationYVariation = Random.nextFloat() * shatterSpec.rotationYVariation - shatterSpec.rotationYVariation / 2,
            rotationZVariation = Random.nextFloat() * shatterSpec.rotationZVariation - shatterSpec.rotationZVariation / 2
        )
    }
    
    val direction = remember(impactPoint, shard.center) { 
        computeOutwardDirection(impactPoint, shard.center) 
    }
    
    val velocity = shatterSpec.velocity + randomVariations.velocityVariation
    val rotationXTarget = shatterSpec.rotationXTarget + randomVariations.rotationXVariation
    val rotationYTarget = shatterSpec.rotationYTarget + randomVariations.rotationYVariation
    val rotationZTarget = shatterSpec.rotationZTarget + randomVariations.rotationZVariation
    val alphaTarget = shatterSpec.alphaTarget

    // Crop the bitmap on demand and remember it
    val croppedBitmap = remember(originalBitmap, shard.path, shard.shardBoundingRect) {
        cropBitmapToFragmentBounds(originalBitmap, shard.path, shard.shardBoundingRect)
    }

    Box(
        modifier = Modifier
            .graphicsLayer {
                translationX = progress * direction.first * velocity
                translationY = progress * direction.second * velocity
                transformOrigin =
                    TransformOrigin(shard.boundingCenterFractionX, shard.boundingCenterFractionY)
                rotationX = progress * rotationXTarget
                rotationY = progress * rotationYTarget
                rotationZ = progress * rotationZTarget
                alpha = 1f - progress * (1f - alphaTarget)
                cameraDistance = 16f * density
            }
            .size(
                with(LocalDensity.current) { shard.parentBoundingRect.width().toDp() },
                with(LocalDensity.current) { shard.parentBoundingRect.height().toDp() }
            )
    ) {
        Image(
            bitmap = croppedBitmap,
            contentDescription = null,
            modifier = Modifier
                .graphicsLayer {
                    // Position the cropped bitmap at the correct location
                    translationX = shard.shardBoundingRect.left
                    translationY = shard.shardBoundingRect.top
                }
        )

        if (showCenterPoint) {
            androidx.compose.foundation.Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                drawCircle(
                    color = androidx.compose.ui.graphics.Color.Red,
                    radius = 8.dp.toPx(),
                    center = shard.center
                )

                // Impact vector
                drawLine(
                    color = androidx.compose.ui.graphics.Color.Yellow,
                    start = shard.center,
                    end = impactPoint,
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
    }
}

@Composable
internal fun ShatteredImage(
    bitmap: ImageBitmap,
    progress: Float,
    modifier: Modifier = Modifier,
    shatterCenter: Offset = Offset.Unspecified,
    shatterSpec: ShatterSpec = ShatterSpec(),
    showCenterPoints: Boolean = false,
) {
    val impactPoint = remember(shatterCenter, bitmap) {
        if (shatterCenter == Offset.Unspecified) Offset(
            bitmap.width / 2f,
            bitmap.height / 2f
        ) else shatterCenter
    }

    val width = bitmap.width.toFloat()
    val height = bitmap.height.toFloat()
    val shards = remember(bitmap) {
        generateVoronoiShards(10, width, height).map { path ->
            val fragmentBounds = RectF()
            path.path.asAndroidPath().computeBounds(fragmentBounds, true)
            ShardData(
                path = path.path,
                vertices = path.vertices,
                parentBoundingRect = RectF(0f, 0f, width, height),
                shardBoundingRect = fragmentBounds
            )
        }.filter { fragment ->
            // Filter out fragments with empty paths or zero area
            fragment.shardBoundingRect.width() > 0 &&
                    fragment.shardBoundingRect.height() > 0 &&
                    fragment.vertices.isNotEmpty()
        }
    }

    Box(
        modifier = modifier
    ) {
        shards.forEach { shard ->
            ShatteredPiece(
                shard = shard,
                originalBitmap = bitmap,
                impactPoint = impactPoint,
                progress = progress,
                shatterSpec = shatterSpec,
                showCenterPoint = showCenterPoints
            )
        }
    }
}


private data class ShardData(
    val path: Path,
    val vertices: List<Offset>,
    val parentBoundingRect: RectF, // Bounding box of the original bitmap
    val shardBoundingRect: RectF // Bounding box of just this shard
) {

    val center: Offset
        get() {
            val centroidX = vertices.sumOf { it.x.toDouble() } / vertices.size
            val centroidY = vertices.sumOf { it.y.toDouble() } / vertices.size
            return Offset(centroidX.toFloat(), centroidY.toFloat())
        }

    // What fraction from 0f to 1f is the center of our shape in our bounding box in x and y
    // For instance, a normal rectangle would be (.5, .5), but within our bounding box the "center"
    // of a shard is a different location in the bounding box.
    val boundingCenterFractionX: Float
        get() = (center.x - parentBoundingRect.left) / parentBoundingRect.width()

    val boundingCenterFractionY: Float
        get() = (center.y - parentBoundingRect.top) / parentBoundingRect.height()

}

@Preview
@Composable
private fun ShatteredPiecePreview() {
    val bitmap = remember {
        createColoredBitmap(
            300,
            300,
            Color.argb(255, 0, 150, 255)
        ).asImageBitmap()
    }

    val path = Path().apply {
        moveTo(50f, 50f)
        lineTo(250f, 100f)
        lineTo(150f, 250f)
        close()
    }

    val bounds = RectF()
    path.asAndroidPath().computeBounds(bounds, true)

    val fragment = ShardData(
        path = path,
        vertices = listOf(
            Offset(50f, 50f),
            Offset(250f, 100f),
            Offset(150f, 250f)
        ),
        parentBoundingRect = RectF(0f, 0f, 300f, 300f),
        shardBoundingRect = bounds
    )

    Surface {
        Box(modifier = Modifier.size(300.dp)) {
            ShatteredPiece(
                shard = fragment,
                originalBitmap = bitmap,
                impactPoint = Offset(150f, 150f),
                progress = 0f,
                shatterSpec = ShatterSpec(),
                showCenterPoint = true
            )
        }
    }
}

@Preview
@Composable
private fun ShatteredImageComposablePreview() {
    val bitmap = remember {
        createColoredBitmap(
            1000,
            1000,
            Color.argb(255, 255, 0, 255)
        ).asImageBitmap()
    }

    Surface {
        Box {
            ShatteredImage(
                bitmap = bitmap,
                progress = .5f,
                shatterSpec = ShatterSpec(),
                showCenterPoints = true
            )
        }
    }
}

private fun cropBitmapToFragmentBounds(
    bitmap: ImageBitmap,
    path: Path,
    bounds: RectF
): ImageBitmap {
    // Create a bitmap that's only as large as the fragment's bounding box
    val left = bounds.left.toInt().coerceAtLeast(0)
    val top = bounds.top.toInt().coerceAtLeast(0)
    val width = bounds.width().toInt().coerceAtMost(bitmap.width - left)
    val height = bounds.height().toInt().coerceAtMost(bitmap.height - top)

    // Safety check - if dimensions are invalid, return a 1x1 transparent bitmap
    if (width <= 0 || height <= 0) {
        val fallbackBitmap = createBitmap(1, 1)
        fallbackBitmap.eraseColor(Color.TRANSPARENT)
        return fallbackBitmap.asImageBitmap()
    }

    // Create a bitmap of the exact size needed
    val resultBitmap = createBitmap(width, height)
    val canvas = android.graphics.Canvas(resultBitmap)

    // Translate the canvas so the path is positioned correctly
    canvas.translate(-left.toFloat(), -top.toFloat())

    val paint = Paint().apply {
        isAntiAlias = true
    }
    canvas.drawPath(path.asAndroidPath(), paint)
    paint.apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    }

    // Draw only the portion of the original bitmap that we need
    canvas.drawBitmap(bitmap.asAndroidBitmap(), 0f, 0f, paint)

    return resultBitmap.asImageBitmap()
}

private fun computeOutwardDirection(center: Offset, shardCenter: Offset): Pair<Float, Float> {
    val dx = shardCenter.x - center.x
    val dy = shardCenter.y - center.y
    val distance = sqrt(dx * dx + dy * dy)

    return if (distance > 0f) {
        Pair(dx / distance, dy / distance) // Normalize vector
    } else {
        Pair(0f, 0f) // Prevent division by zero
    }
}

private fun createColoredBitmap(width: Int, height: Int, color: Int): Bitmap {
    val bitmap = createBitmap(width, height)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = Paint().apply {
        this.color = color
        this.style = Paint.Style.FILL
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    paint.apply {
        this.color = Color.BLACK
        this.style = Paint.Style.FILL
    }
    canvas.drawRect(width / 2f, height / 2f, width.toFloat(), height.toFloat(), paint)

    return bitmap
}

// Add this data class to store random variations
private data class ShardRandomVariations(
    val velocityVariation: Float,
    val rotationXVariation: Float,
    val rotationYVariation: Float,
    val rotationZVariation: Float
)

