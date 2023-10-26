/*
 * Neo Backup: open-source apps backup and restore app.
 * Copyright (C) 2023  Antonios Hazim
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
package com.machiav3lli.backup.pages

import android.content.ComponentName
import android.content.Intent
import android.os.Looper
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.dialogs.BaseDialog
import com.machiav3lli.backup.dialogs.GlobalBlockListDialogUI
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.pref_catchUncaughtException
import com.machiav3lli.backup.pref_uncaughtExceptionsJumpToPreferences
import com.machiav3lli.backup.sheets.Sheet
import com.machiav3lli.backup.sheets.SortFilterSheet
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.FunnelSimple
import com.machiav3lli.backup.ui.compose.icons.phosphor.GearSix
import com.machiav3lli.backup.ui.compose.icons.phosphor.Prohibit
import com.machiav3lli.backup.ui.compose.item.ActionChip
import com.machiav3lli.backup.ui.compose.item.ExpandableSearchAction
import com.machiav3lli.backup.ui.compose.item.RefreshButton
import com.machiav3lli.backup.ui.compose.item.RoundButton
import com.machiav3lli.backup.ui.compose.item.TopBar
import com.machiav3lli.backup.ui.navigation.NavItem
import com.machiav3lli.backup.ui.navigation.PagerNavBar
import com.machiav3lli.backup.ui.navigation.SlidePager
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.system.exitProcess

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainPage(
    navController: NavHostController,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pages = listOf(
        NavItem.Home,
        NavItem.Backup,
        NavItem.Restore,
        NavItem.Scheduler,
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val currentPage by remember(pagerState.currentPage) { mutableStateOf(pages[pagerState.currentPage]) }
    val showSortSheet = remember { mutableStateOf(false) }
    val sortSheetState = rememberModalBottomSheetState(true)

    OABX.appsSuspendedChecked = false

    if (pref_catchUncaughtException.value) {
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            try {
                Timber.i("\n\n" + "=".repeat(60))
                LogsHandler.unexpectedException(e)
                LogsHandler.logErrors("uncaught: ${e.message}")
                if (pref_uncaughtExceptionsJumpToPreferences.value) {
                    context.startActivity(
                        Intent.makeRestartActivityTask(
                            ComponentName(OABX.context, MainActivityX::class.java)
                        )
                    )
                }
                object : Thread() {
                    override fun run() {
                        Looper.prepare()
                        Looper.loop()
                    }
                }.start()
            } catch (_: Throwable) {
                // ignore
            } finally {
                exitProcess(2)
            }
        }
    }

    Shell.getShell()


    BackHandler {
        OABX.main?.finishAffinity()
    }

    val openBlocklist = remember { mutableStateOf(false) }

    var query by rememberSaveable {
        mutableStateOf(
            OABX.main?.viewModel?.searchQuery?.value ?: ""
        )
    }
    val searchExpanded = remember {
        mutableStateOf(false)
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            when (currentPage.destination) {
                NavItem.Scheduler.destination -> TopBar(
                    title = stringResource(id = currentPage.title)
                ) {

                    RoundButton(
                        icon = Phosphor.Prohibit,
                        description = stringResource(id = R.string.sched_blocklist)
                    ) {
                        openBlocklist.value = true
                    }
                    RoundButton(
                        description = stringResource(id = R.string.prefs_title),
                        icon = Phosphor.GearSix
                    ) { navController.navigate(NavItem.Settings.destination) }
                }

                else                          -> Column {
                    TopBar(title = stringResource(id = currentPage.title)) {
                        ExpandableSearchAction(
                            expanded = searchExpanded,
                            query = query,
                            onQueryChanged = { newQuery ->
                                //if (newQuery != query)  // empty string doesn't work...
                                query = newQuery
                                OABX.main?.viewModel?.searchQuery?.value = query
                            },
                            onClose = {
                                query = ""
                                OABX.main?.viewModel?.searchQuery?.value = ""
                            }
                        )
                        AnimatedVisibility(!searchExpanded.value) {
                            RefreshButton { OABX.main?.refreshPackagesAndBackups() }
                        }
                        AnimatedVisibility(!searchExpanded.value) {
                            RoundButton(
                                description = stringResource(id = R.string.prefs_title),
                                icon = Phosphor.GearSix
                            ) { navController.navigate(NavItem.Settings.destination) }
                        }
                    }
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActionChip(
                            modifier = Modifier.weight(1f),
                            icon = Phosphor.Prohibit,
                            text = stringResource(id = R.string.sched_blocklist),
                            positive = false,
                            fullWidth = true,
                        ) {
                            openBlocklist.value = true
                        }
                        ActionChip(
                            modifier = Modifier.weight(1f),
                            icon = Phosphor.FunnelSimple,
                            text = stringResource(id = R.string.sort_and_filter),
                            positive = true,
                            fullWidth = true,
                        ) {
                            showSortSheet.value = true
                        }
                    }
                }
            }
        },
        bottomBar = {
            PagerNavBar(pageItems = pages, pagerState = pagerState)
        }
    ) { paddingValues ->

        SlidePager(
            modifier = Modifier.padding(paddingValues),
            pagerState = pagerState,
            pageItems = pages,
            navController = navController
        )

        if (showSortSheet.value) {
            val dismiss = {
                scope.launch { sortSheetState.hide() }
                showSortSheet.value = false
            }
            Sheet(
                sheetState = sortSheetState,
                onDismissRequest = dismiss,
            ) {
                SortFilterSheet(
                    onDismiss = dismiss,
                )
            }
        }
        if (openBlocklist.value) BaseDialog(openDialogCustom = openBlocklist) {
            GlobalBlockListDialogUI(
                currentBlocklist = OABX.main?.viewModel?.getBlocklist()?.toSet()
                    ?: emptySet(),
                openDialogCustom = openBlocklist,
            ) { newSet ->
                OABX.main?.viewModel?.setBlocklist(newSet)
            }
        }
    }
}
