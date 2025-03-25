package com.kenkeremath.uselessui.app

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.runtime.mutableIntStateOf
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
import com.kenkeremath.uselessui.shatter.ShatterState
import com.kenkeremath.uselessui.shatter.ShatterableLayout
import java.math.RoundingMode
import java.text.DecimalFormat

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
    var shatterState by remember { mutableStateOf(ShatterState.Intact) }
    var showCenterPoints by remember { mutableStateOf(false) }

    val defaultDuration = 1000L
    val defaultShardCount = 15
    val defaultVelocity = 300f
    val defaultRotationX = 30f
    val defaultRotationY = 30f
    val defaultRotationZ = 30f
    val defaultAlphaTarget = 0.3f

    var durationMillis by remember { mutableLongStateOf(defaultDuration) }
    var shardCount by remember { mutableIntStateOf(defaultShardCount) }
    var velocity by remember { mutableFloatStateOf(defaultVelocity) }
    var rotationX by remember { mutableFloatStateOf(defaultRotationX) }
    var rotationY by remember { mutableFloatStateOf(defaultRotationY) }
    var rotationZ by remember { mutableFloatStateOf(defaultRotationZ) }
    var alphaTarget by remember { mutableFloatStateOf(defaultAlphaTarget) }

    fun resetParameters() {
        durationMillis = defaultDuration
        shardCount = defaultShardCount
        velocity = defaultVelocity
        rotationX = defaultRotationX
        rotationY = defaultRotationY
        rotationZ = defaultRotationZ
        alphaTarget = defaultAlphaTarget
    }

    val shatterSpec =
        remember(durationMillis, shardCount, velocity, rotationX, rotationY, rotationZ, alphaTarget) {
            ShatterSpec(
                durationMillis = durationMillis,
                shardCount = shardCount,
                easing = FastOutSlowInEasing,
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

        val interactionSource = remember { MutableInteractionSource() }
        var impactOffset by remember { mutableStateOf(Offset.Unspecified) }
        var animating by remember { mutableStateOf(false) }

        ShatterableLayout(
            captureMode = CaptureMode.LAZY,
            shatterState = shatterState,
            shatterCenter = impactOffset,
            shatterSpec = shatterSpec,
            showCenterPoints = showCenterPoints,
            onAnimationCompleted = {
                animating = false
            },
            modifier = Modifier
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull()
                            if (change != null
                                && !animating && change.pressed
                                && shatterState == ShatterState.Intact
                            ) {
                                impactOffset = change.position
                            }
                        }
                    }
                }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    val newState = when (shatterState) {
                        ShatterState.Intact -> ShatterState.Shattered
                        ShatterState.Shattered -> ShatterState.Intact
                        ShatterState.Reassembled -> ShatterState.Shattered
                    }
                    shatterState = newState
                    animating = true
                }
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tap to break!",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
            label = "Number of shards",
            value = shardCount.toFloat(),
            onValueChange = { shardCount = it.toInt() },
            valueRange = 0f..100f,
            showAsInt = true,
        )

        ParameterSlider(
            label = "Velocity",
            value = velocity,
            onValueChange = { velocity = it },
            valueRange = 0f..2000f
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

        OutlinedButton(
            onClick = { resetParameters() },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Reset Parameters")
        }

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
    modifier: Modifier = Modifier,
    showAsInt: Boolean = false,
) {

    val intFormat = remember {
        val df = DecimalFormat("#")
        df.roundingMode = RoundingMode.CEILING
        df
    }
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label)
            val formattedValue = if (!showAsInt) {
                String.format("%.1f", value)
            } else {
                intFormat.format(value)
            }
            Text(text = formattedValue)
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