package com.machiav3lli.backup.ui.navigation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.machiav3lli.backup.NAV_MAIN
import com.machiav3lli.backup.NAV_PREFS

@Composable
fun BottomNavBar(page: Int = NAV_MAIN, navController: NavController) {
    val items = when (page) {
        NAV_PREFS -> listOf(
            NavItem.UserPrefs,
            NavItem.ServicePrefs,
            NavItem.AdvancedPrefs,
            NavItem.ToolsPrefs,
        )

        else      -> listOf(
            NavItem.Home,
            NavItem.Backup,
            NavItem.Restore,
            NavItem.Scheduler,
        )
    }

    NavigationBar(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination?.route

        items.forEach { item ->
            val selected = currentDestination == item.destination

            NavBarItem(
                selected = selected,
                icon = item.icon,
                labelId = item.title,
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