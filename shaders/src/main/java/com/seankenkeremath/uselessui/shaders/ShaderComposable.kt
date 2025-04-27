package com.seankenkeremath.uselessui.shaders

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

val runtimeShader = """
uniform shader image;
uniform float2 resolution;
uniform float radius;
uniform float time;

half4 main(float2 fragCoord) {
    vec2 uv = fragCoord/resolution.xy;
    uv = uv * 2.0 - 1.0;  // Center the coordinates
    uv.x *= resolution.x/resolution.y;  // Correct aspect ratio
    
    // Create multiple pulsating circles
    float dist = length(uv);
    
    // Main circle with dramatic pulsing
    float circle1 = smoothstep(0.5 + 0.3 * sin(time * 3.0), 0.0, dist);
    
    // Outer ring that changes thickness
    float ring1 = smoothstep(0.8 + 0.2 * sin(time * 2.0), 0.7 + 0.15 * cos(time * 4.0), dist);
    
    // Create vibrant, shifting colors
    vec3 color1 = vec3(0.8 + 0.2 * sin(time), 0.2 * cos(time * 2.0), 0.7 + 0.3 * sin(time * 3.0));
    vec3 color2 = vec3(0.2 * sin(time * 2.0), 0.8 + 0.2 * cos(time), 0.3 + 0.2 * sin(time * 4.0));
    
    // Combine effects
    vec3 finalColor = mix(color1 * circle1, color2 * ring1, 0.5 + 0.5 * sin(time * 5.0));
    
    // Add some extra visual interest with moving lines
    float lines = 0.5 + 0.5 * sin(uv.y * 20.0 + time * 10.0) * sin(uv.x * 20.0 - time * 8.0);
    finalColor += vec3(lines * 0.1);
    
    return half4(finalColor, 1.0);
}
""".trimIndent()

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun ShaderComposable() {
    val shader = remember { RuntimeShader(runtimeShader) }

    // Speed up the animation for more obvious effect
    val infiniteTransition = rememberInfiniteTransition(label = "shader_animation")
    val animatedTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,  // Larger range for more variation
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shader_time"
    )



    LaunchedEffect(animatedTime) {
        shader.setFloatUniform("time", animatedTime)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                shader.setFloatUniform("resolution", size.width, size.height)
                onDrawBehind {
                    drawRect(ShaderBrush(shader))
                }
            }
    )
}

@Preview(showBackground = true)
@Composable
fun ShaderComposablePreview() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ShaderComposable()
    } else {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.Red))
    }
}

// We only need the distortion part
// TODO: move this to the waves module?
const val distortionShader = """
    // Shader Input
uniform float progress; // Animation progress
uniform float time; // Time
uniform float2 size; // View size
uniform shader composable; // Input texture

// Constants
const float speed = 10;
const float frequency = 8;
const float sharpness = 0.99;
const float depth = 0.2;

// Target animation variables
const float targetWaveAmplitude = 6;
const float targetYStretching = 4.5;

// Animation variables
float waveAmplitude = 0;
float yStretching = 0;

// Distortion Constants
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
    float waveOffset = sin(length(originalCoord) * waveFrequency + time * speed);
    
    // Apply the wave distortion to the fragment coordinate
    return originalCoord + (waveOffset * waveAmplitude * edge);
}

float4 main(in float2 fragCoord) {
    // Update animation variables based on the progress
    waveAmplitude = targetWaveAmplitude * progress;
    yStretching = targetYStretching * progress;

    // Evaluate the Composable shader at the distorted coordinate
    float2 distortedCoord = distortCoord(fragCoord);
    return composable.eval(distortedCoord);
}
"""

// TODO keep this (its in tact)?
const val wavyShader = """
    // Shader Input
uniform float progress; // Animation progress
uniform float time; // Time
uniform float2 size; // View size
uniform shader composable; // Input texture

// Constants
const float speed = 10;
const float frequency = 8;
const float sharpness = 0.99;
const float depth = 0.2;

// Target animation variables
const float targetWaveAmplitude = 6;
const float targetYStretching = 4.5;

// Animation variables
float waveAmplitude = 0;
float yStretching = 0;

// Distortion Constants
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
    float waveOffset = sin(length(originalCoord) * waveFrequency + time * speed);
    
    // Apply the wave distortion to the fragment coordinate
    return originalCoord + (waveOffset * waveAmplitude * edge);
}

float4 main(in float2 fragCoord) {
    // Update animation variables based on the progress
    waveAmplitude = targetWaveAmplitude * progress;
    yStretching = targetYStretching * progress;

    // Evaluate the Composable shader at the distorted coordinate
    float2 distortedCoord = distortCoord(fragCoord);
    float4 baseColor = composable.eval(distortedCoord);

    // Normalize the coords
    float2 uv = fragCoord / size;

    // Center and stretch the UV coordinates
    uv -= 0.5;
    uv *= float2(2, yStretching);

    // Calculate y-coordinate
    float y = sqrt(1 - uv.x * uv.x * uv.x * uv.x);

    // Add dynamic offset based on time
    float offset = sin(frequency * uv.x + time * speed) * depth;

    // Calculate upper and lower y-coordinates with offset
    float upperY = y + offset;
    float lowerY = -y + offset;

    // Calculate edge and mid values for smoothstep operation
    float edge = abs(upperY - lowerY);
    float mid = (upperY + lowerY) / 2;

    // Apply smoothstep to create the final color
    return baseColor * smoothstep(edge, edge * sharpness, abs(uv.y - mid));
}
"""

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun Modifier.onPressWavy(
    interactionSource: InteractionSource,
    releaseDelay: Long = 500,
    spec: AnimationSpec<Float> = tween(durationMillis = 400),
    confirmWaving: () -> Boolean = { true }
) = composed {
    // Initialize a runtime shader with the WavyShader and state variables
    val shader = remember { RuntimeShader(distortionShader) }
    var time by remember { mutableStateOf(0f) }
    var playAnimation by remember { mutableStateOf(false) }

    // Collect the state of whether the composable is currently pressed
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animate the progress of the enter/exit animation
    val animationProgress by animateFloatAsState(
        targetValue = if (playAnimation) 1f else 0f,
        animationSpec = spec,
        label = "Wavy Animation Progress"
    )

    // Coroutine to simulate frame updates for the wavy animation
    LaunchedEffect(playAnimation) {
        while (playAnimation) {
            delay(16) // Delay to simulate frame rate, adjust as needed
            time += 0.016f // Increase time by 0.016 seconds (60 FPS simulation)
        }
    }

    // Coroutine to handle user interaction and start/stop the animation
    LaunchedEffect(isPressed) {
        if (isPressed && confirmWaving()) {
            playAnimation = true
        } else if (!isPressed && playAnimation) {
            delay(releaseDelay)
            playAnimation = false
        }
    }

    this
        // Set the shader's uniform values based on the composable's size
        .onSizeChanged { size ->
            shader.setFloatUniform(
                "size",
                size.width.toFloat(),
                size.height.toFloat()
            )
        }
        // Apply graphics layer with clipping and runtime shader effect
        .graphicsLayer {
            clip = true

            // Set shader parameters for time and animation progress
            shader.setFloatUniform("time", time)
            shader.setFloatUniform("progress", animationProgress)

            // Apply the runtime shader
            renderEffect = RenderEffect
                .createRuntimeShaderEffect(shader, "composable")
                .asComposeRenderEffect()
        }
}