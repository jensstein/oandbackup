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
package com.machiav3lli.backup.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.machiav3lli.backup.ALT_MODE_APK
import com.machiav3lli.backup.ALT_MODE_BOTH
import com.machiav3lli.backup.ALT_MODE_DATA
import com.machiav3lli.backup.ALT_MODE_UNSET
import com.machiav3lli.backup.MAIN_FILTER_DEFAULT
import com.machiav3lli.backup.R
import com.machiav3lli.backup.dbs.entity.AppExtras
import com.machiav3lli.backup.dialogs.BatchDialogFragment
import com.machiav3lli.backup.dialogs.PackagesListDialogFragment
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.ui.compose.item.ActionButton
import com.machiav3lli.backup.ui.compose.item.ActionChip
import com.machiav3lli.backup.ui.compose.item.ElevatedActionButton
import com.machiav3lli.backup.ui.compose.item.ExpandableSearchAction
import com.machiav3lli.backup.ui.compose.item.TopBar
import com.machiav3lli.backup.ui.compose.recycler.HomePackageRecycler
import com.machiav3lli.backup.ui.compose.recycler.UpdatedPackageRecycler
import com.machiav3lli.backup.ui.compose.theme.AppTheme
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

class HomeFragment : NavigationFragment(),
    BatchDialogFragment.ConfirmListener, RefreshViewController {
    lateinit var viewModel: HomeViewModel
    private var appSheet: AppSheet? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        val viewModelFactory = HomeViewModel.Factory(requireActivity().application)
        viewModel = ViewModelProvider(this, viewModelFactory)[HomeViewModel::class.java]
        return ComposeView(requireContext()).apply {
            setContent { HomePage() }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.filteredList.observe(viewLifecycleOwner) { list ->
            try {
                // TODO live update of AppSheet
                list?.find { it.packageName == appSheet?.appInfo?.packageName }
                    ?.let { sheetApp ->
                        if (appSheet != null && sheetApp != appSheet?.appInfo)
                            refreshAppSheet(sheetApp)
                    }
            } catch (e: Throwable) {
                LogsHandler.unhandledException(e)
            }
        }

        packageList.observe(requireActivity()) { refreshView(it) }
    }

    override fun onResume() {
        super.onResume()
        requireMainActivity().setRefreshViewController(this)
    }

    private fun onClickUpdateAllAction(updatedApps: List<Package>) {
        val selectedList = updatedApps
            .map { it.packageInfo }
            .toCollection(ArrayList())
        val selectedListModes = updatedApps
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
            BatchDialogFragment(true, selectedList, selectedListModes, this)
                .show(requireActivity().supportFragmentManager, "DialogFragment")
        }
    }

    override fun onConfirmed(
        selectedPackages: List<String?>,
        selectedModes: List<Int>
    ) {
        startBatchAction(true, selectedPackages, selectedModes) {
            //viewModel.refreshNow.value = true
            // TODO refresh only the influenced packages
            it.removeObserver(this)
        }
    }

    override fun refreshView(list: MutableList<Package>?) {
        Timber.d("refreshing")
        sheetSortFilter = SortFilterSheet(
            requireActivity().sortFilterModel, getStats(list ?: mutableListOf())
        )
        try {
            viewModel.filteredList.value =
                list?.applyFilter(requireActivity().sortFilterModel, requireContext())
        } catch (e: FileUtils.BackupLocationInAccessibleException) {
            Timber.e("Could not update application list: $e")
        } catch (e: StorageLocationNotConfiguredException) {
            Timber.e("Could not update application list: $e")
        } catch (e: Throwable) {
            LogsHandler.unhandledException(e)
        }
    }

    private fun refreshAppSheet(app: Package) {
        try {
            // TODO implement auto refresh of AppSheet
            appSheet?.updateApp(app)
        } catch (e: Throwable) {
            appSheet?.dismissAllowingStateLoss()
            LogsHandler.unhandledException(e)
        }
    }

    override fun updateProgress(progress: Int, max: Int) {
        //binding.progressBar.visibility = View.VISIBLE
        //binding.progressBar.max = max
        //binding.progressBar.progress = progress
    }

    override fun hideProgress() {
        //binding.progressBar.visibility = View.GONE
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun HomePage() {
        // TODO include tags in search
        val list by viewModel.filteredList.observeAsState(null)
        val query by viewModel.searchQuery.observeAsState("")
        val updatedApps = list?.filter { it.isUpdated }
        var updatedVisible by remember(viewModel.filteredList.value) { mutableStateOf(false) }

        val filterPredicate = { item: Package ->
            query.isNullOrEmpty() || listOf(item.packageName, item.packageLabel)
                .find { it.contains(query, true) } != null
        }
        val queriedList = list?.filter(filterPredicate)

        AppTheme(
            darkTheme = isSystemInDarkTheme()
        ) {
            Scaffold(
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
                        .background(color = MaterialTheme.colorScheme.surface)
                        .fillMaxSize()
                ) {
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ActionChip(
                            icon = painterResource(id = R.drawable.ic_blocklist),
                            text = stringResource(id = R.string.sched_blocklist),
                            positive = true
                        ) {
                            GlobalScope.launch(Dispatchers.IO) {
                                val blocklistedPackages =
                                    requireMainActivity().viewModel.blocklist.value
                                        ?.mapNotNull { it.packageName }.orEmpty()

                                PackagesListDialogFragment(
                                    blocklistedPackages,
                                    MAIN_FILTER_DEFAULT,
                                    true
                                ) { newList: Set<String> ->
                                    requireMainActivity().viewModel.updateBlocklist(newList)
                                }.show(
                                    requireActivity().supportFragmentManager,
                                    "BLOCKLIST_DIALOG"
                                )
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        ActionChip(
                            icon = painterResource(id = R.drawable.ic_filter),
                            text = stringResource(id = R.string.sort_and_filter),
                            positive = true
                        ) {
                            if (sheetSortFilter == null) sheetSortFilter = SortFilterSheet(
                                requireActivity().sortFilterModel,
                                getStats(packageList.value ?: mutableListOf())
                            )
                            sheetSortFilter?.showNow(
                                requireActivity().supportFragmentManager,
                                "SORTFILTER_SHEET"
                            )
                        }
                    }
                    HomePackageRecycler(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        productsList = queriedList,
                        onClick = { item ->
                            if (appSheet != null) appSheet?.dismissAllowingStateLoss()
                            appSheet = AppSheet(item, AppExtras(item.packageName))
                            appSheet?.showNow(
                                parentFragmentManager,
                                "Package ${item.packageName}"
                            )
                        }
                    )
                    AnimatedVisibility(visible = !updatedApps.isNullOrEmpty()) {
                        Column(
                            modifier = Modifier.wrapContentHeight()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                ActionButton(
                                    modifier = Modifier.weight(1f),
                                    text = LocalContext.current.resources
                                        .getQuantityString(
                                            R.plurals.updated_apps,
                                            updatedApps.orEmpty().size,
                                            updatedApps.orEmpty().size
                                        ),
                                    icon = painterResource(id = if (updatedVisible) R.drawable.ic_arrow_down else R.drawable.ic_arrow_up)
                                ) {
                                    updatedVisible = !updatedVisible
                                }
                                ElevatedActionButton(
                                    modifier = Modifier,
                                    text = stringResource(id = R.string.backup_all_updated),
                                    icon = painterResource(id = R.drawable.ic_update)
                                ) {
                                    onClickUpdateAllAction(updatedApps.orEmpty())
                                }
                            }
                            AnimatedVisibility(visible = updatedVisible) {
                                UpdatedPackageRecycler(productsList = updatedApps,
                                    onClick = { item ->
                                        if (appSheet != null) appSheet?.dismissAllowingStateLoss()
                                        appSheet = AppSheet(item, AppExtras(item.packageName))
                                        appSheet?.showNow(
                                            parentFragmentManager,
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
    }
}
