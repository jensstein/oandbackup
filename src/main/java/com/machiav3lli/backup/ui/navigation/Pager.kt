package com.machiav3lli.backup.ui.navigation

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList

@Composable
fun SlidePager(
    modifier: Modifier = Modifier,
    pageItems: ImmutableList<NavItem>,
    pagerState: PagerState,
) {
    HorizontalPager(modifier = modifier, state = pagerState) { page ->
        pageItems[page].content()
    }
}