package com.kenkeremath.uselessui.shatter

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
 * @param shatterState The current state of the shatter effect (Intact, Shattered, Reassembled)
 * @param modifier Modifier to be applied to the layout
 * @param contentKey A key to invalidate the content bitmap
 * @param captureMode Controls when the content bitmap is captured
 * @param shatterCenter The center point of the shatter effect, Offset.Unspecified for center
 * @param shatterSpec Configuration for the shatter animation
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
    var previousShatterState by remember { mutableStateOf(shatterState) }

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

    // Handle content key changes
    LaunchedEffect(contentKey) {
        if (captureMode != CaptureMode.LAZY) {
            if (shatterState == ShatterState.Intact) {
                contentBitmap = null
            } else {
                needsRecapture = true
            }
        }
    }

    // Handle size changes
    LaunchedEffect(size) {
        if (contentBitmap != null &&
            (contentBitmap!!.width != size.width ||
                    contentBitmap!!.height != size.height)
        ) {
            contentBitmap = null
        }
    }

    // Handle state transitions
    LaunchedEffect(shatterState) {
        if (shatterState != previousShatterState) {
            // If transitioning from intact to shattered, we need to capture the content
            if (previousShatterState == ShatterState.Intact &&
                shatterState == ShatterState.Shattered) {
                if (captureMode == CaptureMode.LAZY || needsRecapture) {
                    contentBitmap = null
                    needsRecapture = false
                }
            }
            
            previousShatterState = shatterState
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
        // If we need to capture the bitmap and haven't yet
        if (contentBitmap == null && size.width > 0 && size.height > 0) {
            // Show the content and capture it
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
                                contentBitmap = bitmap.asImageBitmap()
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
 * Controls when the content bitmap is captured in ShatterableLayout
 */
enum class CaptureMode {
    /**
     * Automatically recapture when content key changes or size changes
     */
    AUTO,

    /**
     * Only capture the bitmap when transitioning to the shattered state
     */
    LAZY,

    /**
     * Capture once and reuse the bitmap until explicitly invalidated via contentKey
     */
    ONCE
}

data class ShatterSpec(
    val durationMillis: Long = 500L,
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