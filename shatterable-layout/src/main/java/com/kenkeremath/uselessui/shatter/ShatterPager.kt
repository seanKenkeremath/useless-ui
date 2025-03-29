package com.kenkeremath.uselessui.shatter

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.TargetedFlingBehavior
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.absoluteValue

/**
 * A pager component that applies a shatter effect during page transitions with a provided PagerState.
 *
 *  * This component uses the ShatterableLayout to create a shatter animation effect
 *  * as the user swipes between pages. Each page has its own shatter effect, with
 *  * offscreen pages starting shattered and becoming intact as they come into view.
 *
 * @param state The state object that controls the pager
 * @param modifier Modifier to be applied to the pager
 * @param shatterSpec Configuration for the shatter animation properties
 * @param captureMode Controls when the content bitmap is captured
 * @param showCenterPoints Whether to show debug points for the shatter centers * @param pageSpacing Spacing between pages
 * @param pageSpacing Spacing between pages
 * @param verticalAlignment The vertical alignment of the pages
 * @param contentPadding a padding around the whole content. This will add padding for the content after it has been clipped, which is not possible via modifier param. You can use it to add a padding before the first page or after the last one. Use pageSpacing to add spacing between the pages.
 * @param pageSize  Use this to change how the pages will look like inside this pager.
 * @param beyondViewportPageCount Pages to compose and layout before and after the list of visible pages. Note: Be aware that using a large value for beyondViewportPageCount will cause a lot of pages to be composed, measured and placed which will defeat the purpose of using lazy loading. This should be used as an optimization to pre-load a couple of pages before and after the visible ones. This does not include the pages automatically composed and laid out by the pre-fetcher in the direction of the scroll during scroll events.
 * @param flingBehavior The fling behavior to use for the pager
 * @param userScrollEnabled whether the scrolling via the user gestures or accessibility actions is allowed. You can still scroll programmatically using PagerState. scroll even when it is disabled.
 * @param reverseLayout Whether the layout should be reversed when scrolling
 * @param key a stable and unique key representing the item. When you specify the key the scroll position will be maintained based on the key, which means if you add/ remove items before the current visible item the item with the given key will be kept as the first visible one. If null is passed the position in the list will represent the key.
 * @param contentKey for a given page, this determines when the shattered bitmap should be invalidated. If this key changes, it is assumed the content rendered in the bitmap did as well and we need to capture again. By default, we just look at the page index
 * @param pageNestedScrollConnection The nested scroll connection to be used for the pages
 * @param snapPosition The calculation of how this Pager will perform snapping of pages. Use this to provide different settling to different positions in the layout. This is used by Pager as a way to calculate PagerState.
 * @param pageContent The content to display for each page
 */
@Composable
fun ShatterPager(
    state: PagerState,
    modifier: Modifier = Modifier,
    shatterSpec: ShatterSpec = ShatterSpec(),
    captureMode: CaptureMode = CaptureMode.LAZY,
    showCenterPoints: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    pageSize: PageSize = PageSize.Fill,
    beyondViewportPageCount: Int = PagerDefaults.BeyondViewportPageCount,
    pageSpacing: Dp = 0.dp,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    flingBehavior: TargetedFlingBehavior = PagerDefaults.flingBehavior(state = state),
    userScrollEnabled: Boolean = true,
    reverseLayout: Boolean = false,
    key: ((index: Int) -> Any)? = null,
    contentKey: ((index: Int) -> Any)? = null,
    pageNestedScrollConnection: NestedScrollConnection = PagerDefaults.pageNestedScrollConnection(
        state,
        Orientation.Horizontal
    ),
    snapPosition: SnapPosition = SnapPosition.Start,
    pageContent: @Composable PagerScope.(page: Int) -> Unit
) {
    HorizontalPager(
        state = state,
        modifier = modifier,
        contentPadding = contentPadding,
        pageSize = pageSize,
        beyondViewportPageCount = beyondViewportPageCount,
        pageSpacing = pageSpacing,
        verticalAlignment = verticalAlignment,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled,
        reverseLayout = reverseLayout,
        key = key,
        pageNestedScrollConnection = pageNestedScrollConnection,
        snapPosition = snapPosition
    ) { page ->
        val shatterProgress = state.getOffsetDistanceInPages(page).absoluteValue
            .coerceIn(0f, 1f)

        val progress =
            if (abs(shatterProgress) < .0001f) 0f else shatterProgress

        // Wrap each page in its own ShatterableLayout
        ShatterableLayout(
            progress = progress,
            captureMode = captureMode,
            shatterSpec = shatterSpec,
            contentKey = contentKey?.invoke(page) ?: page,
            showCenterPoints = showCenterPoints,
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                pageContent(page)
            }
        }
    }
} 