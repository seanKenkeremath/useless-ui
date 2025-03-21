package com.kenkeremath.uselessui.shatter


import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.graphics.createBitmap
import kotlin.math.sqrt
import kotlin.random.Random


@Composable
private fun ShatteredPiece(
    shard: ShatteredFragment,
    impactPoint: Offset,
    progress: Float,
) {
    val direction = remember { computeOutwardDirection(impactPoint, shard.center) }
    val velocity = remember { Random.nextFloat() * 200f + 100f }

    val rotationXTarget = remember { (Random.nextFloat() * 60f - 30f) * 4 }
    val rotationYTarget = remember { (Random.nextFloat() * 60f - 30f) * 4 }
    val rotationZTarget = remember { (Random.nextFloat() * 60f - 30f) * 4 }

    Image(
        bitmap = shard.bitmap,
        contentDescription = null,
        modifier = Modifier
            .graphicsLayer {
                translationX = progress * direction.first * velocity
                translationY = progress * direction.second * velocity
                transformOrigin =
                    TransformOrigin(shard.boundingCenterFractionX, shard.boundingCenterFractionY)
                rotationX = progress * rotationXTarget
                rotationY = progress * rotationYTarget
                rotationZ = progress * rotationZTarget
                alpha = 1f - progress * 0.8f
                cameraDistance = 16f * density
            }
    )
}

@Composable
internal fun ShatteredImage(
    bitmap: ImageBitmap,
    progress: Float,
    hasBeenShattered: Boolean,
    modifier: Modifier = Modifier,
    shatterCenter: Offset = Offset.Unspecified,
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
        generateVoronoiShards(10, bitmap.width.toFloat(), bitmap.height.toFloat()).map { path ->
            ShatteredFragment(
                path = path.path,
                bitmap = cropBitmap(bitmap, path.path, width.toInt(), height.toInt()),
                vertices = path.vertices,
                boundingRect = RectF(0f, 0f, width, height)
            )
        }
    }

    Box(
        modifier = modifier
    ) {
        if (!hasBeenShattered) {
            Image(bitmap = bitmap, contentDescription = null)
        } else {
            shards.forEach { shard ->
                ShatteredPiece(
                    shard = shard,
                    impactPoint = impactPoint,
                    progress = progress
                )
            }
        }
    }
}


private data class ShatteredFragment(
    val path: Path,
    val bitmap: ImageBitmap,
    val vertices: List<Offset>,
    val boundingRect: RectF // Bounding box of the entire original glass area
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
        get() = (center.x - boundingRect.left) / boundingRect.width()

    val boundingCenterFractionY: Float
        get() = (center.y - boundingRect.top) / boundingRect.height()

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
            ShatteredImage(bitmap = bitmap, hasBeenShattered = true, progress = .5f)
        }
    }
}

private fun cropBitmap(bitmap: ImageBitmap, path: Path, width: Int, height: Int): ImageBitmap {
    val resultBitmap = createBitmap(width, height)
    val canvas = android.graphics.Canvas(resultBitmap)

    val paint = Paint().apply {
        isAntiAlias = true
    }
    canvas.drawPath(path.asAndroidPath(), paint)
    paint.apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    }
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

