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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kenkeremath.uselessui.waves.WavyBox
import com.kenkeremath.uselessui.waves.WavyLine
import com.kenkeremath.uselessui.waves.WavyLoadingIndicator

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

        // Wavy Loading Indicator Section
        DemoSection(title = "Wavy Loading Indicator") {
            Column {
                Text("Basic Loading Indicator")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(vertical = 8.dp)
                ) {
                    WavyLoadingIndicator(
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Tall Loading Indicator")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(vertical = 8.dp)
                ) {
                    WavyLoadingIndicator(
                        modifier = Modifier.fillMaxSize(),
                        crestHeight = 20.dp
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Centered Loading Indicator")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(vertical = 8.dp)
                ) {
                    WavyLoadingIndicator(
                        modifier = Modifier.fillMaxSize(),
                        centerWave = true
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
                Text("Outlined Wavy Box")
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .padding(vertical = 8.dp)
                ) {
                    WavyBox(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.primary,
                        crestHeight = 6.dp
                    ) {
                        Text(
                            "Wavy Box Content",
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Filled Wavy Box")
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .padding(vertical = 8.dp)
                ) {
                    WavyBox(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.secondary,
                        filled = true,
                        crestHeight = 8.dp,
                        waveLength = 60.dp,
                        animationDurationMillis = 1500
                    ) {
                        Text(
                            "Filled Wavy Box",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Custom Wavy Box")
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .padding(vertical = 8.dp)
                ) {
                    WavyBox(
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFF6200EA),
                        filled = true,
                        fillColor = Color(0x336200EA),
                        crestHeight = 10.dp,
                        waveLength = 50.dp,
                        strokeWidth = 3.dp,
                        animationDurationMillis = 2000
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Custom",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Wavy Box",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
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