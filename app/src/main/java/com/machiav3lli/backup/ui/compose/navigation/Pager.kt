package com.machiav3lli.backup.ui.compose.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.machiav3lli.backup.preferences.pref_squeezeNavText
import com.machiav3lli.backup.ui.compose.item.ResponsiveText
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun SlidePager(
    modifier: Modifier = Modifier,
    pageItems: List<NavItem>,
    pagerState: PagerState,
    navController: NavHostController
) {
    HorizontalPager(modifier = modifier, state = pagerState, count = pageItems.size) { page ->
        pageItems[page].ComposablePage(
            navController = navController,
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun PagerNavBar(pageItems: List<NavItem>, pagerState: PagerState) {
    val scope = rememberCoroutineScope()

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        pageItems.forEachIndexed { index, tab ->
            val selected = pagerState.currentPage == index

            NavigationBarItem(
                selected = selected,
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = stringResource(id = tab.title),
                        modifier = Modifier
                            .background(
                                if (selected) MaterialTheme.colorScheme
                                    .surfaceColorAtElevation(48.dp)
                                else Color.Transparent,
                                CircleShape
                            )
                            .padding(8.dp)
                            .size(if (selected) 36.dp else 26.dp),
                    )
                },
                label = {
                    if (!selected && pref_squeezeNavText.value)
                        ResponsiveText(
                            text = stringResource(id = tab.title),
                            maxLines = 1,
                            textStyle = MaterialTheme.typography.bodyMedium,
                        )
                    else if (!selected)
                        Text(
                            text = stringResource(id = tab.title),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.background,
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                ),
                onClick = {
                    scope.launch { pagerState.scrollToPage(index) }
                },
            )
        }
    }

}