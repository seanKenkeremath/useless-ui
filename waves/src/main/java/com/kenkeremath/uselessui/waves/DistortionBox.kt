package com.kenkeremath.uselessui.waves

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * A composable that applies a wave distortion effect to its content using RuntimeShader.
 * This component requires Android 13 (API 33/Tiramisu) or higher to work.
 * 
 * Adapted from https://medium.com/@kappdev/magic-wavy-button-in-jetpack-compose-unleashed-the-power-of-agsl-shaders-in-android-de502f882e35
 *
 * @param modifier Modifier to be applied to the component
 * @param playAnimation Whether to play the wave animation
 * @param content The content to be displayed inside the distorted box
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun DistortionBox(
    modifier: Modifier = Modifier,
    playAnimation: Boolean = true,
    content: @Composable BoxScope.() -> Unit = {},
) {
    var time by remember { mutableFloatStateOf(0f) }

    // Simulate frame updates for the animation
    LaunchedEffect(playAnimation) {
        while (playAnimation) {
            // Simulate 60 FPS
            delay(16)
            time += 0.016f
        }
    }

    DistortionBoxImpl(
        time = time,
        modifier = modifier,
    ) {
        content()
    }
}

/**
 * A composable that applies a wave distortion effect to its content using RuntimeShader.
 * This component requires Android 13 (API 33/Tiramisu) or higher to work. This is the stated-hoisted
 * version that allows the caller to control the flow of the animation by passing in the time value
 *
 * Adapted from https://medium.com/@kappdev/magic-wavy-button-in-jetpack-compose-unleashed-the-power-of-agsl-shaders-in-android-de502f882e35
 *
 * @param time Current animation time. This determines the progress of the distortion
 * @param modifier Modifier to be applied to the component
 * @param content The content to be displayed inside the distorted box
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun DistortionBoxImpl(
    time: Float,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val shader = remember { RuntimeShader(distortionShader) }

    Box(
        modifier
            .onSizeChanged { size ->
                shader.setFloatUniform(
                    "size",
                    size.width.toFloat(),
                    size.height.toFloat()
                )
            }
            .graphicsLayer {
                clip = true
                shader.setFloatUniform("time", time)
                renderEffect = RenderEffect
                    .createRuntimeShaderEffect(shader, "composable")
                    .asComposeRenderEffect()
            }
    ) {
        content()
    }
}

private const val distortionShader = """
// Shader Input
uniform float time; // Time
uniform float2 size; // View size
uniform shader composable; // Input texture

// Constants
const float speed = 10;
const float waveAmplitude = 6;
const float margin = 0.4;
const float waveFrequency = 0.02;

// Function to distort the coordinate based on wave deformations
float2 distortCoord(in float2 originalCoord) {
    // Normalize the coordinates to [-1;1], with 0 at the center
    float2 uv = originalCoord / size * 2 - 1;
    
    // Calculate smoothstep values for the x and y coordinates
    float edgeX = 1 - smoothstep(0.0, margin, abs(uv.x)) * smoothstep(1.0 - margin, 1.0, abs(uv.x));
    float edgeY = 1 - smoothstep(0.0, margin, abs(uv.y)) * smoothstep(1.0 - margin, 1.0, abs(uv.y));
    
    // Combine the smoothstep values to create a smooth margin
    float edge = min(edgeX, edgeY);
    
    // Calculate the wave distortion offset based on the length of the distorted coordinate
    // switch the sign of +- time * speed to change direction
    float waveOffset = sin(length(originalCoord) * waveFrequency - time * speed);
    
    // Apply the wave distortion to the fragment coordinate
    return originalCoord + (waveOffset * waveAmplitude * edge);
}

float4 main(in float2 fragCoord) {
    float2 distortedCoord = distortCoord(fragCoord);
    return composable.eval(distortedCoord);
}
"""