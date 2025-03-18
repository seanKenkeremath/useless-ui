package com.seankenkeremath.uselessui.exampleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.seankenkeremath.uselessui.UselessButton
import com.seankenkeremath.uselessui.exampleapp.theme.UselessUITheme
import com.seankenkeremath.uselessui.shatter.CaptureMode
import com.seankenkeremath.uselessui.shatter.ShatterableLayout
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UselessUITheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DemoScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun DemoScreen(modifier: Modifier = Modifier) {
    var clickCount by remember { mutableIntStateOf(0) }
    var isShattered by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Library Demo")
        Text(text = "Button clicked $clickCount times")

        UselessButton(
            text = "Click Me!",
            onClick = { clickCount++ }
        )

        Spacer(modifier = Modifier.height(16.dp))
        
        Text(text = "Tap the image to shatter, tap again to reverse")
        
        Spacer(modifier = Modifier.height(8.dp))
        
        UselessButton(
            text = if (isShattered) "Reverse Shatter" else "Shatter",
            onClick = { isShattered = !isShattered }
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        ShatterableLayout(
            captureMode = CaptureMode.LAZY,
            isShattered = isShattered,
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
    }
}

@Preview(showBackground = true)
@Composable
fun DemoScreenPreview() {
    UselessUITheme {
        DemoScreen()
    }
}