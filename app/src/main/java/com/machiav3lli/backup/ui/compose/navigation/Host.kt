package com.machiav3lli.backup.ui.compose.navigation

import android.app.Application
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.activity
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.activities.PrefsActivityX
import com.machiav3lli.backup.dbs.ODatabase
import com.machiav3lli.backup.pages.PermissionsPage
import com.machiav3lli.backup.pages.WelcomePage
import com.machiav3lli.backup.pages.BatchPage
import com.machiav3lli.backup.pages.HomePage
import com.machiav3lli.backup.pages.SchedulerPage
import com.machiav3lli.backup.preferences.AdvancedPrefsPage
import com.machiav3lli.backup.preferences.ExportsPage
import com.machiav3lli.backup.preferences.LogsPage
import com.machiav3lli.backup.preferences.ServicePrefsPage
import com.machiav3lli.backup.preferences.ToolsPrefsPage
import com.machiav3lli.backup.preferences.UserPrefsPage
import com.machiav3lli.backup.viewmodels.BatchViewModel
import com.machiav3lli.backup.viewmodels.ExportsViewModel
import com.machiav3lli.backup.viewmodels.HomeViewModel
import com.machiav3lli.backup.viewmodels.LogViewModel
import com.machiav3lli.backup.viewmodels.SchedulerViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    application: Application
) =
    AnimatedNavHost(
        modifier = modifier,
        navController = navController,
        startDestination = NavItem.Home.destination
    ) {
        slideUpComposable(NavItem.Home.destination) {
            val viewModel = viewModel<HomeViewModel>(factory = HomeViewModel.Factory(application))
            HomePage(viewModel)
        }
        slideUpComposable(route = NavItem.Backup.destination) {
            val viewModel = viewModel<BatchViewModel>(factory = BatchViewModel.Factory(application))
            BatchPage(viewModel, true)
        }
        slideUpComposable(NavItem.Restore.destination) {
            val viewModel = viewModel<BatchViewModel>(factory = BatchViewModel.Factory(application))
            BatchPage(viewModel, false)
        }
        slideUpComposable(NavItem.Scheduler.destination) {
            val viewModel = viewModel<SchedulerViewModel>(
                factory =
                SchedulerViewModel.Factory(
                    ODatabase.getInstance(navController.context).scheduleDao,
                    application
                )
            )
            SchedulerPage(viewModel)
        }
        activity(NavItem.Settings.destination) {
            this.activityClass = PrefsActivityX::class
        }
    }

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PrefsNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    application: Application
) =
    AnimatedNavHost(
        modifier = modifier,
        navController = navController,
        startDestination = NavItem.UserPrefs.destination
    ) {
        slideUpComposable(NavItem.UserPrefs.destination) {
            UserPrefsPage()
        }
        slideUpComposable(route = NavItem.ServicePrefs.destination) {
            ServicePrefsPage()
        }
        slideUpComposable(NavItem.AdvancedPrefs.destination) {
            AdvancedPrefsPage()
        }
        slideUpComposable(NavItem.ToolsPrefs.destination) {
            ToolsPrefsPage(navController)
        }
        slideUpComposable(NavItem.Exports.destination) {
            val viewModel = viewModel<ExportsViewModel>(
                factory =
                ExportsViewModel.Factory(
                    ODatabase.getInstance(navController.context).scheduleDao,
                    application
                )
            )
            ExportsPage(viewModel)
        }
        slideUpComposable(NavItem.Logs.destination) {
            val viewModel = viewModel<LogViewModel>(
                factory =
                LogViewModel.Factory(application)
            )
            LogsPage(viewModel)
        }
    }

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun IntroNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    beenWelcomed: Boolean,
) =
    AnimatedNavHost(
        modifier = modifier,
        navController = navController,
        startDestination = if (beenWelcomed) NavItem.Permissions.destination
        else NavItem.Welcome.destination
    ) {
        slideUpComposable(NavItem.Welcome.destination) {
            WelcomePage()
        }
        slideUpComposable(route = NavItem.Permissions.destination) {
            PermissionsPage()
        }
        activity(NavItem.Main.destination) {
            this.activityClass = MainActivityX::class
        }
    }

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.slideUpComposable(
    route: String,
    composable: @Composable (AnimatedVisibilityScope.(NavBackStackEntry) -> Unit)
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
fun NavGraphBuilder.fadeComposable(
    route: String,
    composable: @Composable (AnimatedVisibilityScope.(NavBackStackEntry) -> Unit)
) {
    composable(
        route,
        enterTransition = { fadeIn(initialAlpha = 0.1f) },
        exitTransition = { fadeOut() }
    ) {
        composable(it)
    }
}
