package com.machiav3lli.backup.ui.compose.navigation

import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.machiav3lli.backup.NAV_MAIN
import com.machiav3lli.backup.NAV_PREFS

@Composable
fun BottomNavBar(page: Int = NAV_MAIN, navController: NavController) {
    val items = when (page) {
        NAV_PREFS -> listOf(
            BottomNavItem.UserPrefs,
            BottomNavItem.ServicePrefs,
            BottomNavItem.AdvancedPrefs,
            BottomNavItem.ToolsPrefs
        )
        else -> listOf(
            BottomNavItem.Home,
            BottomNavItem.Backup,
            BottomNavItem.Restore,
            BottomNavItem.Scheduler,
            BottomNavItem.Settings
        )
    }

    NavigationBar(
        modifier = Modifier.height(72.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination?.route
        items.forEach { item ->
            val selected = currentDestination == item.destination

            NavigationBarItem(
                icon = {
                    Icon(
                        painterResource(id = item.icon),
                        contentDescription = stringResource(id = item.title),
                    )
                },
                label = {
                    Text(
                        text = stringResource(id = item.title),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                alwaysShowLabel = false,
                selected = selected,
                onClick = {
                    navController.navigate(item.destination) {
                        navController.graph.startDestinationRoute?.let { destination ->
                            popUpTo(destination) { saveState = true }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}