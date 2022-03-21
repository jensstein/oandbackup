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

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.SearchView
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Scaffold
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.machiav3lli.backup.MAIN_FILTER_DEFAULT
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.databinding.FragmentBatchBinding
import com.machiav3lli.backup.dialogs.BatchDialogFragment
import com.machiav3lli.backup.dialogs.PackagesListDialogFragment
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.WorkHandler
import com.machiav3lli.backup.items.AppInfo
import com.machiav3lli.backup.tasks.AppActionWork
import com.machiav3lli.backup.tasks.FinishWork
import com.machiav3lli.backup.ui.compose.recycler.BatchPackageRecycler
import com.machiav3lli.backup.ui.compose.theme.AppTheme
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
        if (requireMainActivity().viewModel.refreshNow.value == true) refreshView()

        viewModel.refreshNow.observe(requireActivity()) {
            //binding.refreshLayout.isRefreshing = it
            if (it) {
                binding.searchBar.setQuery("", false)
                requireMainActivity().viewModel.refreshList()
            }
        }
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

    override fun onInflate(context: Context, attrs: AttributeSet, savedInstanceState: Bundle?) {
        super.onInflate(context, attrs, savedInstanceState)
        refreshView()
    }

    override fun setupViews() {
        binding.buttonAction.setText(if (backupBoolean) R.string.backup else R.string.restore)
        /*binding.refreshLayout.setColorSchemeColors(requireContext().colorAccent)
        binding.refreshLayout.setProgressBackgroundColorSchemeColor(
            resources.getColor(
                R.color.app_primary_base,
                requireActivity().theme
            )
        )
        binding.refreshLayout.setProgressViewOffset(false, 72, 144)
        binding.refreshLayout.setOnRefreshListener { requireMainActivity().viewModel.refreshList() }*/
        binding.buttonAction.setOnClickListener { onClickBatchAction(backupBoolean) }
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
                getStats(appInfoList)
            )
            sheetSortFilter?.showNow(requireActivity().supportFragmentManager, "SORTFILTER_SHEET")
        }
        binding.apkBatch.setOnClickListener {
            binding.apkBatch.isChecked = (it as AppCompatCheckBox).isChecked
            // TODO (un)check all apk
            onCheckedApkClicked()
        }
        binding.dataBatch.setOnClickListener {
            binding.dataBatch.isChecked = (it as AppCompatCheckBox).isChecked
            // TODO (un)check all data
            onCheckedDataClicked()
        }
    }

    private fun setupSearch() {
        /*val filterPredicate = { item: BatchItemX, cs: CharSequence? ->
            item.appExtras.customTags
                .plus(item.app.packageName)
                .plus(item.app.packageLabel)
                .find { it.contains(cs.toString(), true) } != null
        }*/
        binding.searchBar.maxWidth = Int.MAX_VALUE
        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                // TODO apply query
                //batchItemAdapter.filter(newText)
                //batchItemAdapter.itemFilter.filterPredicate = filterPredicate
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                // TODO apply query
                //batchItemAdapter.filter(query)
                //batchItemAdapter.itemFilter.filterPredicate = filterPredicate
                return true
            }
        })
    }

    private fun onClickBatchAction(backupBoolean: Boolean) {
        // TODO maintain packages list
        /*val selectedList = batchItemAdapter.adapterItems
            .filter(BatchItemX::isChecked)
            .map { item: BatchItemX -> item.app.appMetaInfo }
            .toCollection(ArrayList())
        val selectedListModes = batchItemAdapter.adapterItems
            .filter(BatchItemX::isChecked)
            .map(BatchItemX::actionMode)
            .toCollection(ArrayList())
        if (selectedList.isNotEmpty()) {
            BatchDialogFragment(backupBoolean, selectedList, selectedListModes, this)
                .show(requireActivity().supportFragmentManager, "DialogFragment")
        }*/
    }

    private fun onCheckedApkClicked() {
        /*val possibleApkCheckedList =
            batchItemAdapter.adapterItems.filter { it.app.hasApk || backupBoolean }
        val checkBoolean = binding.apkBatch.isChecked
        possibleApkCheckedList.forEach {
            val packageName = it.app.packageName
            it.isApkChecked = checkBoolean
            when {
                checkBoolean -> {
                    if (!viewModel.apkCheckedList.contains(packageName))
                        viewModel.apkCheckedList.add(packageName)
                }
                else -> {
                    viewModel.apkCheckedList.remove(packageName)
                }
            }
        }
        batchFastAdapter?.notifyAdapterDataSetChanged()
        updateApkChecks()*/
    }

    private fun onCheckedDataClicked() {
        /*val possibleDataCheckedList =
            batchItemAdapter.itemList.items.filter { it.app.hasAppData || backupBoolean }
        val checkBoolean = binding.dataBatch.isChecked
        possibleDataCheckedList.forEach {
            val packageName = it.app.packageName
            it.isDataChecked = checkBoolean
            when {
                checkBoolean -> {
                    if (!viewModel.dataCheckedList.contains(packageName))
                        viewModel.dataCheckedList.add(packageName)
                }
                else -> {
                    viewModel.dataCheckedList.remove(packageName)
                }
            }
        }
        batchFastAdapter?.notifyAdapterDataSetChanged()
        updateDataChecks()*/
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

    override fun refreshView() {
        Timber.d("refreshing")
        sheetSortFilter = SortFilterSheet(requireActivity().sortFilterModel, getStats(appInfoList))
        Thread {
            try {
                val filteredList =
                    appInfoList.applyFilter(requireActivity().sortFilterModel, requireContext())
                refreshBatch(filteredList)
            } catch (e: FileUtils.BackupLocationInAccessibleException) {
                Timber.e("Could not update application list: $e")
            } catch (e: StorageLocationNotConfiguredException) {
                Timber.e("Could not update application list: $e")
            } catch (e: Throwable) {
                LogsHandler.unhandledException(e)
            }
        }.start()
    }

    private fun refreshBatch(filteredList: List<AppInfo>) {
        //val batchList = createBatchAppsList(filteredList)
        requireActivity().runOnUiThread {
            try {
                binding.recyclerView.setContent {
                    AppTheme(
                        darkTheme = isSystemInDarkTheme()
                    ) {
                        Scaffold {
                            BatchPackageRecycler(
                                productsList = filteredList.filter {
                                    if (backupBoolean) it.isInstalled
                                    else it.hasBackups
                                },
                                !backupBoolean,
                                viewModel.apkCheckedList,
                                viewModel.dataCheckedList,
                                onClick = { item ->
                                    val showApk = when {
                                        item.isSpecial || (!backupBoolean && !item.hasApk) -> false
                                        else -> true
                                    }
                                    val isApkChecked =
                                        viewModel.apkCheckedList.any { it == item.packageName }
                                    val showData = when {
                                        !backupBoolean && !item.hasAppData -> false
                                        else -> true
                                    }
                                    val isDataChecked =
                                        viewModel.dataCheckedList.any { it == item.packageName }
                                    val bothChecked =
                                        (isApkChecked || !showApk) && (isDataChecked || !showData)
                                    if (bothChecked) {
                                        viewModel.apkCheckedList.remove(item.packageName)
                                        viewModel.dataCheckedList.remove(item.packageName)
                                    }
                                    if (!isApkChecked) viewModel.apkCheckedList.add(item.packageName)
                                    if (!isDataChecked) viewModel.dataCheckedList.add(item.packageName)
                                },
                                onApkClick = { item: AppInfo, b: Boolean ->
                                    if (b) viewModel.apkCheckedList.add(item.packageName)
                                    else viewModel.apkCheckedList.remove(item.packageName)
                                },
                                onDataClick = { item: AppInfo, b: Boolean ->
                                    if (b) viewModel.dataCheckedList.add(item.packageName)
                                    else viewModel.dataCheckedList.remove(item.packageName)
                                },
                            )
                        }
                    }
                }
                setupSearch()
                viewModel.refreshNow.value = false
            } catch (e: Throwable) {
                LogsHandler.unhandledException(e)
            }
        }
    }

    /*private fun createBatchAppsList(filteredList: List<AppInfo>): MutableList<BatchItemX> =
        filteredList
            .filter {
                if (backupBoolean) it.isInstalled
                else it.hasBackups
            }.map {
                val item = BatchItemX(it, appExtrasList.get(it.packageName), backupBoolean)
                item.isApkChecked = viewModel.apkCheckedList.contains(it.packageName)
                item.isDataChecked = viewModel.dataCheckedList.contains(it.packageName)
                item
            }.toMutableList()*/

    override fun updateProgress(progress: Int, max: Int) {
        binding.progressBar.visibility = View.VISIBLE
        binding.progressBar.max = max
        binding.progressBar.progress = progress
    }

    override fun hideProgress() {
        binding.progressBar.visibility = View.GONE
    }

    class BackupFragment : BatchFragment(true)
    class RestoreFragment : BatchFragment(false)
}
