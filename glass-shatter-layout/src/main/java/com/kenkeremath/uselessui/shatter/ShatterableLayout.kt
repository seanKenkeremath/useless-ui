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
 * @param isShattered Whether the content should be shattered. Changing this value will trigger an animation to that state.
 * @param modifier Modifier to be applied to the layout
 * @param continueWhenReassembled If true, the original content will continue rendering after unshattering
 * @param contentKey A key to invalidate the content bitmap
 * @param captureMode Controls when the content bitmap is captured
 * @param content The content to be displayed and potentially shattered
 */
@Composable
fun ShatterableLayout(
    isShattered: Boolean,
    modifier: Modifier = Modifier,
    continueWhenReassembled: Boolean = false,
    contentKey: Any? = null,
    captureMode: CaptureMode = CaptureMode.AUTO,
    shatterCenter: Offset = Offset.Unspecified,
    shatterSpec: ShatterSpec = ShatterSpec(),
    content: @Composable () -> Unit
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    var contentBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var shattered by remember { mutableStateOf(isShattered) }
    var hasBeenShattered by remember { mutableStateOf(isShattered) }
    var needsRecapture by remember { mutableStateOf(false) }

    val progress by animateFloatAsState(
        targetValue = if (shattered) 1f else 0f,
        animationSpec = tween(durationMillis = shatterSpec.durationMillis.toInt()),
        label = "shatter",
        finishedListener = { float ->
            if (float == 0f) {
                if (continueWhenReassembled) {
                    hasBeenShattered = false
                }
            }
        }
    )

    // Invalidate bitmap when content key changes
    LaunchedEffect(contentKey) {
        if (captureMode != CaptureMode.LAZY) {
            if (!shattered) {
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

    // Handle shatter state changes
    LaunchedEffect(isShattered) {
        if (shattered != isShattered) {
            shattered = isShattered
            if (isShattered) {
                // For LAZY mode, only capture when transitioning to shattered state for the first time
                // Once it's been shattered we shouldn't recapture
                if (!hasBeenShattered && (captureMode == CaptureMode.LAZY || needsRecapture)) {
                    contentBitmap = null
                    needsRecapture = false
                }
                hasBeenShattered = true
            }
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
        } else if (contentBitmap != null && (shattered || hasBeenShattered)) {
            // If we have the bitmap and have shattered, show the shattered version
            ShatteredImage(
                bitmap = contentBitmap!!,
                shatterCenter = shatterCenter,
                progress = progress,
                hasBeenShattered = hasBeenShattered,
                shatterSpec = shatterSpec,
                showCenterPoints = true,
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