package com.kenkeremath.uselessui.app.ui.screens.shatter

import ParameterSlider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastRoundToInt
import com.kenkeremath.uselessui.shatter.ShatterPager
import com.kenkeremath.uselessui.shatter.ShatterSpec

@Composable
fun ShatterPagerDemoScreen(modifier: Modifier = Modifier) {
    var showCenterPoints by remember { mutableStateOf(false) }
    var shardCount by remember { mutableIntStateOf(15) }
    var velocity by remember { mutableFloatStateOf(300f) }
    var rotationX by remember { mutableFloatStateOf(30f) }
    var rotationY by remember { mutableFloatStateOf(30f) }
    var rotationZ by remember { mutableFloatStateOf(30f) }
    var alphaTarget by remember { mutableFloatStateOf(0.3f) }
    var pageSpacing by remember { mutableFloatStateOf(8f) }

    val shatterSpec = remember(shardCount, velocity, rotationX, rotationY, rotationZ, alphaTarget) {
        ShatterSpec(
            shardCount = shardCount,
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
            text = "Swipe between pages to see the shatter effect",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )

        val pagerState = rememberPagerState { 5 }

        ShatterPager(
            state = pagerState,
            shatterSpec = shatterSpec,
            showCenterPoints = showCenterPoints,
            contentPadding = PaddingValues(horizontal = 64.dp),
            beyondViewportPageCount = 3,
            pageSpacing = pageSpacing.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) { page ->
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(getColorForPage(page)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Page ${page + 1}",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SwitchSetting(
                label = "Show Center Points",
                checked = showCenterPoints,
                onCheckedChange = { showCenterPoints = it }
            )

            ParameterSlider(
                label = "Page Spacing",
                value = pageSpacing,
                onValueChange = { pageSpacing = it },
                valueRange = 0f..32f
            )

            ParameterSlider(
                label = "Number of shards",
                value = shardCount.toFloat(),
                onValueChange = { shardCount = it.toInt() },
                valueRange = 5f..100f,
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
                onClick = {
                    shardCount = 15
                    velocity = 300f
                    rotationX = 30f
                    rotationY = 30f
                    rotationZ = 30f
                    alphaTarget = 0.3f
                    pageSpacing = 8f
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp)
            ) {
                Text("Reset Parameters")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SwitchSetting(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.align(Alignment.CenterStart)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

private fun getColorForPage(page: Int): Color {
    return when (page % 5) {
        0 -> Color(0xFF1976D2)
        1 -> Color(0xFF388E3C)
        2 -> Color(0xFFD32F2F)
        3 -> Color(0xFF7B1FA2)
        else -> Color(0xFFFF9800)
    }
} 