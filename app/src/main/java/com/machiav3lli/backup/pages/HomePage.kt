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
package com.machiav3lli.backup.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.ALT_MODE_APK
import com.machiav3lli.backup.ALT_MODE_BOTH
import com.machiav3lli.backup.ALT_MODE_DATA
import com.machiav3lli.backup.ALT_MODE_UNSET
import com.machiav3lli.backup.MAIN_FILTER_DEFAULT
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.dialogs.BatchDialogFragment
import com.machiav3lli.backup.dialogs.PackagesListDialogFragment
import com.machiav3lli.backup.fragments.AppSheet
import com.machiav3lli.backup.fragments.SortFilterSheet
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.ui.compose.item.ActionButton
import com.machiav3lli.backup.ui.compose.item.ElevatedActionButton
import com.machiav3lli.backup.ui.compose.item.ExpandableSearchAction
import com.machiav3lli.backup.ui.compose.item.TopBar
import com.machiav3lli.backup.ui.compose.recycler.HomePackageRecycler
import com.machiav3lli.backup.ui.compose.recycler.UpdatedPackageRecycler
import com.machiav3lli.backup.utils.FileUtils
import com.machiav3lli.backup.utils.StorageLocationNotConfiguredException
import com.machiav3lli.backup.utils.applyFilter
import com.machiav3lli.backup.utils.getStats
import com.machiav3lli.backup.utils.sortFilterModel
import com.machiav3lli.backup.viewmodels.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(viewModel: HomeViewModel) {
    // TODO include tags in search
    val context = LocalContext.current
    var appSheet: AppSheet? = null
    var sheetSortFilter: SortFilterSheet? = null
    val list by (context as MainActivityX).viewModel.packageList.observeAsState(null)
    val filteredList by viewModel.filteredList.observeAsState(null)
    val query by viewModel.searchQuery.observeAsState("")
    val updatedApps = filteredList?.filter { it.isUpdated }
    var updatedVisible by remember(viewModel.filteredList.value) { mutableStateOf(false) }
    OABX.main?.viewModel?.isNeedRefresh?.observeForever {
        viewModel.refreshing.postValue(it)
    }

    val filterPredicate = { item: Package ->
        query.isNullOrEmpty() || listOf(item.packageName, item.packageLabel)
            .find { it.contains(query, true) } != null
    }
    val queriedList = filteredList?.filter(filterPredicate)
    val refreshing by viewModel.refreshing.observeAsState()
    val progress by viewModel.progress.observeAsState(Pair(false, 0f))

    val batchConfirmListener = object : BatchDialogFragment.ConfirmListener {
        override fun onConfirmed(selectedPackages: List<String?>, selectedModes: List<Int>) {
            (context as MainActivityX).startBatchAction(true, selectedPackages, selectedModes) {
                it.removeObserver(this)
            }
        }
    }

    LaunchedEffect(key1 = list) {
        sheetSortFilter = SortFilterSheet(
            context.sortFilterModel, getStats(list ?: mutableListOf())
        )
        try {
            viewModel.filteredList.value =
                list?.applyFilter(context.sortFilterModel, context)
        } catch (e: FileUtils.BackupLocationInAccessibleException) {
            Timber.e("Could not update application list: $e")
        } catch (e: StorageLocationNotConfiguredException) {
            Timber.e("Could not update application list: $e")
        } catch (e: Throwable) {
            LogsHandler.unhandledException(e)
        }
    }


    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopBar(title = stringResource(id = R.string.main)) {
                ExpandableSearchAction(
                    query = viewModel.searchQuery.value.orEmpty(),
                    onQueryChanged = { new ->
                        viewModel.searchQuery.value = new
                    },
                    onClose = {
                        viewModel.searchQuery.value = ""
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ElevatedActionButton(
                    icon = painterResource(id = R.drawable.ic_blocklist),
                    text = stringResource(id = R.string.sched_blocklist),
                    positive = false
                ) {
                    GlobalScope.launch(Dispatchers.IO) {
                        val blocklistedPackages =
                            (context as MainActivityX).viewModel.blocklist.value
                                ?.mapNotNull { it.packageName }.orEmpty()

                        PackagesListDialogFragment(
                            blocklistedPackages,
                            MAIN_FILTER_DEFAULT,
                            true
                        ) { newList: Set<String> ->
                            context.viewModel.updateBlocklist(newList)
                        }.show(
                            context.supportFragmentManager,
                            "BLOCKLIST_DIALOG"
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                ElevatedActionButton(
                    icon = painterResource(id = R.drawable.ic_filter),
                    text = stringResource(id = R.string.sort_and_filter),
                    positive = true
                ) {
                    if (sheetSortFilter == null) sheetSortFilter = SortFilterSheet(
                        (context as MainActivityX).sortFilterModel,
                        getStats(list ?: mutableListOf())
                    )
                    sheetSortFilter?.showNow(
                        (context as MainActivityX).supportFragmentManager,
                        "SORTFILTER_SHEET"
                    )
                }
            }
            AnimatedVisibility(visible = refreshing ?: false) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            AnimatedVisibility(visible = progress?.first == true) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    progress = progress.second
                )
            }
            HomePackageRecycler(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                productsList = queriedList,
                onClick = { item ->
                    if (appSheet != null) appSheet?.dismissAllowingStateLoss()
                    appSheet = AppSheet(item)
                    appSheet?.showNow(
                        (context as MainActivityX).supportFragmentManager,
                        "Package ${item.packageName}"
                    )
                }
            )
            AnimatedVisibility(visible = !updatedApps.isNullOrEmpty()) {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .wrapContentHeight()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ActionButton(
                            modifier = Modifier.weight(1f),
                            text = updatedApps.orEmpty().size.toString(),
                            icon = painterResource(id = if (updatedVisible) R.drawable.ic_arrow_down else R.drawable.ic_updated)
                        ) {
                            updatedVisible = !updatedVisible
                        }
                        ElevatedActionButton(
                            modifier = Modifier,
                            text = stringResource(id = R.string.backup_all_updated),
                            icon = painterResource(id = R.drawable.ic_update)
                        ) {
                            val selectedList = updatedApps.orEmpty()
                                .map { it.packageInfo }
                                .toCollection(ArrayList())
                            val selectedListModes = updatedApps.orEmpty()
                                .mapNotNull {
                                    it.latestBackup?.let { bp ->
                                        when {
                                            bp.hasApk && bp.hasAppData -> ALT_MODE_BOTH
                                            bp.hasApk -> ALT_MODE_APK
                                            bp.hasAppData -> ALT_MODE_DATA
                                            else -> ALT_MODE_UNSET
                                        }
                                    }
                                }
                                .toCollection(ArrayList())
                            if (selectedList.isNotEmpty()) {
                                BatchDialogFragment(
                                    true,
                                    selectedList,
                                    selectedListModes,
                                    batchConfirmListener
                                )
                                    .show(
                                        (context as MainActivityX).supportFragmentManager,
                                        "DialogFragment"
                                    )
                            }
                        }
                    }
                    AnimatedVisibility(visible = updatedVisible) {
                        UpdatedPackageRecycler(
                            productsList = updatedApps,
                            onClick = { item ->
                                if (appSheet != null) appSheet?.dismissAllowingStateLoss()
                                appSheet = AppSheet(item)
                                appSheet?.showNow(
                                    (context as MainActivityX).supportFragmentManager,
                                    "Package ${item.packageName}"
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
