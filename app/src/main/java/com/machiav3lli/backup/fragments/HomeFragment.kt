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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Scaffold
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.machiav3lli.backup.*
import com.machiav3lli.backup.databinding.FragmentHomeBinding
import com.machiav3lli.backup.dbs.entity.AppExtras
import com.machiav3lli.backup.dialogs.BatchDialogFragment
import com.machiav3lli.backup.dialogs.PackagesListDialogFragment
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.WorkHandler
import com.machiav3lli.backup.items.AppInfo
import com.machiav3lli.backup.tasks.AppActionWork
import com.machiav3lli.backup.tasks.FinishWork
import com.machiav3lli.backup.ui.compose.recycler.HomePackageRecycler
import com.machiav3lli.backup.ui.compose.recycler.UpdatedPackageRecycler
import com.machiav3lli.backup.ui.compose.theme.AppTheme
import com.machiav3lli.backup.utils.*
import com.machiav3lli.backup.viewmodels.HomeViewModel
import timber.log.Timber

class HomeFragment : NavigationFragment(),
    BatchDialogFragment.ConfirmListener, RefreshViewController {
    private lateinit var binding: FragmentHomeBinding
    lateinit var viewModel: HomeViewModel
    private var appSheet: AppSheet? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        val viewModelFactory = HomeViewModel.Factory(requireActivity().application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(HomeViewModel::class.java)
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

        viewModel.updatedApps.observe(viewLifecycleOwner) {
            viewModel.nUpdatedApps.value = it.size
            binding.updatedRecycler.setContent {
                AppTheme(
                    darkTheme = isSystemInDarkTheme()
                ) {
                    UpdatedPackageRecycler(productsList = it,
                        onClick = { item ->
                            if (appSheet != null) appSheet?.dismissAllowingStateLoss()
                            appSheet = AppSheet(item, AppExtras(), it.indexOf(item))
                            appSheet?.showNow(
                                parentFragmentManager,
                                "Package ${item.packageName}"
                            )
                        }
                    )
                }
            }
        }
        viewModel.nUpdatedApps.observe(requireActivity()) {
            binding.buttonUpdated.text =
                binding.root.context.resources.getQuantityString(R.plurals.updated_apps, it, it)
            if (it > 0) {
                binding.updatedBar.visibility = View.VISIBLE
                binding.buttonUpdated.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_arrow_up,
                    0
                )
                binding.buttonUpdateAll.visibility = View.VISIBLE
                binding.buttonUpdated.setOnClickListener {
                    binding.updatedRecycler.visibility = when (binding.updatedRecycler.visibility) {
                        View.VISIBLE -> {
                            binding.buttonUpdated.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.ic_arrow_up,
                                0
                            )
                            View.GONE
                        }
                        else -> {
                            binding.buttonUpdated.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.ic_arrow_down,
                                0
                            )
                            View.VISIBLE
                        }
                    }
                }
            } else {
                binding.updatedBar.visibility = View.GONE
                binding.updatedRecycler.visibility = View.GONE
                binding.buttonUpdateAll.visibility = View.GONE
                binding.buttonUpdated.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                binding.buttonUpdated.setOnClickListener(null)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        binding.pageHeadline.text = resources.getText(R.string.main)
    }

    override fun onResume() {
        super.onResume()
        setupOnClicks()
        setupSearch()
        requireMainActivity().setRefreshViewController(this)
    }

    override fun setupViews() {
        /*binding.refreshLayout.setColorSchemeColors(requireContext().colorAccent)
        binding.refreshLayout.setProgressBackgroundColorSchemeColor(
            resources.getColor(R.color.app_primary_base, requireActivity().theme)
        )
        binding.refreshLayout.setProgressViewOffset(false, 72, 144)
        binding.refreshLayout.setOnRefreshListener { requireMainActivity().viewModel.refreshList() }*/
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
            if (sheetSortFilter != null && sheetSortFilter!!.isVisible) sheetSortFilter?.dismissAllowingStateLoss()
            sheetSortFilter = SortFilterSheet(
                requireActivity().sortFilterModel,
                getStats(appInfoList)
            )
            sheetSortFilter?.showNow(requireActivity().supportFragmentManager, "SORTFILTER_SHEET")
        }
        binding.buttonUpdateAll.setOnClickListener { onClickUpdateAllAction() }
    }

    private fun setupSearch() {
        /*val filterPredicate = { item: HomeItemX, cs: CharSequence? ->
            item.appExtras.customTags
                .plus(item.app.packageName)
                .plus(item.app.packageLabel)
                .find { it.contains(cs.toString(), true) } != null
        }*/
        binding.searchBar.maxWidth = Int.MAX_VALUE
        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                // TODO apply query
                //homeItemAdapter.filter(newText)
                //homeItemAdapter.itemFilter.filterPredicate = filterPredicate
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                // TODO apply query
                //homeItemAdapter.filter(query)
                //homeItemAdapter.itemFilter.filterPredicate = filterPredicate
                return true
            }
        })
    }

    private fun onClickUpdateAllAction() {
        val selectedList = viewModel.updatedApps.value
            ?.map { it.appMetaInfo }
            ?.toCollection(ArrayList()) ?: arrayListOf()
        val selectedListModes = viewModel.updatedApps.value
            ?.mapNotNull {
                it.latestBackup?.backupProperties?.let { bp ->
                    when {
                        bp.hasApk && bp.hasAppData -> ALT_MODE_BOTH
                        bp.hasApk -> ALT_MODE_APK
                        bp.hasAppData -> ALT_MODE_DATA
                        else -> ALT_MODE_UNSET
                    }
                }
            }
            ?.toCollection(ArrayList()) ?: arrayListOf()
        if (selectedList.isNotEmpty()) {
            BatchDialogFragment(true, selectedList, selectedListModes, this)
                .show(requireActivity().supportFragmentManager, "DialogFragment")
        }
    }

    // TODO abstract this to fit for Main- & BatchFragment
    override fun onConfirmed(
        selectedPackages: List<String?>,
        selectedModes: List<Int>
    ) {
        val now = System.currentTimeMillis()
        val notificationId = now.toInt()
        val batchType = getString(R.string.backup)
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
        val workManager = WorkManager.getInstance(requireContext())
        OABX.work.beginBatch(batchName)
        selectedItems.forEach { (packageName, mode) ->

            val oneTimeWorkRequest =
                AppActionWork.Request(packageName, mode, true, notificationId, batchName)
            worksList.add(oneTimeWorkRequest)

            val oneTimeWorkLiveData = WorkManager.getInstance(requireContext())
                .getWorkInfoByIdLiveData(oneTimeWorkRequest.id)
            oneTimeWorkLiveData.observeForever(object : Observer<WorkInfo> {
                override fun onChanged(t: WorkInfo?) {
                    if (t?.state == WorkInfo.State.SUCCEEDED) {
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

        val finishWorkRequest = FinishWork.Request(resultsSuccess, true, batchName)

        /*
        val finishWorkLiveData = WorkManager.getInstance(requireContext())
            .getWorkInfoByIdLiveData(finishWorkRequest.id)
        finishWorkLiveData.observeForever(object : Observer<WorkInfo> {
            override fun onChanged(t: WorkInfo?) {
                if (t?.state == WorkInfo.State.SUCCEEDED) {
                    val (message, title) = FinishWork.getOutput(t)
                    showNotification(
                        requireContext(), MainActivityX::class.java,
                        notificationId, title, message, true
                    )
                    val overAllResult = ActionResult(null, null, errors, resultsSuccess)
                    requireActivity().showActionResult(overAllResult) { _: DialogInterface?, _: Int ->
                        LogsHandler.logErrors(requireContext(), errors.dropLast(2))
                    }

                    finishWorkLiveData.removeObserver(this)
                    viewModel.refreshNow.value = true
                }
            }
        })
        */

        if (worksList.isNotEmpty()) {
            workManager
                .beginWith(worksList)
                .then(finishWorkRequest)
                .enqueue()
        }
    }

    override fun refreshView() {
        Timber.d("refreshing")
        sheetSortFilter = SortFilterSheet(
            requireActivity().sortFilterModel, getStats(
                appInfoList
            )
        )
        Thread {
            try {
                val filteredList =
                    appInfoList.applyFilter(requireActivity().sortFilterModel, requireContext())
                refreshMain(filteredList, appSheet != null)
            } catch (e: FileUtils.BackupLocationInAccessibleException) {
                Timber.e("Could not update application list: $e")
            } catch (e: StorageLocationNotConfiguredException) {
                Timber.e("Could not update application list: $e")
            } catch (e: Throwable) {
                LogsHandler.unhandledException(e)
            }
        }.start()
    }

    private fun refreshMain(filteredList: List<AppInfo>, appSheetBoolean: Boolean) {
        //val mainList = createMainAppsList(filteredList)
        //val updatedList = createUpdatedAppsList(filteredList)
        requireActivity().runOnUiThread {
            try {
                viewModel.updatedApps.value = filteredList.filter { it.isUpdated }
                binding.recyclerView.setContent {
                    AppTheme(
                        darkTheme = isSystemInDarkTheme()
                    ) {
                        Scaffold {
                            HomePackageRecycler(productsList = filteredList,
                                onClick = { item ->
                                    if (appSheet != null) appSheet?.dismissAllowingStateLoss()
                                    appSheet =
                                        AppSheet(item, AppExtras(), filteredList.indexOf(item))
                                    appSheet?.showNow(
                                        parentFragmentManager,
                                        "Package ${item.packageName}"
                                    )
                                }
                            )
                        }
                    }
                }
                setupSearch()
                if (appSheetBoolean) refreshAppSheet()
                viewModel.refreshNow.value = false
            } catch (e: Throwable) {
                LogsHandler.unhandledException(e)
            }
        }
    }

    private fun refreshAppSheet() {
        try {
            val position = appSheet?.position ?: -1
            // TODO implement auto refresh of AppSheet
            /*if (homeItemAdapter.itemList.size() > position && position != -1) {
                val sheetAppInfo = homeFastAdapter?.getItem(position)?.app
                sheetAppInfo?.let {
                    if (appSheet?.packageName == sheetAppInfo.packageName) {
                        homeFastAdapter?.getItem(position)?.let { appSheet?.updateApp(it) }
                    } else
                        appSheet?.dismissAllowingStateLoss()
                }
            } else
                appSheet?.dismissAllowingStateLoss()*/
        } catch (e: Throwable) {
            appSheet?.dismissAllowingStateLoss()
            //LogsHandler.unhandledException(e)
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
}
