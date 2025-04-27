package com.kenkeremath.uselessui.app.ui.screens.shader

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kenkeremath.uselessui.waves.DistortionBox
import com.kenkeremath.uselessui.waves.WavyBox
import com.kenkeremath.uselessui.waves.WavyBoxSpec
import com.kenkeremath.uselessui.waves.WavyBoxStyle
import com.seankenkeremath.uselessui.shaders.ShaderComposable
import com.seankenkeremath.uselessui.shaders.onPressWavy

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun ShaderDemoScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        val wavyBoxSpec = remember {
            WavyBoxSpec(
                topWavy = true,
                rightWavy = false,
                leftWavy = false,
                bottomWavy = false,
                crestHeight = 4.dp,
                waveLength = 100.dp
            )
        }

        val wavyBoxStyle = remember {
            WavyBoxStyle.FilledWithColor(
                Color.Blue,
                strokeColor = Color.Blue,
                strokeWidth = 4.dp
            )
        }

        DistortionBox(
            modifier = Modifier
                .padding(top = 100.dp)
                .alpha(.5f)
                .fillMaxSize()
        ) {
            WavyBox(
                spec = wavyBoxSpec,
                style = wavyBoxStyle,
                Modifier
                    .fillMaxSize()

            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Blue),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Shader Demo",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    val interactionSource = remember { MutableInteractionSource() }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Button(
                            onClick = {},
                            interactionSource = interactionSource,
                            modifier = Modifier.onPressWavy(interactionSource)
                        ) {
                            Text(
                                text = "Tet button",
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val interactionSource2 = remember { MutableInteractionSource() }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = interactionSource2,
                                    indication = null,
                                    onClick = {})
                                .onPressWavy(interactionSource2)
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            ShaderComposable()
                        }
                    } else {
                        Text(
                            text = "This demo requires Android 13 (API level 33) or higher",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(32.dp)
                        )
                    }
                }
            }
        }
    }
} 