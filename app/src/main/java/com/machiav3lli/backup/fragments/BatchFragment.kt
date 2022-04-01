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
import androidx.appcompat.widget.SearchView
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.machiav3lli.backup.*
import com.machiav3lli.backup.R
import com.machiav3lli.backup.databinding.FragmentBatchBinding
import com.machiav3lli.backup.dialogs.BatchDialogFragment
import com.machiav3lli.backup.dialogs.PackagesListDialogFragment
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.WorkHandler
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.tasks.AppActionWork
import com.machiav3lli.backup.tasks.FinishWork
import com.machiav3lli.backup.ui.compose.item.ActionButton
import com.machiav3lli.backup.ui.compose.item.StateChip
import com.machiav3lli.backup.ui.compose.recycler.BatchPackageRecycler
import com.machiav3lli.backup.ui.compose.theme.APK
import com.machiav3lli.backup.ui.compose.theme.AppTheme
import com.machiav3lli.backup.ui.compose.theme.Data
import com.machiav3lli.backup.utils.*
import com.machiav3lli.backup.viewmodels.BatchViewModel
import timber.log.Timber

open class BatchFragment(private val backupBoolean: Boolean) : NavigationFragment(),
    BatchDialogFragment.ConfirmListener, RefreshViewController {
    private lateinit var binding: FragmentBatchBinding
    lateinit var viewModel: BatchViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        binding = FragmentBatchBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        val viewModelFactory = BatchViewModel.Factory(requireActivity().application)
        viewModel = ViewModelProvider(this, viewModelFactory)[BatchViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()

        viewModel.refreshNow.observe(requireActivity()) {
            //binding.refreshLayout.isRefreshing = it
            if (it) {
                binding.searchBar.setQuery("", false)
                requireMainActivity().viewModel.refreshList()
            }
        }
        viewModel.filteredList.observe(viewLifecycleOwner) { list ->
            try {
                redrawList(list, viewModel.searchQuery.value)
                setupSearch()
                viewModel.refreshNow.value = false
            } catch (e: Throwable) {
                LogsHandler.unhandledException(e)
            }
        }

        packageList.observe(requireActivity()) { refreshView(it) }
    }

    override fun onStart() {
        super.onStart()
        binding.pageHeadline.text = when {
            backupBoolean -> resources.getText(R.string.backup)
            else -> resources.getText(R.string.restore)
        }
    }

    override fun onResume() {
        super.onResume()
        setupSearch()
        setupOnClicks()
        requireMainActivity().setRefreshViewController(this)
    }

    override fun setupViews() {
    }

    override fun setupOnClicks() {
        binding.buttonBlocklist.setOnClickListener {
            Thread {
                val blocklistedPackages = requireMainActivity().viewModel.blocklist.value
                    ?.mapNotNull { it.packageName }
                    ?: listOf()

                PackagesListDialogFragment(
                    blocklistedPackages,
                    MAIN_FILTER_DEFAULT,
                    true
                ) { newList: Set<String> ->
                    requireMainActivity().viewModel.updateBlocklist(newList)
                }.show(requireActivity().supportFragmentManager, "BLOCKLIST_DIALOG")
            }.start()
        }
        binding.buttonSortFilter.setOnClickListener {
            if (sheetSortFilter == null) sheetSortFilter = SortFilterSheet(
                requireActivity().sortFilterModel,
                getStats(packageList.value ?: mutableListOf())
            )
            sheetSortFilter?.showNow(requireActivity().supportFragmentManager, "SORTFILTER_SHEET")
        }
    }

    private fun setupSearch() {
        binding.searchBar.maxWidth = Int.MAX_VALUE
        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                viewModel.searchQuery.value = newText
                redrawList(viewModel.filteredList.value, newText)
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                viewModel.searchQuery.value = query
                redrawList(viewModel.filteredList.value, query)
                return true
            }
        })
    }

    private fun onClickBatchAction(backupBoolean: Boolean) {
        val checkedPackages = viewModel.filteredList.value
            ?.filter { it.packageName in viewModel.apkCheckedList.union(viewModel.dataCheckedList) }
            ?: listOf()
        val selectedList = checkedPackages.map(Package::packageInfo).toCollection(ArrayList())
        val selectedListModes = checkedPackages
            .map {
                when (it.packageName) {
                    in viewModel.apkCheckedList.intersect(viewModel.dataCheckedList) -> ALT_MODE_BOTH
                    in viewModel.apkCheckedList -> ALT_MODE_APK
                    else -> ALT_MODE_DATA
                }
            }
            .toCollection(ArrayList())
        if (selectedList.isNotEmpty()) {
            BatchDialogFragment(backupBoolean, selectedList, selectedListModes, this)
                .show(requireActivity().supportFragmentManager, "DialogFragment")
        }
    }

    // TODO abstract this to fit for Main- & BatchFragment
    override fun onConfirmed(selectedPackages: List<String?>, selectedModes: List<Int>) {

        val notificationId = System.currentTimeMillis().toInt()
        val now = System.currentTimeMillis()
        val batchType = getString(if (backupBoolean) R.string.backup else R.string.restore)
        val batchName = WorkHandler.getBatchName(batchType, now)

        val selectedItems = selectedPackages
            .mapIndexed { i, packageName ->
                if (packageName.isNullOrEmpty()) null
                else Pair(packageName, selectedModes[i])
            }
            .filterNotNull()

        var errors = ""
        var resultsSuccess = true
        var counter = 0
        val worksList: MutableList<OneTimeWorkRequest> = mutableListOf()
        OABX.work.beginBatch(batchName)
        selectedItems.forEach { (packageName, mode) ->

            val oneTimeWorkRequest =
                AppActionWork.Request(packageName, mode, backupBoolean, notificationId, batchName)
            worksList.add(oneTimeWorkRequest)

            val oneTimeWorkLiveData = WorkManager.getInstance(requireContext())
                .getWorkInfoByIdLiveData(oneTimeWorkRequest.id)
            oneTimeWorkLiveData.observeForever(object : Observer<WorkInfo> {
                override fun onChanged(t: WorkInfo?) {
                    if (t?.state == WorkInfo.State.SUCCEEDED) {
                        binding.progressBar.progress = counter
                        counter += 1

                        val (succeeded, packageLabel, error) = AppActionWork.getOutput(t)
                        if (error.isNotEmpty()) errors = "$errors$packageLabel: ${
                            LogsHandler.handleErrorMessages(
                                requireContext(),
                                error
                            )
                        }\n"

                        resultsSuccess = resultsSuccess and succeeded
                        oneTimeWorkLiveData.removeObserver(this)
                    }
                }
            })
        }

        val finishWorkRequest = FinishWork.Request(resultsSuccess, backupBoolean, batchName)

        val finishWorkLiveData = WorkManager.getInstance(requireContext())
            .getWorkInfoByIdLiveData(finishWorkRequest.id)
        finishWorkLiveData.observeForever(object : Observer<WorkInfo> {
            override fun onChanged(t: WorkInfo?) {
                if (t?.state == WorkInfo.State.SUCCEEDED) {
                    viewModel.refreshNow.value = true
                    finishWorkLiveData.removeObserver(this)
                }
            }
        })

        if (worksList.isNotEmpty()) {
            WorkManager.getInstance(requireContext())
                .beginWith(worksList)
                .then(finishWorkRequest)
                .enqueue()
        }
    }

    override fun refreshView(list: MutableList<Package>?) {
        Timber.d("refreshing")
        sheetSortFilter =
            SortFilterSheet(requireActivity().sortFilterModel, getStats(list ?: mutableListOf()))
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

    override fun updateProgress(progress: Int, max: Int) {
        binding.progressBar.visibility = View.VISIBLE
        binding.progressBar.max = max
        binding.progressBar.progress = progress
    }

    override fun hideProgress() {
        binding.progressBar.visibility = View.GONE
    }

    fun redrawList(list: List<Package>?, query: String? = "") {
        binding.recyclerView.setContent {

            // TODO include tags in search
            val filterPredicate = { item: Package ->
                val includedBoolean = if (backupBoolean) item.isInstalled else item.hasBackups
                val queryBoolean =
                    query.isNullOrEmpty() || listOf(item.packageName, item.packageLabel)
                        .find { it.contains(query, true) } != null
                includedBoolean && queryBoolean
            }
            var allApkChecked by remember {
                mutableStateOf(viewModel.apkCheckedList.size == list?.size)
            }
            var allDataChecked by remember {
                mutableStateOf(viewModel.dataCheckedList.size == list?.size)
            }

            AppTheme(
                darkTheme = isSystemInDarkTheme()
            ) {
                Scaffold {
                    Column(modifier = Modifier.fillMaxSize()) {

                        BatchPackageRecycler(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            productsList = list?.filter(filterPredicate),
                            !backupBoolean,
                            viewModel.apkCheckedList,
                            viewModel.dataCheckedList,
                            onApkClick = { item: Package, b: Boolean ->
                                if (b) viewModel.apkCheckedList.add(item.packageName)
                                else viewModel.apkCheckedList.remove(item.packageName)
                                allApkChecked =
                                    viewModel.apkCheckedList.size == viewModel.filteredList.value
                                        ?.filter { ai -> !ai.isSpecial && (backupBoolean || ai.hasApk) }?.size
                            }, onDataClick = { item: Package, b: Boolean ->
                                if (b) viewModel.dataCheckedList.add(item.packageName)
                                else viewModel.dataCheckedList.remove(item.packageName)
                                allDataChecked =
                                    viewModel.dataCheckedList.size == viewModel.filteredList.value
                                        ?.filter { ai -> backupBoolean || ai.hasData }?.size
                            }) { item, checkApk, checkData ->
                            when (checkApk) {
                                true -> viewModel.apkCheckedList.add(item.packageName)
                                else -> viewModel.apkCheckedList.remove(item.packageName)
                            }
                            when (checkData) {
                                true -> viewModel.dataCheckedList.add(item.packageName)
                                else -> viewModel.dataCheckedList.remove(item.packageName)
                            }
                            allApkChecked =
                                viewModel.apkCheckedList.size == viewModel.filteredList.value
                                    ?.filter { ai -> !ai.isSpecial && (backupBoolean || ai.hasApk) }?.size
                            allDataChecked =
                                viewModel.dataCheckedList.size == viewModel.filteredList.value
                                    ?.filter { ai -> backupBoolean || ai.hasData }?.size
                        }
                        Row(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            StateChip(
                                modifier = Modifier.padding(start = 8.dp, end = 4.dp),
                                icon = painterResource(id = R.drawable.ic_apk),
                                text = stringResource(id = R.string.all_apk),
                                checked = allApkChecked,
                                color = APK
                            ) {
                                val checkBoolean = !allApkChecked
                                allApkChecked = checkBoolean
                                if (checkBoolean)
                                    viewModel.apkCheckedList.addAll(
                                        viewModel.filteredList.value
                                            ?.filter { ai -> !ai.isSpecial && (backupBoolean || ai.hasApk) }
                                            ?.mapNotNull(Package::packageName).orEmpty()
                                    )
                                else
                                    viewModel.apkCheckedList.clear()
                                redrawList(
                                    viewModel.filteredList.value,
                                    viewModel.searchQuery.value
                                )
                            }
                            StateChip(
                                icon = painterResource(id = R.drawable.ic_data),
                                text = stringResource(id = R.string.all_data),
                                checked = allDataChecked,
                                color = Data
                            ) {
                                val checkBoolean = !allDataChecked
                                allDataChecked = checkBoolean
                                if (checkBoolean)
                                    viewModel.dataCheckedList.addAll(
                                        viewModel.filteredList.value
                                            ?.filter { ai -> backupBoolean || ai.hasData }
                                            ?.mapNotNull(Package::packageName).orEmpty()
                                    )
                                else
                                    viewModel.dataCheckedList.clear()
                                redrawList(
                                    viewModel.filteredList.value,
                                    viewModel.searchQuery.value
                                )
                            }
                            ActionButton(
                                modifier = Modifier.weight(1f),
                                text = stringResource(id = if (backupBoolean) R.string.backup else R.string.restore),
                                positive = true
                            ) {
                                onClickBatchAction(backupBoolean)
                            }
                        }
                    }
                }
            }
        }
    }

    class BackupFragment : BatchFragment(true)
    class RestoreFragment : BatchFragment(false)
}
