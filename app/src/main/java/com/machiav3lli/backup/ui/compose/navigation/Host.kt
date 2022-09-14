package com.machiav3lli.backup.ui.compose.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.machiav3lli.backup.preferences.AdvancedPrefsPage
import com.machiav3lli.backup.preferences.ExportsPage
import com.machiav3lli.backup.preferences.LogsPage
import com.machiav3lli.backup.preferences.ServicePrefsPage
import com.machiav3lli.backup.preferences.ToolsPrefsPage
import com.machiav3lli.backup.preferences.UserPrefsPage
import com.machiav3lli.backup.viewmodels.ExportsViewModel
import com.machiav3lli.backup.viewmodels.LogViewModel

@Composable
        /** TODO Replace all those inefficient calls with real composables (When fragments are migrated) or wait androidx to fix fragment
         * an example of possible detour with greatly bad performance:
         * composable(BottomNavItem.Home.destination) {
         *    AndroidView(
         *       modifier = Modifier.fillMaxSize(),
         *       factory = {
         *          FragmentContainerView(context = navController.context).apply {
         *             id = R.id.fragmentContainer
         *          }
         *       },
         *       update = {
         *          activity.supportFragmentManager.beginTransaction().replace(
         *          R.id.fragmentContainer,
         *          HomeFragment::class.java,
         *          null
         *          ).commitAllowingStateLoss()
         *       }
         *    )
         * }
         */
fun MainNavHost(modifier: Modifier = Modifier, navController: NavHostController) =
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = NavItem.Home.destination
    ) {
        /*fragment<PreferenceFragmentCompat>(NavItem.Home.destination) {
            label = navController.context.getString(NavItem.Home.title)
        }
        fragment<PreferenceFragmentCompat>(NavItem.Backup.destination) {
            label = navController.context.getString(NavItem.Backup.title)
        }
        fragment<PreferenceFragmentCompat>(NavItem.Restore.destination) {
            label = navController.context.getString(NavItem.Restore.title)
        }
        fragment<PreferenceFragmentCompat>(NavItem.Scheduler.destination) {
            label = navController.context.getString(NavItem.Scheduler.title)
        }
        activity(NavItem.Settings.destination) {
            targetPackage = PrefsActivityX::class.java.`package`.name
            activityClass = PrefsActivityX::class
        }*/
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
