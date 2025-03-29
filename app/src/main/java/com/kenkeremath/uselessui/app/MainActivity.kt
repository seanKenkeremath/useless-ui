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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kenkeremath.uselessui.app.navigation.NavRoutes
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
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = getScreenTitle(currentRoute)) },
                navigationIcon = {
                    if (currentRoute != NavRoutes.DemoList.route) {
                        IconButton(onClick = { navController.navigateUp() }) {
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
        NavHost(
            navController = navController,
            startDestination = NavRoutes.DemoList.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavRoutes.DemoList.route) {
                DemoListScreen(
                    onDemoSelected = { route -> navController.navigate(route) }
                )
            }
            composable(NavRoutes.ShatterableLayoutDemo.route) {
                ShatterableLayoutDemoScreen()
            }
            composable(NavRoutes.ShatterPagerDemo.route) {
                ShatterPagerDemoScreen()
            }
        }
    }
}

@Composable
fun DemoListScreen(
    onDemoSelected: (String) -> Unit,
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
            headlineContent = { Text("ShatterableLayout Demo") },
            supportingContent = { Text("Tap to break content into pieces") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDemoSelected(NavRoutes.ShatterableLayoutDemo.route) }
        )

        HorizontalDivider()

        ListItem(
            headlineContent = { Text("ShatterPager Demo") },
            supportingContent = { Text("Swipe between pages with shatter effect") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDemoSelected(NavRoutes.ShatterPagerDemo.route) }
        )
    }
}

/**
 * Returns the screen title based on the current route
 */
private fun getScreenTitle(route: String?): String {
    return when (route) {
        NavRoutes.DemoList.route -> "Useless UI Demos"
        NavRoutes.ShatterableLayoutDemo.route -> "ShatterableLayout Demo"
        NavRoutes.ShatterPagerDemo.route -> "ShatterPager Demo"
        else -> "Useless UI Demos"
    }
}
