package com.seankenkeremath.uselessui.shatter


import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.launch
import kotlin.math.sqrt
import kotlin.random.Random


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

@Composable
private fun ShatteredPiece(shard: ShatteredFragment, impactPoint: Offset) {
    val animationMillis = remember { 2000 }
    val animX = remember { Animatable(0f) }
    val animY = remember { Animatable(0f) }
    val animAlpha = remember { Animatable(1f) }
    val animRotationX = remember { Animatable(0f) }
    val animRotationY = remember { Animatable(0f) }
    val animRotationZ = remember { Animatable(0f) }

    val direction = remember { computeOutwardDirection(impactPoint, shard.center) }
    val velocity = remember { Random.nextFloat() * 200f + 100f } // Random explosion force

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            launch {
                animX.animateTo(direction.first * velocity, animationSpec = tween(animationMillis))
            }
            launch {
                animY.animateTo(direction.second * velocity, animationSpec = tween(animationMillis))
            }
            launch {
                animAlpha.animateTo(
                    0f,
                    animationSpec = tween((animationMillis * 1.2).toInt())
                )
            }
            launch {
                animRotationX.animateTo(
                    (Random.nextFloat() * 60f - 30f) * 4,
                    animationSpec = tween(animationMillis)
                )
            }
            launch {
                animRotationY.animateTo(
                    (Random.nextFloat() * 60f - 30f) * 4,
                    animationSpec = tween(animationMillis)
                )
            }
            launch {
                animRotationZ.animateTo(
                    (Random.nextFloat() * 60f - 30f) * 4,
                    animationSpec = tween(animationMillis)
                )
            }
        }
    }

    Image(
        bitmap = shard.bitmap,
        contentDescription = null,
        modifier = Modifier
            .graphicsLayer {
                translationX = animX.value
                translationY = animY.value
                transformOrigin = TransformOrigin(shard.boundingCenterFractionX, shard.boundingCenterFractionY)
                rotationX = animRotationX.value
                rotationY = animRotationY.value
                rotationZ = animRotationZ.value
                alpha = animAlpha.value
                cameraDistance = 16f * density
            }
    )
}

@Composable
fun GlassShatterEffect(bitmap: ImageBitmap, modifier: Modifier = Modifier) {
    val b = remember {
        bitmap
    }

    var impactPoint by remember { mutableStateOf(Offset.Zero) }
    var shattered by remember { mutableStateOf(false) }

    val width = bitmap.width.toFloat()
    val height = bitmap.height.toFloat()
    val shards = remember {
        generateVoronoiShards(10, b.width.toFloat(), b.height.toFloat()).map { path ->
            ShatteredFragment(
                path = path.path,
                bitmap = cropBitmap(b, path.path, width.toInt(), height.toInt()),
                vertices = path.vertices,
                boundingRect = RectF(0f, 0f, width, height)
            )
        }
    }

    Box(
        modifier = modifier
            .size(b.width.dp, b.height.dp)
            .pointerInput(Unit) {
                detectTapGestures { tapPosition ->
                    impactPoint = tapPosition
                    shattered = true
                }
            }
    ) {
        if (shattered) {
            shards.forEach { shard ->
                ShatteredPiece(shard, impactPoint)
            }
        } else {
            Image(bitmap = bitmap, contentDescription = null)
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

    // At what fraction from 0 to 1f is the center of our shape relative to the bounding box
    // For instance, a normal rectangle would be (.5, .5), but within our bounding box the "center"
    // of a shard is a different location in the bounding box.
    val boundingCenterFractionX: Float
        get() = (center.x - boundingRect.left) / boundingRect.width()

    val boundingCenterFractionY: Float
        get() = (center.y - boundingRect.top) / boundingRect.height()

}


@Preview
@Composable
private fun GlassShatterComposablePreview() {
    val bitmap = remember {
        createColoredBitmap(
            1000,
            1000,
            android.graphics.Color.argb(255, 255, 0, 255)
        ).asImageBitmap()
    }

    Surface {
        Box {
            GlassShatterEffect(bitmap = bitmap)
        }
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

