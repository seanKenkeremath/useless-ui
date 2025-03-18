package com.seankenkeremath.uselessui.exampleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.seankenkeremath.uselessui.R
import com.seankenkeremath.uselessui.UselessButton
import com.seankenkeremath.uselessui.exampleapp.theme.UselessUITheme
import com.seankenkeremath.uselessui.shatter.ShatterableLayout

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
    val context = LocalContext.current
    val clickCount = remember { mutableStateOf(0) }
    val isShattered = remember { mutableStateOf(false) }

    val shatterBitmap = remember {
        ContextCompat.getDrawable(context, R.drawable.ic_launcher_background)?.toBitmap()?.asImageBitmap()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Library Demo")
        Text(text = "Button clicked ${clickCount.value} times")

        UselessButton(
            text = "Click Me!",
            onClick = { clickCount.value++ }
        )

        Spacer(modifier = Modifier.height(16.dp))
        
        Text(text = "Tap the image to shatter, tap again to reverse")
        
        Spacer(modifier = Modifier.height(8.dp))
        
        UselessButton(
            text = if (isShattered.value) "Reverse Shatter" else "Shatter",
            onClick = { isShattered.value = !isShattered.value }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ShatterableLayout(
            isShattered = isShattered.value,
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Shatter Me!",
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