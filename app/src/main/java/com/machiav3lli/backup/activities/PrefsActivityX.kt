/*
 * OAndBackupX: open-source apps backup and restore app.
 * Copyright (C) 2020  Antonios Hazim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.machiav3lli.backup.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.machiav3lli.backup.NAV_PREFS
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.fragments.HelpSheet
import com.machiav3lli.backup.ui.compose.item.TopBar
import com.machiav3lli.backup.ui.compose.item.TopBarButton
import com.machiav3lli.backup.ui.compose.navigation.BottomNavBar
import com.machiav3lli.backup.ui.compose.navigation.PrefsNavHost
import com.machiav3lli.backup.ui.compose.theme.AppTheme
import com.machiav3lli.backup.utils.setCustomTheme

class PrefsActivityX : BaseActivity() {
    private var helpSheet: HelpSheet? = null

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        OABX.activity = this
        setCustomTheme()
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme(
                darkTheme = isSystemInDarkTheme()
            ) {
                val navController = rememberAnimatedNavController()

                Scaffold(
                    topBar = {
                        TopBar(
                            title = title.toString()
                        ) {
                            TopBarButton(
                                icon = painterResource(id = R.drawable.ic_info),
                                description = stringResource(id = R.string.help),
                            ) {
                                if (helpSheet != null && helpSheet!!.isVisible) helpSheet?.dismissAllowingStateLoss()
                                helpSheet = HelpSheet()
                                helpSheet!!.showNow(supportFragmentManager, "HELPSHEET")
                            }
                        }
                    },
                    bottomBar = { BottomNavBar(page = NAV_PREFS, navController = navController) }
                ) { paddingValues ->
                    PrefsNavHost(
                        modifier = Modifier.padding(paddingValues),
                        navController = navController
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        OABX.activity = this
    }
}