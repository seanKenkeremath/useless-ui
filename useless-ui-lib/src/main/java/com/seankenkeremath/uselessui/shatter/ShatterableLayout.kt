package com.seankenkeremath.uselessui.shatter

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.createBitmap

/**
 * A composable that can shatter its content when triggered.
 *
 * @param isShattered Whether the content should be shattered. Changing this value will trigger an animation to that state.
 * @param modifier Modifier to be applied to the layout
 * @param content The content to be displayed and potentially shattered
 */
@Composable
fun ShatterableLayout(
    isShattered: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    var contentBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var shattered by remember { mutableStateOf(isShattered) }
    var hasBeenShattered by remember { mutableStateOf(false) }

    LaunchedEffect(isShattered) {
        if (shattered != isShattered) {
            shattered = isShattered
            if (isShattered) {
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
        } else if (contentBitmap != null && (isShattered || hasBeenShattered)) {
            // If we have the bitmap and have shattered, show the shattered version
            ShatteredImage(
                bitmap = contentBitmap!!,
                isShattered = shattered
            )
        } else {
            // Otherwise show the normal content
            content()
        }
    }
} 