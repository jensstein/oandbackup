package com.machiav3lli.backup.ui.compose.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.AndroidViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.activity
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.activities.PrefsActivityX
import com.machiav3lli.backup.pages.PermissionsPage
import com.machiav3lli.backup.pages.WelcomePage
import com.machiav3lli.backup.preferences.ExportsPage
import com.machiav3lli.backup.preferences.LogsPage
import com.machiav3lli.backup.preferences.TerminalPage
import com.machiav3lli.backup.preferences.persist_beenWelcomed
import com.machiav3lli.backup.viewmodels.ExportsViewModel
import com.machiav3lli.backup.viewmodels.LogViewModel

@OptIn(ExperimentalAnimationApi::class, ExperimentalPagerApi::class)
@Composable
fun MainNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    pagerState: PagerState,
    pages: List<NavItem>,
) {
    AnimatedNavHost(
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
            SlidePager(
                pagerState = pagerState,
                pageItems = pages,
                navController = navController
            )
        }
        activity(NavItem.Settings.destination) {
            this.activityClass = PrefsActivityX::class
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalPagerApi::class)
@Composable
fun PrefsNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    pagerState: PagerState,
    pages: List<NavItem>,
    viewModels: List<AndroidViewModel>,
) {
    AnimatedNavHost(
        modifier = modifier,
        navController = navController,
        startDestination = NavItem.Settings.destination
    ) {
        slideUpComposable(NavItem.Settings.destination) {
            SlidePager(
                pageItems = pages,
                pagerState = pagerState,
                navController = navController,
            )
        }
        slideDownComposable(NavItem.Exports.destination) {
            val viewModel = viewModels.find { it is ExportsViewModel } as ExportsViewModel
            ExportsPage(viewModel)
        }
        slideDownComposable(NavItem.Logs.destination) {
            val viewModel = viewModels.find { it is LogViewModel } as LogViewModel
            LogsPage(viewModel)
        }
        slideUpComposable(NavItem.Terminal.destination) {
            TerminalPage()
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun IntroNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    beenWelcomed: Boolean,
) {
    AnimatedNavHost(
        modifier = modifier,
        navController = navController,
        startDestination = if (beenWelcomed) NavItem.Permissions.destination
        else NavItem.Welcome.destination
    ) {
        slideDownComposable(NavItem.Welcome.destination) {
            WelcomePage()
        }
        slideUpComposable(route = NavItem.Permissions.destination) {
            PermissionsPage()
        }
        activity(NavItem.Main.destination) {
            this.activityClass = MainActivityX::class
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
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

@OptIn(ExperimentalAnimationApi::class)
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

@OptIn(ExperimentalAnimationApi::class)
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


