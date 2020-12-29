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

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.PowerManager
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.google.android.material.badge.BadgeDrawable
import com.machiav3lli.backup.*
import com.machiav3lli.backup.R
import com.machiav3lli.backup.databinding.ActivityMainXBinding
import com.machiav3lli.backup.dialogs.BatchDialogFragment
import com.machiav3lli.backup.fragments.AppSheet
import com.machiav3lli.backup.fragments.HelpSheet
import com.machiav3lli.backup.fragments.SortFilterSheet
import com.machiav3lli.backup.handler.NotificationHandler
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.handler.SortFilterManager.applyFilter
import com.machiav3lli.backup.items.*
import com.machiav3lli.backup.tasks.AppActionWork
import com.machiav3lli.backup.tasks.FinishWork
import com.machiav3lli.backup.utils.*
import com.machiav3lli.backup.utils.FileUtils.BackupLocationIsAccessibleException
import com.machiav3lli.backup.viewmodels.MainViewModel
import com.machiav3lli.backup.viewmodels.MainViewModelFactory
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import com.mikepenz.fastadapter.listeners.ClickEventHook
import com.topjohnwu.superuser.Shell
import timber.log.Timber

class MainActivityX : BaseActivity(), BatchDialogFragment.ConfirmListener, SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        var shellHandlerInstance: ShellHandler? = null
            private set

        fun initShellHandler(): Boolean {
            shellHandlerInstance = ShellHandler()
            return true
        }

        init {
            /*
            * Shell.Config methods shall be called before any shell is created
            * This is the why in this example we call it in a static block
            * The followings are some examples, check Javadoc for more details
            */
            Shell.enableVerboseLogging = BuildConfig.DEBUG
            Shell.setDefaultBuilder(Shell.Builder.create()
                    .setTimeout(20))
        }
    }

    private var updatedBadge: BadgeDrawable? = null
    private lateinit var prefs: SharedPreferences
    private var navController: NavController? = null
    private var powerManager: PowerManager? = null
    private var searchViewController: SearchViewController? = null

    private lateinit var binding: ActivityMainXBinding
    private lateinit var viewModel: MainViewModel
    val mainItemAdapter = ItemAdapter<MainItemX>()
    private var mainFastAdapter: FastAdapter<MainItemX>? = null
    val batchItemAdapter = ItemAdapter<BatchItemX>()
    private var batchFastAdapter: FastAdapter<BatchItemX>? = null
    private var mainBoolean = false
    private var backupBoolean = false
    private var sheetSortFilter: SortFilterSheet? = null
    private var sheetApp: AppSheet? = null
    var sheetHelp: HelpSheet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainXBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        setContentView(binding.root)
        powerManager = getSystemService(POWER_SERVICE) as PowerManager
        prefs = getPrivateSharedPrefs(this)
        prefs.registerOnSharedPreferenceChangeListener(this)
        val viewModelFactory = MainViewModelFactory(this, application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
        if (!isRememberFiltering(this)) saveFilterPreferences(this, SortFilterModel())
        viewModel.refreshActive.observe(this, {
            binding.refreshLayout.isRefreshing = it
            if (it) searchViewController?.clean()
            else if (mainBoolean) {
                mainItemAdapter.adapterItems.forEach { item ->
                    if (item.app.hasBackups && item.app.isUpdated)
                        viewModel.badgeCounter.value = viewModel.badgeCounter.value?.plus(1)
                }
            }
        })
        viewModel.refreshNow.observe(this, {
            if (it) {
                refreshView()
            }
        })
        viewModel.badgeCounter.observe(this, {
            updatedBadge?.isVisible = it != 0
            updatedBadge?.number = it
        })
        checkUtilBox()
        runOnUiThread { showEncryptionDialog() }
        setupViews()
    }

    override fun onStart() {
        super.onStart()
        setupOnClicks()
        setupNavigation()
    }

    override fun onBackPressed() {
        finishAffinity()
    }

    fun onResumeFragment() {
        if (viewModel.initial.value != true) refreshView()
        else {
            viewModel.refreshList()
            setNeedRefresh(this, false)
        }
    }

    fun setSearchViewController(searchViewController: SearchViewController?) {
        this.searchViewController = searchViewController
    }

    private fun setupViews() {
        binding.cbAll.isChecked = false
        binding.refreshLayout.setColorSchemeColors(resources.getColor(R.color.app_accent, theme))
        binding.refreshLayout.setProgressBackgroundColorSchemeColor(resources.getColor(R.color.app_primary_base, theme))
        binding.refreshLayout.setOnRefreshListener { viewModel.refreshList() }
        binding.bottomNavigation.getOrCreateBadge(R.id.mainFragment)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        updatedBadge = binding.bottomNavigation.getOrCreateBadge(R.id.mainFragment)
        updatedBadge?.backgroundColor = resources.getColor(R.color.app_accent, theme)
        mainFastAdapter = FastAdapter.with(mainItemAdapter)
        batchFastAdapter = FastAdapter.with(batchItemAdapter)
        mainFastAdapter?.setHasStableIds(true)
        batchFastAdapter?.setHasStableIds(true)
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNavigation.setOnNavigationItemSelectedListener { item: MenuItem ->
            if (item.itemId == binding.bottomNavigation.selectedItemId) return@setOnNavigationItemSelectedListener false
            navController?.navigate(item.itemId)
            true
        }
        navController?.addOnDestinationChangedListener { _: NavController?, destination: NavDestination, _: Bundle? ->
            when (destination.id) {
                R.id.mainFragment -> navigateMain()
                R.id.backupFragment -> {
                    backupBoolean = true
                    navigateBatch()
                }
                R.id.restoreFragment -> {
                    backupBoolean = false
                    navigateBatch()
                }
            }
        }
    }

    private fun navigateMain() {
        mainBoolean = true
        binding.batchBar.visibility = View.GONE
        binding.modeBar.visibility = View.GONE
        binding.recyclerView.adapter = mainFastAdapter
    }

    private fun navigateBatch() {
        mainBoolean = false
        binding.batchBar.visibility = View.VISIBLE
        binding.modeBar.visibility = View.VISIBLE
        binding.buttonAction.setText(if (backupBoolean) R.string.backup else R.string.restore)
        binding.recyclerView.adapter = batchFastAdapter
        binding.buttonAction.setOnClickListener { onClickBatchAction(backupBoolean) }
        viewModel.dataCheckedList.clear()
        viewModel.apkCheckedList.clear()
    }

    private fun setupOnClicks() {
        binding.buttonSettings.setOnClickListener { startActivity(Intent(applicationContext, PrefsActivity::class.java)) }
        binding.buttonScheduler.setOnClickListener { startActivity(Intent(applicationContext, SchedulerActivityX::class.java)) }
        binding.cbAll.setOnClickListener {
            binding.cbAll.isChecked = (it as AppCompatCheckBox).isChecked
            onCheckAllChanged()
        }
        binding.buttonSortFilter.setOnClickListener {
            if (sheetSortFilter == null) sheetSortFilter = SortFilterSheet(SortFilterModel(
                    getFilterPreferences(this).toString()),
                    getStats(viewModel.appInfoList.value ?: mutableListOf())
            )
            sheetSortFilter?.show(supportFragmentManager, "SORTFILTER_SHEET")
        }
        mainFastAdapter?.onClickListener = { _: View?, _: IAdapter<MainItemX>?, item: MainItemX?, position: Int? ->
            if (sheetApp != null) sheetApp?.dismissAllowingStateLoss()
            item?.let {
                sheetApp = AppSheet(item, position ?: -1)
                sheetApp?.showNow(supportFragmentManager, "APP_SHEET")
            }
            false
        }
        batchFastAdapter?.onClickListener = { _: View?, _: IAdapter<BatchItemX>?, item: BatchItemX, _: Int? ->
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
            updateCheckAll()
            false
        }
        binding.apkBatch.setOnClickListener { onCheckedApkClicked() }
        binding.dataBatch.setOnClickListener { onCheckedDataClicked() }
        batchFastAdapter?.addEventHook(OnApkCheckBoxClickHook())
        batchFastAdapter?.addEventHook(OnDataCheckBoxClickHook())
    }

    private fun onCheckAllChanged() {
        batchItemAdapter.adapterItems.forEach { item ->
            val apbEligible = item.app.hasApk || backupBoolean
            val dataEligible = item.app.hasAppData || backupBoolean
            if (apbEligible) item.isApkChecked = binding.cbAll.isChecked
            if (dataEligible) item.isDataChecked = binding.cbAll.isChecked
            when {
                binding.cbAll.isChecked -> {
                    if (!viewModel.apkCheckedList.contains(item.app.packageName) && apbEligible)
                        viewModel.apkCheckedList.add(item.app.packageName)
                    if (!viewModel.dataCheckedList.contains(item.app.packageName) && dataEligible)
                        viewModel.dataCheckedList.add(item.app.packageName)
                }
                else -> {
                    viewModel.apkCheckedList.remove(item.app.packageName)
                    viewModel.dataCheckedList.remove(item.app.packageName)
                }
            }
        }
        batchFastAdapter?.notifyAdapterDataSetChanged()
    }

    private fun onCheckedApkClicked() {
        val possibleApkCheckedList = batchItemAdapter.adapterItems.filter { it.app.hasApk || backupBoolean }
        val checkBoolean = viewModel.apkCheckedList.size != possibleApkCheckedList.size
        viewModel.apkCheckedList.clear()
        possibleApkCheckedList.forEach {
            val packageName = it.app.packageName
            it.isApkChecked = checkBoolean
            if (checkBoolean) viewModel.apkCheckedList.add(packageName)
        }
        batchFastAdapter?.notifyAdapterDataSetChanged()
        updateCheckAll()
    }

    private fun onCheckedDataClicked() {
        val possibleDataCheckedList = batchItemAdapter.itemList.items.filter { it.app.hasAppData || backupBoolean }
        val checkBoolean = viewModel.dataCheckedList.size != possibleDataCheckedList.size
        viewModel.dataCheckedList.clear()
        possibleDataCheckedList.forEach {
            val packageName = it.app.packageName
            it.isDataChecked = checkBoolean
            if (checkBoolean) viewModel.dataCheckedList.add(packageName)
        }
        batchFastAdapter?.notifyAdapterDataSetChanged()
        updateCheckAll()
    }

    private fun updateCheckAll() {
        val possibleApkChecked: Int = batchItemAdapter.itemList.items.filter { it.app.hasApk || backupBoolean }.size
        val possibleDataChecked: Int = batchItemAdapter.itemList.items.filter { it.app.hasAppData || backupBoolean }.size
        binding.cbAll.isChecked = viewModel.apkCheckedList.size == possibleApkChecked && viewModel.dataCheckedList.size == possibleDataChecked
    }

    inner class OnApkCheckBoxClickHook : ClickEventHook<BatchItemX>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            return viewHolder.itemView.findViewById(R.id.apkCheckbox)
        }

        override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<BatchItemX>, item: BatchItemX) {
            item.isApkChecked = !item.isApkChecked
            if (item.isApkChecked && !viewModel.apkCheckedList.contains(item.app.packageName)) {
                viewModel.apkCheckedList.add(item.app.packageName)
            } else {
                viewModel.apkCheckedList.remove(item.app.packageName)
            }
            batchFastAdapter?.notifyAdapterDataSetChanged()
            updateCheckAll()
        }
    }

    inner class OnDataCheckBoxClickHook : ClickEventHook<BatchItemX>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            return viewHolder.itemView.findViewById(R.id.dataCheckbox)
        }

        override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<BatchItemX>, item: BatchItemX) {
            item.isDataChecked = !item.isDataChecked
            if (item.isDataChecked && !viewModel.dataCheckedList.contains(item.app.packageName)) {
                viewModel.dataCheckedList.add(item.app.packageName)
            } else {
                viewModel.dataCheckedList.remove(item.app.packageName)
            }
            batchFastAdapter?.notifyAdapterDataSetChanged()
            updateCheckAll()
        }
    }

    private fun showEncryptionDialog() {
        val defPrefs = getDefaultSharedPreferences(this)
        val dontShowAgain = defPrefs.getBoolean(PREFS_ENCRYPTION, false) && (defPrefs.getString(PREFS_PASSWORD, "")
                ?: "").isNotEmpty()
        if (dontShowAgain) return
        val dontShowCounter = prefs.getInt(PREFS_SKIPPEDENCRYPTION, 0)
        prefs.edit().putInt(PREFS_SKIPPEDENCRYPTION, dontShowCounter + 1).apply()
        if (dontShowCounter % 10 == 0) {
            AlertDialog.Builder(this)
                    .setTitle(R.string.enable_encryption_title)
                    .setMessage(R.string.enable_encryption_message)
                    .setPositiveButton(R.string.dialog_approve) { _: DialogInterface?, _: Int -> startActivity(Intent(applicationContext, PrefsActivity::class.java).putExtra(".toEncryption", true)) }
                    .show()
        }
    }

    private fun onClickBatchAction(backupBoolean: Boolean) {
        val arguments = Bundle()
        val selectedList = batchItemAdapter.adapterItems
                .filter(BatchItemX::isChecked)
                .map { item: BatchItemX -> item.app.appMetaInfo }
                .toCollection(ArrayList())
        arguments.putParcelableArrayList("selectedList", selectedList)
        val selectedListModes = batchItemAdapter.adapterItems
                .filter(BatchItemX::isChecked)
                .map(BatchItemX::actionMode)
                .toCollection(ArrayList())
        arguments.putIntegerArrayList("selectedListModes", selectedListModes)
        arguments.putBoolean("backupBoolean", backupBoolean)
        val dialog = BatchDialogFragment(this)
        dialog.arguments = arguments
        dialog.show(supportFragmentManager, "DialogFragment")
    }

    override fun onConfirmed(selectedPackages: List<String?>, selectedModes: List<Int>) {
        val notificationId = System.currentTimeMillis()
        val backupBoolean = backupBoolean  // use a copy because the variable can change while running this task
        val notificationMessage = String.format(getString(R.string.fetching_action_list),
                (if (backupBoolean) getString(R.string.backup) else getString(R.string.restore)))
        NotificationHandler.showNotification(this, MainActivityX::class.java,
                notificationId.toInt(), notificationMessage, "", true)
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
            val oneTimeWorkRequest = OneTimeWorkRequest.Builder(AppActionWork::class.java)
                    .setInputData(workDataOf(
                            "packageName" to packageName,
                            "selectedMode" to mode,
                            "backupBoolean" to backupBoolean,
                            "notificationId" to notificationId.toInt()
                    ))
                    .build()

            worksList.add(oneTimeWorkRequest)

            val oneTimeWorkLiveData = WorkManager.getInstance(this)
                    .getWorkInfoByIdLiveData(oneTimeWorkRequest.id)
            oneTimeWorkLiveData.observeForever(object : Observer<WorkInfo> {
                override fun onChanged(t: WorkInfo?) {
                    if (t?.state == WorkInfo.State.SUCCEEDED) {
                        binding.progressBar.progress = counter
                        counter += 1
                        val succeeded = t.outputData.getBoolean("succeeded", false)
                        val packageLabel = t.outputData.getString("packageLabel")
                                ?: ""
                        val error = t.outputData.getString("error")
                                ?: ""
                        val message = "${if (backupBoolean) getString(R.string.backupProgress) else getString(R.string.restoreProgress)} ($counter/${selectedItems.size})"
                        NotificationHandler.showNotification(this@MainActivityX, MainActivityX::class.java, notificationId.toInt(), message, packageLabel, false)
                        if (error.isNotEmpty()) errors = "$errors$packageLabel: $error\n"
                        resultsSuccess = resultsSuccess && succeeded
                        oneTimeWorkLiveData.removeObserver(this)
                    }
                }
            })
        }

        val finishWorkRequest = OneTimeWorkRequest.Builder(FinishWork::class.java)
                .setInputData(workDataOf(
                        "resultsSuccess" to resultsSuccess,
                        "backupBoolean" to backupBoolean
                ))
                .build()

        val finishWorkLiveData = WorkManager.getInstance(this)
                .getWorkInfoByIdLiveData(finishWorkRequest.id)
        finishWorkLiveData.observeForever(object : Observer<WorkInfo> {
            override fun onChanged(t: WorkInfo?) {
                if (t?.state == WorkInfo.State.SUCCEEDED) {
                    val message = t.outputData.getString("notificationMessage")
                            ?: ""
                    val title = t.outputData.getString("notificationTitle")
                            ?: ""
                    NotificationHandler.showNotification(this@MainActivityX, MainActivityX::class.java,
                            notificationId.toInt(), title, message, true)

                    val overAllResult = ActionResult(null, null, errors, resultsSuccess)
                    showActionResult(this@MainActivityX, overAllResult,
                            if (overAllResult.succeeded) null
                            else DialogInterface.OnClickListener { _: DialogInterface?, _: Int ->
                                LogUtils.logErrors(this@MainActivityX, errors.dropLast(2))
                            }
                    )
                    binding.progressBar.visibility = View.GONE
                    viewModel.refreshList()
                    finishWorkLiveData.removeObserver(this)
                }
            }
        })

        WorkManager.getInstance(this)
                .beginWith(worksList)
                .then(finishWorkRequest)
                .enqueue()

    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (isNeedRefresh(this)) {
            viewModel.refreshList()
            setNeedRefresh(this,false)
        }
    }

    private fun checkUtilBox() {
        // Initialize the ShellHandler for further root checks
        if (!initShellHandler()) {
            showWarning(this, classTag("MainActivityX"), this.getString(R.string.busyboxProblem)) { _: DialogInterface?, _: Int -> finishAffinity() }
        }
    }

    fun updatePackage(packageName: String) {
        StorageFile.invalidateCache()
        viewModel.updatePackage(packageName)
    }

    fun refreshView() {
        refresh(mainBoolean, (!mainBoolean && backupBoolean) || (mainBoolean && sheetApp != null))
    }

    // Most functionality could be added to the view model
    fun refresh(mainBoolean: Boolean, backupOrAppSheetBoolean: Boolean) {
        Timber.d("refreshing")
        viewModel.badgeCounter.value = 0
        if (mainBoolean) {
            viewModel.apkCheckedList.clear()
            viewModel.dataCheckedList.clear()
        }
        sheetSortFilter = SortFilterSheet(getFilterPreferences(this), getStats(viewModel.appInfoList.value
                ?: mutableListOf()))
        Thread {
            try {
                val filteredList = applyFilter(viewModel.appInfoList.value
                        ?: listOf(), getFilterPreferences(this).toString(), this)
                when {
                    mainBoolean -> refreshMain(filteredList, backupOrAppSheetBoolean)
                    else -> refreshBatch(filteredList, backupOrAppSheetBoolean)
                }
            } catch (e: BackupLocationIsAccessibleException) {
                Timber.e("Could not update application list: $e")
            } catch (e: StorageLocationNotConfiguredException) {
                Timber.e("Could not update application list: $e")
            } catch (e: Throwable) {
                LogUtils.unhandledException(e)
            }
        }.start()
    }

    private fun refreshMain(filteredList: List<AppInfo>, appSheetBoolean: Boolean) {
        val mainList = createMainAppsList(filteredList)
        runOnUiThread {
            try {
                FastAdapterDiffUtil[mainItemAdapter] = mainList
                if (mainList.isEmpty())
                    Toast.makeText(this, getString(R.string.empty_filtered_list), Toast.LENGTH_SHORT).show()
                searchViewController?.setup()
                mainFastAdapter?.notifyAdapterDataSetChanged()
                if (appSheetBoolean) refreshAppSheet()
                OnlyInJava.slideUp(binding.bottomBar)
                viewModel.finishRefresh()
            } catch (e: Throwable) {
                LogUtils.unhandledException(e)
            }
        }
    }

    private fun createMainAppsList(filteredList: List<AppInfo>): MutableList<MainItemX> = filteredList
            .map { MainItemX(it) }.toMutableList()


    private fun refreshAppSheet() {
        try {
            val position = sheetApp?.position ?: -1
            if (mainItemAdapter.itemList.size() > position && position != -1) {
                val sheetAppInfo = mainFastAdapter?.getItem(position)?.app
                sheetAppInfo?.let {
                    if (sheetApp?.packageName == sheetAppInfo.packageName) {
                        mainFastAdapter?.getItem(position)?.let { sheetApp?.updateApp(it) }
                    } else
                        sheetApp?.dismissAllowingStateLoss()
                }
            } else
                sheetApp?.dismissAllowingStateLoss()
        } catch (e: Throwable) {
            LogUtils.unhandledException(e)
        }
    }

    private fun refreshBatch(filteredList: List<AppInfo>, backupBoolean: Boolean) {
        val batchList = createBatchAppsList(filteredList, backupBoolean)
        runOnUiThread {
            try {
                FastAdapterDiffUtil[batchItemAdapter] = batchList
                if (batchList.isEmpty())
                    Toast.makeText(this, getString(R.string.empty_filtered_list), Toast.LENGTH_SHORT).show()
                searchViewController?.setup()
                batchFastAdapter?.notifyAdapterDataSetChanged()
                updateCheckAll()

                OnlyInJava.slideUp(binding.bottomBar)
                viewModel.finishRefresh()
            } catch (e: Throwable) {
                LogUtils.unhandledException(e)
            }
        }
    }

    private fun createBatchAppsList(filteredList: List<AppInfo>, backupBoolean: Boolean): MutableList<BatchItemX> = filteredList
            .filter { toAddToBatch(backupBoolean, it) }.map {
                val item = BatchItemX(it, backupBoolean)
                if (viewModel.apkCheckedList.contains(it.packageName)) item.isApkChecked = true
                if (viewModel.dataCheckedList.contains(it.packageName)) item.isDataChecked = true
                return@map item
            }.toMutableList()


    private fun toAddToBatch(backupBoolean: Boolean, app: AppInfo): Boolean = if (backupBoolean) app.isInstalled else app.hasBackups
}
