package com.kenkeremath.uselessui.app.ui.screens.waves

import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.draw.alpha
import com.kenkeremath.uselessui.waves.DistortionBox

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
        DemoSection(title = "WavyLine") {
            Column {
                Text("Basic Wavy Line")
                Spacer(modifier = Modifier.height(8.dp))
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
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(vertical = 8.dp)
                ) {
                    WavyLine(
                        modifier = Modifier.fillMaxSize(),
                        centerWave = true,
                        crestHeight = 4.dp,
                        waveLength = 30.dp,
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
                Spacer(modifier = Modifier.height(8.dp))
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
                            crestHeight = 6.dp,
                            waveLength = 60.dp
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

        // DistortionBox Section
        DemoSection(title = "DistortionBox") {
            Column {
                Text("Shader-based Wave Distortion")
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 8.dp)
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        DistortionBox(
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(0.5f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Text(
                                    "Distorted Content",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                            ) {
                                Text(
                                    text = "RuntimeShader Not Supported",
                                    style = MaterialTheme.typography.titleLarge,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "This effect requires Android 13 (API 33) or higher",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Wavy Box Section
        DemoSection(title = "WavyBox") {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Horizontal Waves Only (Top & Bottom)")
                Spacer(modifier = Modifier.height(8.dp))
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
                Spacer(modifier = Modifier.height(8.dp))
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
                Spacer(modifier = Modifier.height(8.dp))
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
                            crestHeight = 4.dp,
                            waveLength = 30.dp
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
                Spacer(modifier = Modifier.height(8.dp))
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
                            crestHeight = 6.dp,
                            waveLength = 40.dp
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
                Spacer(modifier = Modifier.height(8.dp))
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
                            crestHeight = 6.dp,
                            waveLength = 40.dp
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
                Spacer(modifier = Modifier.height(8.dp))
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

                Spacer(modifier = Modifier.height(16.dp))

                Text("Animated Crest Height")
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .padding(vertical = 8.dp)
                ) {
                    val infiniteTransition = rememberInfiniteTransition()
                    val animatedCrestHeight by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 15f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                    
                    WavyBox(
                        spec = WavyBoxSpec(
                            topWavy = true,
                            rightWavy = false,
                            bottomWavy = true,
                            leftWavy = false,
                            crestHeight = animatedCrestHeight.dp,
                            waveLength = 40.dp
                        ),
                        style = WavyBoxStyle.FilledWithColor(
                            strokeWidth = 2.dp,
                            strokeColor = MaterialTheme.colorScheme.primary,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Text(
                            "Animated\nCrest Height",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
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