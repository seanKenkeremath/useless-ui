package com.kenkeremath.uselessui.shatter

import android.view.View
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.createBitmap

/**
 * A composable that can shatter its content when triggered.
 *
 * This layout captures a bitmap of its content and can render its content as a shattered image
 * based on the ShatterSpec parameters passed in.
 *
 * @param progress How far along (0..1f) the shatter effect should be rendered. 1f is completely shattered, 0f is intact
 * @param modifier Modifier to be applied to the layout
 * @param contentKey A key to invalidate the content bitmap when it changes
 * @param captureMode Controls when the content bitmap is captured (AUTO or LAZY)
 * @param shatterCenter The center point of the shatter effect, Offset.Unspecified for center
 * @param shatterSpec Configuration for the shatter animation properties
 * @param showCenterPoints Whether to show debug points for the shatter centers
 * @param content The content to be displayed and potentially shattered
 */
@Composable
fun ShatterableLayout(
    progress: Float,
    modifier: Modifier = Modifier,
    contentKey: Any? = null,
    captureMode: CaptureMode = CaptureMode.AUTO,
    showCenterPoints: Boolean = false,
    shatterCenter: Offset = Offset.Unspecified,
    shatterSpec: ShatterSpec = ShatterSpec(),
    content: @Composable () -> Unit
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    var contentBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var needsRecapture by remember { mutableStateOf(false) }

    // Handle content key changes by marking the bitmap as invalid
    LaunchedEffect(contentKey) {
        if (contentBitmap != null) {
            needsRecapture = true
        }
    }

    // Handle size changes by marking the bitmap as invalid
    LaunchedEffect(size) {
        if (contentBitmap != null &&
            (contentBitmap!!.width != size.width ||
                    contentBitmap!!.height != size.height)
        ) {
            needsRecapture = true
        }
    }

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                if (size != coordinates.size) {
                    size = coordinates.size
                }
            }
    ) {
        // Capture the content bitmap if needed
        if ((captureMode != CaptureMode.LAZY || progress > 0f) && (contentBitmap == null || needsRecapture) && size.width > 0 && size.height > 0) {
            AndroidView(
                factory = { ctx ->
                    ComposeView(ctx).apply {
                        // Prevent this from being rendered for the first frame
                        // if we are starting shattered
                        visibility = if (progress < .1f) View.VISIBLE else View.INVISIBLE
                        setContent {
                            Box {
                                content()
                            }
                        }

                        // Capture the bitmap after layout
                        post {
                            if (width > 0 && height > 0) {
                                val bitmap = createBitmap(width, height)
                                val canvas = android.graphics.Canvas(bitmap)
                                draw(canvas)
                                contentBitmap = bitmap.asImageBitmap()
                                needsRecapture = false
                            }
                        }
                    }
                }
            )
        } else if (contentBitmap != null && progress > 0f) {
            // Show shattered version if we're not intact OR if we're still animating back to intact
            ShatteredImage(
                bitmap = contentBitmap!!,
                shatterCenter = shatterCenter,
                showCenterPoints = showCenterPoints,
                progress = progress,
                shatterSpec = shatterSpec,
                modifier = Modifier.size(
                    size.width.pxToDp(), size.height.pxToDp()
                )
            )
        } else if (progress < 0.1f) {
            // Otherwise show the normal content
            content()
        }
    }
}

@Composable
internal fun Int.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }

/**
 * Controls when the content bitmap is captured in ShatterableLayout.
 *
 * This determines the strategy for when to capture the bitmap of the content
 * that will be used for the shatter effect.
 */
enum class CaptureMode {
    /**
     * Automatically recapture the bitmap when content key changes or size changes.
     * This ensures the shattered image always reflects the current content but may
     * use more resources.
     */
    AUTO,

    /**
     * Only capture the bitmap when transitioning to the shattered state.
     * The old bitmap is invalidated when content key or size changes, but a new
     * bitmap is only captured when needed. This is more efficient but may not
     * always reflect the latest content state.
     */
    LAZY,
}

/**
 * Configuration for the shatter animation effect.
 *
 * This class defines how the shatter animation should behave, including the number of shards,
 * speed, rotation, and transparency of the shattered pieces.
 *
 * Velocity determines how quickly the pieces move and therefore how far they go.
 * All target values represent what the values of those properties should be when
 * the shatter has completed.
 * Variation values are the range within which a property will randomly vary for
 * a given piece. This is important to make all pieces have some slight differences
 * in movement.
 *
 * @property shardCount Number of pieces the content will be broken into
 * @property velocity Base velocity of the shattered pieces
 * @property rotationXTarget Target X-axis rotation of pieces at the end of animation
 * @property rotationYTarget Target Y-axis rotation of pieces at the end of animation
 * @property rotationZTarget Target Z-axis rotation of pieces at the end of animation
 * @property velocityVariation Random variation in velocity between pieces
 * @property rotationXVariation Random variation in X-axis rotation between pieces
 * @property rotationYVariation Random variation in Y-axis rotation between pieces
 * @property rotationZVariation Random variation in Z-axis rotation between pieces
 * @property alphaTarget Target transparency of pieces at the end of animation (0 = transparent)
 */
@Stable
data class ShatterSpec(
    val shardCount: Int = 15,
    val easing: Easing = FastOutSlowInEasing,
    val velocity: Float = 300f,
    val rotationXTarget: Float = 30f,
    val rotationYTarget: Float = 30f,
    val rotationZTarget: Float = 30f,
    val velocityVariation: Float = 100f,
    val rotationXVariation: Float = 10f,
    val rotationYVariation: Float = 10f,
    val rotationZVariation: Float = 10f,
    val alphaTarget: Float = 0f,
)