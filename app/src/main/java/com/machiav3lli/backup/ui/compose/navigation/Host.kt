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
import androidx.navigation.activity
import androidx.navigation.compose.NavHost
import androidx.navigation.fragment.fragment
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.machiav3lli.backup.activities.PrefsActivityX
import com.machiav3lli.backup.fragments.PrefsUserFragment
import com.machiav3lli.backup.preferences.AdvancedPrefsPage
import com.machiav3lli.backup.preferences.ServicePrefsPage
import com.machiav3lli.backup.preferences.ToolsPrefsPage
import com.machiav3lli.backup.preferences.UserPrefsPage

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
        startDestination = BottomNavItem.Home.destination
    ) {
        fragment<PrefsUserFragment>(BottomNavItem.Home.destination) {
            label = navController.context.getString(BottomNavItem.Home.title)
        }
        fragment<PrefsUserFragment>(BottomNavItem.Backup.destination) {
            label = navController.context.getString(BottomNavItem.Backup.title)
        }
        fragment<PrefsUserFragment>(BottomNavItem.Restore.destination) {
            label = navController.context.getString(BottomNavItem.Restore.title)
        }
        fragment<PrefsUserFragment>(BottomNavItem.Scheduler.destination) {
            label = navController.context.getString(BottomNavItem.Scheduler.title)
        }
        activity(BottomNavItem.Settings.destination) {
            targetPackage = PrefsActivityX::class.java.`package`.name
            activityClass = PrefsActivityX::class
        }
    }

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PrefsNavHost(modifier: Modifier = Modifier, navController: NavHostController) =
    AnimatedNavHost(
        modifier = modifier,
        navController = navController,
        startDestination = BottomNavItem.UserPrefs.destination
    ) {
        slideUpComposable(BottomNavItem.UserPrefs.destination) {
            UserPrefsPage()
        }
        slideUpComposable(route = BottomNavItem.ServicePrefs.destination) {
            ServicePrefsPage()
        }
        slideUpComposable(BottomNavItem.AdvancedPrefs.destination) {
            AdvancedPrefsPage()
        }
        slideUpComposable(BottomNavItem.ToolsPrefs.destination) {
            ToolsPrefsPage()
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
