package com.kenkeremath.uselessui.app

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.kenkeremath.uselessui.shatter.ShatterableLayout
import java.math.RoundingMode
import java.text.DecimalFormat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UselessUITheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.DemoList) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = currentScreen.title) },
                navigationIcon = {
                    if (currentScreen != Screen.DemoList) {
                        IconButton(onClick = { currentScreen = Screen.DemoList }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        when (currentScreen) {
            Screen.DemoList -> DemoListScreen(
                onDemoSelected = { screen -> currentScreen = screen },
                modifier = Modifier.padding(innerPadding)
            )

            Screen.ShatterableLayoutDemo -> ShatterDemoScreen(
                modifier = Modifier.padding(innerPadding)
            )

            Screen.ShatterPagerDemo -> ShatterPagerDemoScreen(
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun DemoListScreen(
    onDemoSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Select a Demo",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        ListItem(
            headlineContent = { Text("Shatterable Layout Demo") },
            supportingContent = { Text("Tap to break content into pieces") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDemoSelected(Screen.ShatterableLayoutDemo) }
        )

        Divider()

        ListItem(
            headlineContent = { Text("Shatter Pager Demo") },
            supportingContent = { Text("Swipe between pages with shatter effect") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDemoSelected(Screen.ShatterPagerDemo) }
        )
    }
}

sealed class Screen(val title: String) {
    object DemoList : Screen("Useless UI Demos")
    object ShatterableLayoutDemo : Screen("Shatterable Layout Demo")
    object ShatterPagerDemo : Screen("Shatter Pager Demo")
}

@Composable
fun ShatterDemoScreen(modifier: Modifier = Modifier) {
    var targetProgress by remember { mutableFloatStateOf(0f) }
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
        remember(
            durationMillis,
            shardCount,
            velocity,
            rotationX,
            rotationY,
            rotationZ,
            alphaTarget
        ) {
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
        var shattering by remember { mutableStateOf(false) }
        val progress by animateFloatAsState(
            targetValue = targetProgress,
            animationSpec = tween(
                durationMillis = shatterSpec.durationMillis.toInt(),
                easing = shatterSpec.easing
            ),
            label = "shatter",
            finishedListener = { _ ->
                animating = false
            }
        )

        ShatterableLayout(
            captureMode = CaptureMode.LAZY,
            progress = progress,
            shatterCenter = impactOffset,
            shatterSpec = shatterSpec,
            showCenterPoints = showCenterPoints,
            modifier = Modifier
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull()
                            if (change != null
                                && !animating && change.pressed
                                && progress == 0f
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
                    shattering = !shattering
                    targetProgress = if (shattering) 1f else 0f
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
internal fun ParameterSlider(
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