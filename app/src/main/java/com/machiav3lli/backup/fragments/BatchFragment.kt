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
import android.content.DialogInterface
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.machiav3lli.backup.MAIN_FILTER_DEFAULT
import com.machiav3lli.backup.R
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.databinding.FragmentBatchBinding
import com.machiav3lli.backup.dialogs.BatchDialogFragment
import com.machiav3lli.backup.dialogs.PackagesListDialogFragment
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.showNotification
import com.machiav3lli.backup.items.ActionResult
import com.machiav3lli.backup.items.AppInfo
import com.machiav3lli.backup.items.BatchItemX
import com.machiav3lli.backup.tasks.AppActionWork
import com.machiav3lli.backup.tasks.FinishWork
import com.machiav3lli.backup.utils.*
import com.machiav3lli.backup.viewmodels.BatchViewModel
import com.machiav3lli.backup.viewmodels.BatchViewModelFactory
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook
import timber.log.Timber

open class BatchFragment(private val backupBoolean: Boolean) : NavigationFragment(),
    BatchDialogFragment.ConfirmListener, RefreshViewController {
    private lateinit var binding: FragmentBatchBinding
    lateinit var viewModel: BatchViewModel

    val batchItemAdapter = ItemAdapter<BatchItemX>()
    private var batchFastAdapter: FastAdapter<BatchItemX>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        binding = FragmentBatchBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        val viewModelFactory = BatchViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(BatchViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        if (requireMainActivity().viewModel.refreshNow.value == true) refreshView()

        viewModel.refreshNow.observe(requireActivity(), {
            binding.refreshLayout.isRefreshing = it
            if (it) {
                binding.searchBar.setQuery("", false)
                requireMainActivity().viewModel.refreshList()
            }
        })
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
        binding.refreshLayout.setColorSchemeColors(requireContext().colorAccent)
        binding.refreshLayout.setProgressBackgroundColorSchemeColor(
            resources.getColor(
                R.color.app_primary_base,
                requireActivity().theme
            )
        )
        binding.refreshLayout.setOnRefreshListener { requireMainActivity().viewModel.refreshList() }
        batchFastAdapter = FastAdapter.with(batchItemAdapter)
        batchFastAdapter?.setHasStableIds(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = batchFastAdapter
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
            sheetSortFilter?.show(requireActivity().supportFragmentManager, "SORTFILTER_SHEET")
        }
        batchFastAdapter?.onClickListener =
            { _: View?, _: IAdapter<BatchItemX>?, item: BatchItemX, _: Int? ->
                val oldChecked = item.isChecked
                item.isApkChecked = !oldChecked && (item.app.hasApk || item.backupBoolean)
                item.isDataChecked = !oldChecked && (item.app.hasAppData || item.backupBoolean)
                if (item.isChecked) {
                    if (!viewModel.apkCheckedList.contains(item.app.packageName) && (item.app.hasApk || item.backupBoolean)) {
                        viewModel.apkCheckedList.add(item.app.packageName)
                    }
                    if (!viewModel.dataCheckedList.contains(item.app.packageName) && (item.app.hasAppData || item.backupBoolean)) {
                        viewModel.dataCheckedList.add(item.app.packageName)
                    }
                } else {
                    viewModel.apkCheckedList.remove(item.app.packageName)
                    viewModel.dataCheckedList.remove(item.app.packageName)
                }
                batchFastAdapter?.notifyAdapterDataSetChanged()
                updateApkChecks()
                updateDataChecks()
                false
            }
        binding.apkBatch.setOnClickListener {
            binding.apkBatch.isChecked = (it as AppCompatCheckBox).isChecked
            onCheckedApkClicked()
        }
        binding.dataBatch.setOnClickListener {
            binding.dataBatch.isChecked = (it as AppCompatCheckBox).isChecked
            onCheckedDataClicked()
        }
        batchFastAdapter?.addEventHook(OnApkCheckBoxClickHook())
        batchFastAdapter?.addEventHook(OnDataCheckBoxClickHook())
        binding.helpButton.setOnClickListener {
            if (requireMainActivity().sheetHelp == null) requireMainActivity().sheetHelp =
                HelpSheet()
            requireMainActivity().sheetHelp!!.showNow(
                requireActivity().supportFragmentManager,
                "HELPSHEET"
            )
        }
    }

    private fun setupSearch() {
        val filterPredicate = { item: BatchItemX, cs: CharSequence? ->
            item.appExtras.customTags
                .plus(item.app.packageName)
                .plus(item.app.packageLabel)
                .find { it.contains(cs.toString(), true) } != null
        }
        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                batchItemAdapter.filter(newText)
                batchItemAdapter.itemFilter.filterPredicate = filterPredicate
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                batchItemAdapter.filter(query)
                batchItemAdapter.itemFilter.filterPredicate = filterPredicate
                return true
            }
        })
    }

    private fun onClickBatchAction(backupBoolean: Boolean) {
        val selectedList = batchItemAdapter.adapterItems
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
        }
    }


    private fun onCheckedApkClicked() {
        val possibleApkCheckedList =
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
        updateApkChecks()
    }

    private fun onCheckedDataClicked() {
        val possibleDataCheckedList =
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
        updateDataChecks()
    }

    inner class OnApkCheckBoxClickHook : ClickEventHook<BatchItemX>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            return viewHolder.itemView.findViewById(R.id.apkCheckbox)
        }

        override fun onClick(
            v: View,
            position: Int,
            fastAdapter: FastAdapter<BatchItemX>,
            item: BatchItemX
        ) {
            item.isApkChecked = !item.isApkChecked
            if (item.isApkChecked && !viewModel.apkCheckedList.contains(item.app.packageName)) {
                viewModel.apkCheckedList.add(item.app.packageName)
            } else {
                viewModel.apkCheckedList.remove(item.app.packageName)
            }
            batchFastAdapter?.notifyAdapterDataSetChanged()
            updateApkChecks()
        }
    }

    inner class OnDataCheckBoxClickHook : ClickEventHook<BatchItemX>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            return viewHolder.itemView.findViewById(R.id.dataCheckbox)
        }

        override fun onClick(
            v: View,
            position: Int,
            fastAdapter: FastAdapter<BatchItemX>,
            item: BatchItemX
        ) {
            item.isDataChecked = !item.isDataChecked
            if (item.isDataChecked && !viewModel.dataCheckedList.contains(item.app.packageName)) {
                viewModel.dataCheckedList.add(item.app.packageName)
            } else {
                viewModel.dataCheckedList.remove(item.app.packageName)
            }
            batchFastAdapter?.notifyAdapterDataSetChanged()
            updateDataChecks()
        }
    }

    // TODO abstract this to fit for Main- & BatchFragment
    // TODO break down to smaller bits
    override fun onConfirmed(selectedPackages: List<String?>, selectedModes: List<Int>) {
        val notificationId = System.currentTimeMillis()
        val notificationMessage = String.format(
            getString(R.string.fetching_action_list),
            getString(if (backupBoolean) R.string.backup else R.string.restore)
        )
        showNotification(
            requireContext(), MainActivityX::class.java, notificationId.toInt(),
            notificationMessage, "", true
        )
        val selectedItems = selectedPackages
            .mapIndexed { i, packageName ->
                if (packageName.isNullOrEmpty()) null
                else Pair(packageName, selectedModes[i])
            }
            .filterNotNull()

        binding.progressBar.visibility = View.VISIBLE
        binding.progressBar.max = selectedItems.size
        var errors = ""
        var resultsSuccess = true
        var counter = 0
        val worksList: MutableList<OneTimeWorkRequest> = mutableListOf()
        selectedItems.forEach { (packageName, mode) ->
            val oneTimeWorkRequest =
                AppActionWork.Request(packageName, mode, backupBoolean, notificationId.toInt())
            worksList.add(oneTimeWorkRequest)

            val oneTimeWorkLiveData = WorkManager.getInstance(requireContext())
                .getWorkInfoByIdLiveData(oneTimeWorkRequest.id)
            oneTimeWorkLiveData.observeForever(object : Observer<WorkInfo> {
                override fun onChanged(t: WorkInfo?) {
                    if (t?.state == WorkInfo.State.SUCCEEDED) {
                        binding.progressBar.progress = counter
                        counter += 1

                        val (succeeded, packageLabel, error) = AppActionWork.getOutput(t)
                        val message = "${
                            getString(if (backupBoolean) R.string.backupProgress else R.string.restoreProgress)
                        } ($counter/${selectedItems.size})"
                        showNotification(
                            requireContext(), MainActivityX::class.java, notificationId.toInt(),
                            message, packageLabel, false
                        )
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

        val finishWorkRequest = FinishWork.Request(resultsSuccess, backupBoolean)

        val finishWorkLiveData = WorkManager.getInstance(requireContext())
            .getWorkInfoByIdLiveData(finishWorkRequest.id)
        finishWorkLiveData.observeForever(object : Observer<WorkInfo> {
            override fun onChanged(t: WorkInfo?) {
                if (t?.state == WorkInfo.State.SUCCEEDED) {
                    val (message, title) = FinishWork.getOutput(t)
                    showNotification(
                        requireContext(), MainActivityX::class.java,
                        notificationId.toInt(), title, message, true
                    )
                    val overAllResult = ActionResult(null, null, errors, resultsSuccess)
                    requireActivity().showActionResult(overAllResult) { _: DialogInterface?, _: Int ->
                        LogsHandler.logErrors(requireContext(), errors.dropLast(2))
                    }

                    binding.progressBar.visibility = View.GONE
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
            } catch (e: FileUtils.BackupLocationIsAccessibleException) {
                Timber.e("Could not update application list: $e")
            } catch (e: StorageLocationNotConfiguredException) {
                Timber.e("Could not update application list: $e")
            } catch (e: Throwable) {
                LogsHandler.unhandledException(e)
            }
        }.start()
    }

    private fun refreshBatch(filteredList: List<AppInfo>) {
        val batchList = createBatchAppsList(filteredList)
        requireActivity().runOnUiThread {
            try {
                batchItemAdapter.set(batchList)
                if (batchList.isEmpty())
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.empty_filtered_list),
                        Toast.LENGTH_SHORT
                    ).show()
                setupSearch()
                updateApkChecks()
                updateDataChecks()
                viewModel.refreshNow.value = false
            } catch (e: Throwable) {
                LogsHandler.unhandledException(e)
            }
        }
    }

    private fun createBatchAppsList(filteredList: List<AppInfo>): MutableList<BatchItemX> =
        filteredList
            .filter {
                if (backupBoolean) it.isInstalled
                else it.hasBackups
            }.map {
                val item = BatchItemX(it, appExtrasList.get(it.packageName), backupBoolean)
                item.isApkChecked = viewModel.apkCheckedList.contains(it.packageName)
                item.isDataChecked = viewModel.dataCheckedList.contains(it.packageName)
                item
            }.toMutableList()

    private fun updateApkChecks() {
        val possibleApkChecked: Int =
            batchItemAdapter.itemList.items.filter { it.app.hasApk || backupBoolean }.size
        binding.apkBatch.isChecked = viewModel.apkCheckedList.size == possibleApkChecked
    }

    private fun updateDataChecks() {
        val possibleDataChecked: Int =
            batchItemAdapter.itemList.items.filter { it.app.hasAppData || backupBoolean }.size
        binding.dataBatch.isChecked = viewModel.dataCheckedList.size == possibleDataChecked
    }

    class BackupFragment : BatchFragment(true)
    class RestoreFragment : BatchFragment(false)
}