package com.kenkeremath.uselessui.shatter

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
 * This layout captures a bitmap of its content and can animate a shattering effect
 * when the shatter state changes. The content can be in one of three states:
 * intact (normal), shattered (broken into pieces), or reassembled (pieces back in place but with cracks).
 *
 * @param shatterState The current state of the shatter effect (Intact, Shattered, Reassembled)
 * @param modifier Modifier to be applied to the layout
 * @param contentKey A key to invalidate the content bitmap when it changes
 * @param captureMode Controls when the content bitmap is captured (AUTO or LAZY)
 * @param shatterCenter The center point of the shatter effect, Offset.Unspecified for center
 * @param shatterSpec Configuration for the shatter animation properties
 * @param showCenterPoints Whether to show debug points for the shatter centers
 * @param onAnimationCompleted Callback when the animation to the target state completes
 * @param content The content to be displayed and potentially shattered
 */
@Composable
fun ShatterableLayout(
    shatterState: ShatterState,
    modifier: Modifier = Modifier,
    contentKey: Any? = null,
    captureMode: CaptureMode = CaptureMode.AUTO,
    shatterCenter: Offset = Offset.Unspecified,
    shatterSpec: ShatterSpec = ShatterSpec(),
    showCenterPoints: Boolean = false,
    onAnimationCompleted: (ShatterState) -> Unit = {},
    content: @Composable () -> Unit
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    var contentBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var needsRecapture by remember { mutableStateOf(false) }

    val targetProgress = when (shatterState) {
        ShatterState.Intact, ShatterState.Reassembled -> 0f
        ShatterState.Shattered -> 1f
    }

    val progress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = shatterSpec.durationMillis.toInt()),
        label = "shatter",
        finishedListener = { _ ->
            onAnimationCompleted(shatterState)
        }
    )

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
        if ((captureMode != CaptureMode.LAZY || shatterState != ShatterState.Intact) && (contentBitmap == null || needsRecapture) && size.width > 0 && size.height > 0) {
            AndroidView(
                factory = { ctx ->
                    ComposeView(ctx).apply {
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
                                Log.d("REC", "capturing bitmap")
                                contentBitmap = bitmap.asImageBitmap()
                                needsRecapture = false
                            }
                        }
                    }
                }
            )
        } else if (contentBitmap != null && (shatterState != ShatterState.Intact || progress > 0f)) {
            // Show shattered version if we're not intact OR if we're still animating back to intact
            ShatteredImage(
                bitmap = contentBitmap!!,
                shatterCenter = shatterCenter,
                progress = progress,
                shatterSpec = shatterSpec,
                showCenterPoints = showCenterPoints,
                modifier = Modifier.size(
                    size.width.pxToDp(), size.height.pxToDp()
                )
            )
        } else {
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
 * @property durationMillis Duration of the shatter animation in milliseconds
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
data class ShatterSpec(
    val durationMillis: Long = 500L,
    val shardCount: Int = 15,
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

/**
 * The state that the shattered layout can be in.
 * 
 * The layout will animate transitions between these states.
 */
enum class ShatterState {
    /**
     * The layout is not shattered and will render the live content.
     * This is the normal state where the content is displayed as-is.
     */
    Intact,
    
    /**
     * The layout will render the shattered version of a captured bitmap of the content.
     * In this state, the pieces of the content are animated moving away from each other.
     */
    Shattered,
    
    /**
     * The layout will render the individual pieces of the captured bitmap in their original positions.
     * This state is as if you have tried to reassemble broken shards of something but it's still cracked.
     * In this state, the live content will not be displayed, and cracks will still be visible.
     */
    Reassembled
}