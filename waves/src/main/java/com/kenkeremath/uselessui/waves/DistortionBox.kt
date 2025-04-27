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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import kotlinx.coroutines.delay

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

    DistortionBox(
        time = time,
        modifier = modifier,
    ) {
        content()
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun DistortionBox(
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