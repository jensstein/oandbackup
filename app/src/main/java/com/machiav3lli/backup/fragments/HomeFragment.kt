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

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.machiav3lli.backup.*
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.databinding.FragmentHomeBinding
import com.machiav3lli.backup.dialogs.BatchDialogFragment
import com.machiav3lli.backup.dialogs.PackagesListDialogFragment
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.showNotification
import com.machiav3lli.backup.items.ActionResult
import com.machiav3lli.backup.items.AppInfo
import com.machiav3lli.backup.items.MainItemX
import com.machiav3lli.backup.items.UpdatedItemX
import com.machiav3lli.backup.tasks.AppActionWork
import com.machiav3lli.backup.tasks.FinishWork
import com.machiav3lli.backup.utils.*
import com.machiav3lli.backup.viewmodels.HomeViewModel
import com.machiav3lli.backup.viewmodels.HomeViewModelFactory
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import timber.log.Timber

class HomeFragment : NavigationFragment(),
    BatchDialogFragment.ConfirmListener, RefreshViewController {
    private lateinit var binding: FragmentHomeBinding
    lateinit var viewModel: HomeViewModel
    private var appSheet: AppSheet? = null

    val mainItemAdapter = ItemAdapter<MainItemX>()
    private var mainFastAdapter: FastAdapter<MainItemX>? = null
    private val updatedItemAdapter = ItemAdapter<UpdatedItemX>()
    private var updatedFastAdapter: FastAdapter<UpdatedItemX>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        val viewModelFactory = HomeViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(HomeViewModel::class.java)
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

        viewModel.nUpdatedApps.observe(requireActivity(), {
            binding.buttonUpdated.text =
                binding.root.context.resources.getQuantityString(R.plurals.updated_apps, it, it)
            if (it > 0) {
                binding.buttonUpdated.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ic_arrow_up,
                    0,
                    0,
                    0
                )
                binding.buttonUpdated.setOnClickListener {
                    binding.updatedBar.visibility = when (binding.updatedBar.visibility) {
                        View.VISIBLE -> {
                            binding.buttonUpdated.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                R.drawable.ic_arrow_up,
                                0,
                                0,
                                0
                            )
                            View.GONE
                        }
                        else -> {
                            binding.buttonUpdated.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                R.drawable.ic_arrow_down,
                                0,
                                0,
                                0
                            )
                            View.VISIBLE
                        }
                    }
                }
            } else {
                binding.updatedBar.visibility = View.GONE
                binding.buttonUpdated.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                binding.buttonUpdated.setOnClickListener(null)
            }
        })
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
        binding.refreshLayout.setColorSchemeColors(requireContext().colorAccent)
        binding.refreshLayout.setProgressBackgroundColorSchemeColor(
            resources.getColor(R.color.app_primary_base, requireActivity().theme)
        )
        binding.refreshLayout.setOnRefreshListener { requireMainActivity().viewModel.refreshList() }
        mainFastAdapter = FastAdapter.with(mainItemAdapter)
        updatedFastAdapter = FastAdapter.with(updatedItemAdapter)
        mainFastAdapter?.setHasStableIds(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = mainFastAdapter
        updatedFastAdapter?.setHasStableIds(true)
        binding.updatedRecycler.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        binding.updatedRecycler.adapter = updatedFastAdapter
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
        mainFastAdapter?.onClickListener =
            { _: View?, _: IAdapter<MainItemX>?, item: MainItemX?, position: Int? ->
                if (appSheet != null) appSheet?.dismissAllowingStateLoss()
                item?.let {
                    appSheet = AppSheet(item.app, item.appExtras, position ?: -1)
                    appSheet?.showNow(requireActivity().supportFragmentManager, "APP_SHEET")
                }
                false
            }
        updatedFastAdapter?.onClickListener =
            { _: View?, _: IAdapter<UpdatedItemX>?, item: UpdatedItemX?, position: Int? ->
                if (appSheet != null) appSheet?.dismissAllowingStateLoss()
                item?.let {
                    appSheet = AppSheet(item.app, item.appExtras, position ?: -1)
                    appSheet?.showNow(requireActivity().supportFragmentManager, "APP_SHEET")
                }
                false
            }
        binding.updateAllAction.setOnClickListener { onClickUpdateAllAction() }
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
        val filterPredicate = { item: MainItemX, cs: CharSequence? ->
            item.appExtras.customTags
                .plus(item.app.packageName)
                .plus(item.app.packageLabel)
                .find { it.contains(cs.toString(), true) } != null
        }
        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                mainItemAdapter.filter(newText)
                mainItemAdapter.itemFilter.filterPredicate = filterPredicate
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                mainItemAdapter.filter(query)
                mainItemAdapter.itemFilter.filterPredicate = filterPredicate
                return true
            }
        })
    }

    private fun onClickUpdateAllAction() {
        val selectedList = updatedItemAdapter.adapterItems
            .map { it.app.appMetaInfo }
            .toCollection(ArrayList())
        val selectedListModes = updatedItemAdapter.adapterItems
            .mapNotNull {
                it.app.latestBackup?.backupProperties?.let { bp ->
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

    // TODO abstract this to fit for Main- & BatchFragment
    // TODO break down to smaller bits
    override fun onConfirmed(
        selectedPackages: List<String?>,
        selectedModes: List<Int>
    ) {
        val notificationId = System.currentTimeMillis()
        val notificationMessage = String.format(
            getString(R.string.fetching_action_list),
            getString(R.string.backup)
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
                AppActionWork.Request(packageName, mode, true, notificationId.toInt())
            worksList.add(oneTimeWorkRequest)

            val oneTimeWorkLiveData = WorkManager.getInstance(requireContext())
                .getWorkInfoByIdLiveData(oneTimeWorkRequest.id)
            oneTimeWorkLiveData.observeForever(object : Observer<WorkInfo> {
                override fun onChanged(t: WorkInfo?) {
                    if (t?.state == WorkInfo.State.SUCCEEDED) {
                        binding.progressBar.progress = counter
                        counter += 1

                        val (succeeded, packageLabel, error) = AppActionWork.getOutput(t)
                        val message =
                            "${getString(R.string.backupProgress)} ($counter/${selectedItems.size})"
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

        val finishWorkRequest = FinishWork.Request(resultsSuccess, true)

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
            } catch (e: FileUtils.BackupLocationIsAccessibleException) {
                Timber.e("Could not update application list: $e")
            } catch (e: StorageLocationNotConfiguredException) {
                Timber.e("Could not update application list: $e")
            } catch (e: Throwable) {
                LogsHandler.unhandledException(e)
            }
        }.start()
    }

    private fun refreshMain(filteredList: List<AppInfo>, appSheetBoolean: Boolean) {
        val mainList = createMainAppsList(filteredList)
        val updatedList = createUpdatedAppsList(filteredList)
        requireActivity().runOnUiThread {
            try {
                mainItemAdapter.set(mainList)
                updatedItemAdapter.set(updatedList)
                viewModel.nUpdatedApps.value = updatedList.size
                if (mainList.isEmpty())
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.empty_filtered_list),
                        Toast.LENGTH_SHORT
                    ).show()
                setupSearch()
                if (appSheetBoolean) refreshAppSheet()
                viewModel.refreshNow.value = false
            } catch (e: Throwable) {
                LogsHandler.unhandledException(e)
            }
        }
    }

    private fun createMainAppsList(filteredList: List<AppInfo>): MutableList<MainItemX> =
        filteredList
            .map { MainItemX(it, appExtrasList.get(it.packageName)) }.toMutableList()

    private fun createUpdatedAppsList(filteredList: List<AppInfo>): MutableList<UpdatedItemX> =
        filteredList
            .filter { it.isUpdated }
            .map { UpdatedItemX(it, appExtrasList.get(it.packageName)) }.toMutableList()

    private fun refreshAppSheet() {
        try {
            val position = appSheet?.position ?: -1
            if (mainItemAdapter.itemList.size() > position && position != -1) {
                val sheetAppInfo = mainFastAdapter?.getItem(position)?.app
                sheetAppInfo?.let {
                    if (appSheet?.packageName == sheetAppInfo.packageName) {
                        mainFastAdapter?.getItem(position)?.let { appSheet?.updateApp(it) }
                    } else
                        appSheet?.dismissAllowingStateLoss()
                }
            } else
                appSheet?.dismissAllowingStateLoss()
        } catch (e: Throwable) {
            LogsHandler.unhandledException(e)
        }
    }
}