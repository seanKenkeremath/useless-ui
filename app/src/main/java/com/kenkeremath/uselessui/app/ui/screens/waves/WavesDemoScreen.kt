package com.kenkeremath.uselessui.app.ui.screens.waves

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kenkeremath.uselessui.waves.WavyBox
import com.kenkeremath.uselessui.waves.WavyLine
import com.kenkeremath.uselessui.waves.WavyBoxSpec
import com.kenkeremath.uselessui.waves.WavyBoxStyle

@Composable
fun WavesDemoScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Wave Components",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Wavy Line Section
        DemoSection(title = "Wavy Line") {
            Column {
                Text("Basic Wavy Line")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(vertical = 8.dp)
                ) {
                    WavyLine(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Centered Wavy Line with Custom Parameters")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(vertical = 8.dp)
                ) {
                    WavyLine(
                        modifier = Modifier.fillMaxSize(),
                        centerWave = true,
                        crestHeight = 12.dp,
                        waveLength = 60.dp,
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.secondary,
                        animationDurationMillis = 1500
                    )
                }
            }
        }

        // Gradient Wave Effect Section
        DemoSection(title = "Gradient Wave Effect") {
            Column {
                Text("Gradient Wave")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(vertical = 8.dp)
                ) {
                    val gradientBrush = remember {
                        Brush.linearGradient(
                            colors = listOf(Color.Cyan, Color.Blue),
                            start = Offset(0f, Float.POSITIVE_INFINITY),
                            end = Offset(0f, 0f)
                        )
                    }
                    
                    WavyBox(
                        spec = WavyBoxSpec(
                            topWavy = true,
                            rightWavy = false,
                            bottomWavy = false,
                            leftWavy = false,
                            crestHeight = 12.dp,
                            waveLength = 80.dp
                        ),
                        style = WavyBoxStyle.FilledWithBrush(
                            brush = gradientBrush,
                            strokeWidth = 0.dp,
                            strokeColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Wavy Box Section
        DemoSection(title = "Wavy Box") {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Horizontal Waves Only (Top & Bottom)")
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .padding(vertical = 8.dp)
                ) {
                    WavyBox(
                        spec = WavyBoxSpec(
                            topWavy = true,
                            rightWavy = false,
                            bottomWavy = true,
                            leftWavy = false,
                            crestHeight = 6.dp,
                        ),
                        style = WavyBoxStyle.Outlined(
                            strokeWidth = 2.dp,
                            strokeColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Text(
                            "Horizontal\nWaves",
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Vertical Waves Only (Left & Right)")
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .padding(vertical = 8.dp)
                ) {
                    WavyBox(
                        spec = WavyBoxSpec(
                            topWavy = false,
                            rightWavy = true,
                            bottomWavy = false,
                            leftWavy = true,
                            crestHeight = 6.dp,
                        ),
                        style = WavyBoxStyle.FilledWithColor(
                            strokeWidth = 2.dp,
                            strokeColor = MaterialTheme.colorScheme.tertiary,
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Text(
                            "Vertical\nWaves",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("All Sides Wavy")
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .padding(vertical = 8.dp)
                ) {
                    WavyBox(
                        spec = WavyBoxSpec(
                            topWavy = true,
                            rightWavy = true,
                            bottomWavy = true,
                            leftWavy = true,
                            crestHeight = 8.dp,
                            waveLength = 60.dp
                        ),
                        style = WavyBoxStyle.FilledWithColor(
                            strokeWidth = 2.dp,
                            strokeColor = MaterialTheme.colorScheme.tertiary,
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        modifier = Modifier.fillMaxSize(),
                        animationDurationMillis = 1500
                    ) {
                        Text(
                            "All Sides\nWavy",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Alternating Sides (Top & Left)")
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .padding(vertical = 8.dp)
                ) {
                    WavyBox(
                        spec = WavyBoxSpec(
                            topWavy = true,
                            rightWavy = false,
                            bottomWavy = false,
                            leftWavy = true,
                            crestHeight = 10.dp,
                            waveLength = 50.dp
                        ),
                        style = WavyBoxStyle.FilledWithColor(
                            strokeWidth = 2.dp,
                            strokeColor = MaterialTheme.colorScheme.tertiary,
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                        ),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Text(
                            "Top & Left\nWavy",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Alternating Sides (Bottom & Right)")
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .padding(vertical = 8.dp)
                ) {
                    WavyBox(
                        spec = WavyBoxSpec(
                            topWavy = false,
                            rightWavy = true,
                            bottomWavy = true,
                            leftWavy = false,
                            crestHeight = 10.dp,
                            waveLength = 50.dp
                        ),
                        style = WavyBoxStyle.FilledWithColor(
                            strokeWidth = 2.dp,
                            strokeColor = MaterialTheme.colorScheme.tertiary,
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Text(
                            "Bottom & Right\nWavy",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("No Waves (Regular Box)")
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .padding(vertical = 8.dp)
                ) {
                    WavyBox(
                        spec = WavyBoxSpec(
                            topWavy = false,
                            rightWavy = false,
                            bottomWavy = false,
                            leftWavy = false,
                            crestHeight = 6.dp,
                        ),
                        style = WavyBoxStyle.FilledWithColor(
                            strokeWidth = 2.dp,
                            strokeColor = Color.Gray,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Text(
                            "Regular Box\n(No Waves)",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DemoSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WavesDemoScreenPreview() {
    Surface {
        WavesDemoScreen()
    }
} 