package com.machiav3lli.backup.ui.compose.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.activity
import androidx.navigation.compose.NavHost
import androidx.navigation.fragment.fragment
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.activities.PrefsActivity
import com.machiav3lli.backup.fragments.PrefsAdvancedFragment
import com.machiav3lli.backup.fragments.PrefsServiceFragment
import com.machiav3lli.backup.fragments.PrefsToolsFragment
import com.machiav3lli.backup.fragments.PrefsUserFragment

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
fun MainNavHost(activity: MainActivityX, navController: NavHostController) =
    NavHost(navController, startDestination = BottomNavItem.Home.destination) {
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
            targetPackage = PrefsActivity::class.java.`package`.name
            activityClass = PrefsActivity::class
        }
    }

@Composable
fun PrefsNavHost(navController: NavHostController) =
    NavHost(navController, startDestination = BottomNavItem.UserPrefs.destination) {
        fragment<PrefsUserFragment>(BottomNavItem.UserPrefs.destination) {
            label = navController.context.getString(BottomNavItem.UserPrefs.title)
        }
        fragment<PrefsServiceFragment>(BottomNavItem.ServicePrefs.destination) {
            label = navController.context.getString(BottomNavItem.ServicePrefs.title)
        }
        fragment<PrefsAdvancedFragment>(BottomNavItem.AdvancedPrefs.destination) {
            label = navController.context.getString(BottomNavItem.AdvancedPrefs.title)
        }
        fragment<PrefsToolsFragment>(BottomNavItem.ToolsPrefs.destination) {
            label = navController.context.getString(BottomNavItem.ToolsPrefs.title)
        }
    }