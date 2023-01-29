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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.machiav3lli.backup.NAV_MAIN
import com.machiav3lli.backup.NAV_PREFS
import com.machiav3lli.backup.preferences.pref_squeezeNavText
import com.machiav3lli.backup.ui.compose.item.ResponsiveText

@Composable
fun BottomNavBar(page: Int = NAV_MAIN, navController: NavController) {
    val items = when (page) {
        NAV_PREFS -> listOf(
            NavItem.UserPrefs,
            NavItem.ServicePrefs,
            NavItem.AdvancedPrefs,
            NavItem.ToolsPrefs,
        )
        else -> listOf(
            NavItem.Home,
            NavItem.Backup,
            NavItem.Restore,
            NavItem.Scheduler,
        )
    }

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination?.route

        items.forEach { item ->
            val selected = currentDestination == item.destination

            NavigationBarItem(

                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = stringResource(id = item.title),
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
                            text = stringResource(id = item.title),
                            maxLines = 1,
                            textStyle = MaterialTheme.typography.bodyMedium,
                        )
                    else if (!selected)
                        Text(
                            text = stringResource(id = item.title),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.background,
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                ),
                alwaysShowLabel = true,
                selected = selected,
                onClick = {
                    navController.navigate(item.destination) {
                        navController.currentDestination?.id?.let {
                            popUpTo(it) {
                                inclusive = true
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}