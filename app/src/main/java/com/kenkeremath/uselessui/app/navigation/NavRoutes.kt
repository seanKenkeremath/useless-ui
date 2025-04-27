package com.kenkeremath.uselessui.app.navigation

/**
 * Navigation routes for the app
 */
sealed class NavRoutes(val route: String) {
    object DemoList : NavRoutes("demo_list")
    object ShatterableLayoutDemo : NavRoutes("shatterable_layout_demo")
    object ShatterPagerDemo : NavRoutes("shatter_pager_demo")
    object WavesDemo : NavRoutes("waves_demo")
    object ShaderDemo : NavRoutes("shader_demo")
} 