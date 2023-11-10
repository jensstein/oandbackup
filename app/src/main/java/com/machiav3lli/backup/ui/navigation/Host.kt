package com.machiav3lli.backup.ui.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.pages.LockPage
import com.machiav3lli.backup.pages.MainPage
import com.machiav3lli.backup.pages.PermissionsPage
import com.machiav3lli.backup.pages.PrefsPage
import com.machiav3lli.backup.pages.WelcomePage
import com.machiav3lli.backup.preferences.ExportsPage
import com.machiav3lli.backup.preferences.LogsPage
import com.machiav3lli.backup.preferences.TerminalPage
import com.machiav3lli.backup.preferences.persist_beenWelcomed

@Composable
fun MainNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = if (persist_beenWelcomed.value) NavItem.Permissions.destination
        else NavItem.Welcome.destination
    ) {
        slideInComposable(NavItem.Lock.destination) {
            LockPage { OABX.main?.resumeMain() }
        }
        slideInComposable(NavItem.Welcome.destination) {
            WelcomePage()
        }
        slideInComposable(route = NavItem.Permissions.destination) {
            PermissionsPage()
        }
        slideInComposable(NavItem.Main.destination) {
            MainPage(
                navController = navController
            )
        }
        slideInComposable(NavItem.Settings.destination) {
            PrefsPage(
                navController = navController
            )
        }
        slideInComposable(NavItem.Exports.destination) {
            OABX.main?.exportsViewModel?.let { viewModel ->
                ExportsPage(viewModel)
            }
        }
        slideInComposable(NavItem.Logs.destination) {
            OABX.main?.logsViewModel?.let { viewModel ->
                LogsPage(viewModel)
            }
        }
        slideInComposable(NavItem.Terminal.destination) {
            TerminalPage()
        }
    }
}

fun NavGraphBuilder.slideInComposable(
    route: String,
    composable: @Composable (AnimatedVisibilityScope.(NavBackStackEntry) -> Unit),
) {
    composable(
        route,
        enterTransition = { slideInHorizontally { width -> width } },
        exitTransition = { slideOutHorizontally { width -> -width } },
        popEnterTransition = { slideInHorizontally { width -> -width } },
        popExitTransition = { slideOutHorizontally { width -> width } },
    ) {
        composable(it)
    }
}

fun NavGraphBuilder.slideDownComposable(
    route: String,
    composable: @Composable (AnimatedVisibilityScope.(NavBackStackEntry) -> Unit),
) {
    composable(
        route,
        enterTransition = { slideInVertically { height -> -height } + fadeIn() },
        exitTransition = { slideOutVertically { height -> height } + fadeOut() }
    ) {
        composable(it)
    }
}

fun NavGraphBuilder.fadeComposable(
    route: String,
    composable: @Composable (AnimatedVisibilityScope.(NavBackStackEntry) -> Unit),
) {
    composable(
        route,
        enterTransition = { fadeIn(initialAlpha = 0.1f) },
        exitTransition = { fadeOut() }
    ) {
        composable(it)
    }
}

fun NavHostController.clearBackStack() {
    while (this.currentBackStack.value.isNotEmpty()) {
        popBackStack()
    }
}

fun NavHostController.safeNavigate(route: String) {
    if (currentDestination?.route != route) {
        popBackStack()
        navigate(route)
    }
}
