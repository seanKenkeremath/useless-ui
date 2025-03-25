package com.kenkeremath.uselessui.shatter

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.absoluteValue

/**
 * A pager component that applies a shatter effect during page transitions.
 *
 * This component uses the ShatterableLayout to create a shatter animation effect
 * as the user swipes between pages. Each page has its own shatter effect, with
 * offscreen pages starting shattered and becoming intact as they come into view.
 *
 * @param pageCount The number of pages in the pager
 * @param modifier Modifier to be applied to the pager
 * @param initialPage The initial page to display
 * @param pageSpacing Spacing between pages
 * @param pageOffset How much of adjacent pages to show (0f-1f)
 * @param shatterSpec Configuration for the shatter animation properties
 * @param captureMode Controls when the content bitmap is captured
 * @param showCenterPoints Whether to show debug points for the shatter centers
 * @param pageContent The content to display for each page
 */
@Composable
fun ShatterPager(
    pageCount: Int,
    modifier: Modifier = Modifier,
    initialPage: Int = 0,
    pageSpacing: Dp = 8.dp,
    pageOffset: Float = 0.3f, // Show 30% of adjacent pages by default
    shatterSpec: ShatterSpec = ShatterSpec(durationMillis = 0), // Duration 0 as we control progress manually
    captureMode: CaptureMode = CaptureMode.LAZY,
    showCenterPoints: Boolean = false,
    pageContent: @Composable (page: Int) -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialPage) { pageCount }
    ShatterPager(
        pagerState = pagerState,
        modifier = modifier,
        pageSpacing = pageSpacing,
        pageOffset = pageOffset,
        shatterSpec = shatterSpec,
        captureMode = captureMode,
        showCenterPoints = showCenterPoints,
        pageContent = pageContent
    )
}

/**
 * A pager component that applies a shatter effect during page transitions with a provided PagerState.
 *
 * @param pagerState The state object that controls the pager
 * @param modifier Modifier to be applied to the pager
 * @param pageSpacing Spacing between pages
 * @param pageOffset How much of adjacent pages to show (0f-1f)
 * @param shatterSpec Configuration for the shatter animation properties
 * @param captureMode Controls when the content bitmap is captured
 * @param showCenterPoints Whether to show debug points for the shatter centers
 * @param pageContent The content to display for each page
 */
@Composable
fun ShatterPager(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    pageSpacing: Dp = 8.dp,
    pageOffset: Float = 0.3f, // Show 30% of adjacent pages by default
    shatterSpec: ShatterSpec = ShatterSpec(durationMillis = 0), // Duration 0 as we control progress manually
    captureMode: CaptureMode = CaptureMode.LAZY,
    showCenterPoints: Boolean = false,
    pageContent: @Composable (page: Int) -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val sidePadding = (screenWidthDp * pageOffset)

    HorizontalPager(
        state = pagerState,
        modifier = modifier,
        pageSpacing = pageSpacing,
        contentPadding = PaddingValues(horizontal = sidePadding)
    ) { page ->
        val shatterProgress = pagerState.getOffsetDistanceInPages(page).absoluteValue
            .coerceIn(0f, 1f)

        val delta = abs(shatterProgress)
        Log.d("PAGE", "page: $page, $shatterProgress, $delta ${delta < .01f}")
        val shatterState =
            if (abs(shatterProgress) < .0001f) ShatterState.Intact else ShatterState.Shattered

        // Wrap each page in its own ShatterableLayout
        ShatterableLayout(
            shatterState = shatterState,
            contentKey = page,
            captureMode = captureMode,
            shatterSpec = shatterSpec,
            showCenterPoints = showCenterPoints,
            // TODO: remove this temporary thing and create a second state hoisted constructor
            overrideProgress = if (shatterState == ShatterState.Intact) 0f else shatterProgress,
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp) // Add some padding around each page
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                pageContent(page)
            }
        }
    }
} 