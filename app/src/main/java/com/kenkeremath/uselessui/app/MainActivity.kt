package com.kenkeremath.uselessui.app

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kenkeremath.uselessui.app.theme.UselessUITheme
import com.kenkeremath.uselessui.shatter.CaptureMode
import com.kenkeremath.uselessui.shatter.ShatterSpec
import com.kenkeremath.uselessui.shatter.ShatterableLayout
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UselessUITheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ShatterDemoScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun ShatterDemoScreen(modifier: Modifier = Modifier) {
    var isShattered by remember { mutableStateOf(false) }
    var showCenterPoints by remember { mutableStateOf(false) }

    val defaultDuration = 1000L
    val defaultVelocity = 300f
    val defaultRotationX = 30f
    val defaultRotationY = 30f
    val defaultRotationZ = 30f
    val defaultAlphaTarget = 0.3f

    var durationMillis by remember { mutableLongStateOf(defaultDuration) }
    var velocity by remember { mutableFloatStateOf(defaultVelocity) }
    var rotationX by remember { mutableFloatStateOf(defaultRotationX) }
    var rotationY by remember { mutableFloatStateOf(defaultRotationY) }
    var rotationZ by remember { mutableFloatStateOf(defaultRotationZ) }
    var alphaTarget by remember { mutableFloatStateOf(defaultAlphaTarget) }

    fun resetParameters() {
        durationMillis = defaultDuration
        velocity = defaultVelocity
        rotationX = defaultRotationX
        rotationY = defaultRotationY
        rotationZ = defaultRotationZ
        alphaTarget = defaultAlphaTarget
    }

    val shatterSpec = remember(durationMillis, velocity, rotationX, rotationY, rotationZ, alphaTarget) {
        ShatterSpec(
            durationMillis = durationMillis,
            velocity = velocity,
            rotationXTarget = rotationX,
            rotationYTarget = rotationY,
            rotationZTarget = rotationZ,
            alphaTarget = alphaTarget
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Tap the image to shatter, tap again to reverse",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )

        val repeatedValue by rememberInfiniteTransition().animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1200,
                )
            )
        )

        val text = "Shatter Me!"
        val displayedText = text.substring(0, (text.length * repeatedValue).roundToInt())
        val interactionSource = remember { MutableInteractionSource() }
        var impactOffset by remember { mutableStateOf(Offset.Unspecified) }

        ShatterableLayout(
            captureMode = CaptureMode.LAZY,
            isShattered = isShattered,
            continueWhenReassembled = true,
            shatterCenter = impactOffset,
            shatterSpec = shatterSpec,
            showCenterPoints = showCenterPoints,
            modifier = Modifier
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull()
                            if (change != null && change.pressed) {
                                impactOffset = change.position
                            }
                        }
                    }
                }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    isShattered = !isShattered
                }
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayedText,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add toggle for center points visualization
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Show Center Points",
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = showCenterPoints,
                onCheckedChange = { showCenterPoints = it }
            )
        }

        Spacer(modifier = Modifier.padding(vertical = 8.dp))

        ParameterSlider(
            label = "Duration",
            value = durationMillis.toFloat(),
            onValueChange = { durationMillis = it.toLong() },
            valueRange = 0f..5000f
        )

        ParameterSlider(
            label = "Velocity",
            value = velocity,
            onValueChange = { velocity = it },
            valueRange = 0f..600f
        )

        ParameterSlider(
            label = "Rotation X",
            value = rotationX,
            onValueChange = { rotationX = it },
            valueRange = -180f..180f
        )

        ParameterSlider(
            label = "Rotation Y",
            value = rotationY,
            onValueChange = { rotationY = it },
            valueRange = -180f..180f
        )

        ParameterSlider(
            label = "Rotation Z",
            value = rotationZ,
            onValueChange = { rotationZ = it },
            valueRange = -180f..180f
        )

        ParameterSlider(
            label = "Alpha",
            value = alphaTarget,
            onValueChange = { alphaTarget = it },
            valueRange = 0f..1f
        )

        // Add reset button
        OutlinedButton(
            onClick = { resetParameters() },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Reset Parameters")
        }

        // Add some space at the bottom for better scrolling experience
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun ParameterSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label)
            Text(text = String.format("%.1f", value))
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DemoScreenPreview() {
    UselessUITheme {
        ShatterDemoScreen()
    }
}