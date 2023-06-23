package com.machiav3lli.backup.ui.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.machiav3lli.backup.preferences.pref_altNavBarItem
import com.machiav3lli.backup.preferences.pref_squeezeNavText
import com.machiav3lli.backup.ui.compose.ifThen
import com.machiav3lli.backup.ui.compose.item.ResponsiveText
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SlidePager(
    modifier: Modifier = Modifier,
    pageItems: List<NavItem>,
    pagerState: PagerState,
    navController: NavHostController,
) {
    HorizontalPager(modifier = modifier, state = pagerState) { page ->
        pageItems[page].ComposablePage(
            navController = navController,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PagerNavBar(pageItems: List<NavItem>, pagerState: PagerState) {
    val scope = rememberCoroutineScope()

    NavigationBar(
        modifier = Modifier.padding(horizontal = 8.dp),
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        pageItems.forEachIndexed { index, tab ->
            val selected = pagerState.currentPage == index

            if (pref_altNavBarItem.value) AltNavBarItem(
                selected = selected,
                icon = tab.icon,
                labelId = tab.title,
                onClick = {
                    scope.launch { pagerState.scrollToPage(index) }
                },
            ) else NavBarItem(
                modifier = Modifier.weight(if (selected) 2f else 1f),
                selected = selected,
                icon = tab.icon,
                labelId = tab.title,
                onClick = {
                    scope.launch { pagerState.scrollToPage(index) }
                },
            )
        }
    }
}

@Composable
fun RowScope.AltNavBarItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    labelId: Int,
    selected: Boolean,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .weight(1f),
        horizontalArrangement = Arrangement.Center,
    ) {
        Column(
            modifier = modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(id = labelId),
                modifier = Modifier
                    .background(
                        if (selected) MaterialTheme.colorScheme.surfaceColorAtElevation(12.dp)
                        else Color.Transparent,
                        CircleShape
                    )
                    .padding(8.dp)
                    .size(if (selected) 32.dp else 24.dp),
                tint = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
            if (!selected && pref_squeezeNavText.value)
                ResponsiveText(
                    text = stringResource(id = labelId),
                    maxLines = 1,
                    textStyle = MaterialTheme.typography.bodyMedium,
                )
            else if (!selected)
                Text(
                    text = stringResource(id = labelId),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
        }
    }
}

@Composable
fun RowScope.NavBarItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    labelId: Int,
    selected: Boolean,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .padding(vertical = 8.dp)
            .clickable { onClick() }
            .ifThen(selected) {
                background(
                    MaterialTheme.colorScheme.surfaceColorAtElevation(12.dp),
                    MaterialTheme.shapes.extraLarge
                )
            }
            .padding(8.dp)
            .weight(1f),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(id = labelId),
            modifier = Modifier.size(24.dp),
            tint = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
        if (pref_squeezeNavText.value && selected) ResponsiveText(
            text = stringResource(id = labelId),
            maxLines = 1,
            textStyle = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        else if (selected) Text(
            text = stringResource(id = labelId),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}