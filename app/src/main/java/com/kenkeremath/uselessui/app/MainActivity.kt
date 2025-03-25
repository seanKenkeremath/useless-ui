package com.kenkeremath.uselessui.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kenkeremath.uselessui.app.theme.UselessUITheme
import com.kenkeremath.uselessui.app.ui.screens.shatter.ShatterPagerDemoScreen
import com.kenkeremath.uselessui.app.ui.screens.shatter.ShatterableLayoutDemoScreen

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

            Screen.ShatterableLayoutDemo -> ShatterableLayoutDemoScreen(
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

        HorizontalDivider()

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
