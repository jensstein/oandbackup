package com.machiav3lli.backup.ui.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.AndroidViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.activity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.machiav3lli.backup.activities.PrefsActivityX
import com.machiav3lli.backup.pages.MainPage
import com.machiav3lli.backup.pages.PermissionsPage
import com.machiav3lli.backup.pages.WelcomePage
import com.machiav3lli.backup.preferences.ExportsPage
import com.machiav3lli.backup.preferences.LogsPage
import com.machiav3lli.backup.preferences.TerminalPage
import com.machiav3lli.backup.preferences.persist_beenWelcomed
import com.machiav3lli.backup.viewmodels.ExportsViewModel
import com.machiav3lli.backup.viewmodels.LogViewModel

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
        slideDownComposable(NavItem.Welcome.destination) {
            WelcomePage()
        }
        composable(route = NavItem.Permissions.destination) {
            PermissionsPage()
        }
        slideUpComposable(NavItem.Main.destination) {
            MainPage(
                navController = navController
            )
        }
        activity(NavItem.Settings.destination) {
            this.activityClass = PrefsActivityX::class
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PrefsNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    pagerState: PagerState,
    pages: List<NavItem>,
    viewModels: List<AndroidViewModel>,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = NavItem.Settings.destination,
    ) {
        slideDownComposable(NavItem.Settings.destination) {
            SlidePager(
                pageItems = pages,
                pagerState = pagerState,
                navController = navController,
            )
        }
        slideUpComposable(NavItem.Exports.destination) {
            val viewModel = viewModels.find { it is ExportsViewModel } as ExportsViewModel
            ExportsPage(viewModel)
        }
        slideUpComposable(NavItem.Logs.destination) {
            val viewModel = viewModels.find { it is LogViewModel } as LogViewModel
            LogsPage(viewModel)
        }
        slideUpComposable(NavItem.Terminal.destination) {
            TerminalPage()
        }
    }
}

fun NavGraphBuilder.slideUpComposable(
    route: String,
    composable: @Composable (AnimatedVisibilityScope.(NavBackStackEntry) -> Unit),
) {
    composable(
        route,
        enterTransition = { slideInVertically { height -> height } + fadeIn() },
        exitTransition = { slideOutVertically { height -> -height } + fadeOut() }
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
