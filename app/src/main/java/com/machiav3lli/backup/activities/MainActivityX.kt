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

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.badge.BadgeDrawable
import com.machiav3lli.backup.*
import com.machiav3lli.backup.Constants.classTag
import com.machiav3lli.backup.activities.PrefsActivity
import com.machiav3lli.backup.databinding.ActivityMainXBinding
import com.machiav3lli.backup.dialogs.BatchConfirmDialog
import com.machiav3lli.backup.fragments.AppSheet
import com.machiav3lli.backup.fragments.HelpSheet
import com.machiav3lli.backup.fragments.SortFilterSheet
import com.machiav3lli.backup.handler.BackendController.getApplicationList
import com.machiav3lli.backup.handler.BackupRestoreHelper
import com.machiav3lli.backup.handler.NotificationHelper.showNotification
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.handler.SortFilterManager.applyFilter
import com.machiav3lli.backup.handler.SortFilterManager.getFilterPreferences
import com.machiav3lli.backup.handler.SortFilterManager.saveFilterPreferences
import com.machiav3lli.backup.items.*
import com.machiav3lli.backup.utils.FileUtils.BackupLocationIsAccessibleException
import com.machiav3lli.backup.utils.LogUtils.Companion.logErrors
import com.machiav3lli.backup.utils.PrefUtils.StorageLocationNotConfiguredException
import com.machiav3lli.backup.utils.PrefUtils.getDefaultSharedPreferences
import com.machiav3lli.backup.utils.PrefUtils.getPrivateSharedPrefs
import com.machiav3lli.backup.utils.UIUtils.showActionResult
import com.machiav3lli.backup.utils.UIUtils.showWarning
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil.set
import com.mikepenz.fastadapter.listeners.ClickEventHook
import com.topjohnwu.superuser.Shell
import java.util.function.Consumer

class MainActivityX : BaseActivity(), BatchConfirmDialog.ConfirmListener {

    companion object {
        private val TAG = classTag(".MainActivityX")
        var shellHandlerInstance: ShellHandler? = null
            private set

        fun initShellHandler(): Boolean {
            shellHandlerInstance = ShellHandler()
            return true
        }

        init {
            /* Shell.Config methods shall be called before any shell is created
         * This is the why in this example we call it in a static block
         * The followings are some examples, check Javadoc for more details */
            Shell.enableVerboseLogging = BuildConfig.DEBUG
            Shell.setDefaultBuilder(Shell.Builder.create()
                    .setTimeout(20))
        }
    }

    // TODO DataModel to lay the ground for more abstraction
    private var appsList: List<AppInfoX>? = null

    // TODO optimize usage (maybe a map instead?)
    var apkCheckedList: MutableList<String>? = java.util.ArrayList()
    var dataCheckedList: MutableList<String>? = java.util.ArrayList()
    private var updatedBadge: BadgeDrawable? = null
    private var badgeCounter = 0
    private var powerManager: PowerManager? = null
    private var binding: ActivityMainXBinding? = null
    val mainItemAdapter = ItemAdapter<MainItemX>()
    private var mainFastAdapter: FastAdapter<MainItemX>? = null
    val batchItemAdapter = ItemAdapter<BatchItemX>()
    private var batchFastAdapter: FastAdapter<BatchItemX>? = null
    private var mainBoolean = false
    private var backupBoolean = false
    private var prefs: SharedPreferences? = null
    private var navController: NavController? = null
    private var sheetSortFilter: SortFilterSheet? = null
    private var sheetApp: AppSheet? = null
    var sheetHelp: HelpSheet? = null
    private var searchViewController: SearchViewController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainXBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        powerManager = getSystemService(POWER_SERVICE) as PowerManager
        prefs = getPrivateSharedPrefs(this)
        checkUtilBox()
        setupViews(savedInstanceState)
        setupNavigation()
        setupOnClicks()
        runOnUiThread { showEncryptionDialog() }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        apkCheckedList = savedInstanceState.getStringArrayList("apkCheckedList")
        dataCheckedList = savedInstanceState.getStringArrayList("dataCheckedList")
        setupViews(savedInstanceState)
        setupNavigation()
        setupOnClicks()
    }

    override fun onBackPressed() {
        finishAffinity()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        var outState = outState
        outState = mainFastAdapter!!.saveInstanceState(outState)
        outState = batchFastAdapter!!.saveInstanceState(outState)
        outState.putStringArrayList("apkCheckedList", java.util.ArrayList(apkCheckedList))
        outState.putStringArrayList("dataCheckedList", java.util.ArrayList(dataCheckedList))
        super.onSaveInstanceState(outState)
    }

    fun setSearchViewController(searchViewController: SearchViewController?) {
        this.searchViewController = searchViewController
    }

    private fun setupViews(savedInstanceState: Bundle?) {
        binding!!.refreshLayout.setColorSchemeColors(resources.getColor(R.color.app_accent, theme))
        binding!!.refreshLayout.setProgressBackgroundColorSchemeColor(resources.getColor(R.color.app_primary_base, theme))
        binding!!.cbAll.isChecked = false
        binding!!.bottomNavigation.getOrCreateBadge(R.id.mainFragment)
        updatedBadge = binding!!.bottomNavigation.getOrCreateBadge(R.id.mainFragment)
        updatedBadge!!.backgroundColor = resources.getColor(R.color.app_accent, theme)
        updatedBadge!!.isVisible = badgeCounter != 0
        mainFastAdapter = FastAdapter.with(mainItemAdapter)
        batchFastAdapter = FastAdapter.with(batchItemAdapter)
        mainFastAdapter!!.setHasStableIds(true)
        batchFastAdapter!!.setHasStableIds(true)
        if (savedInstanceState != null) {
            if (mainBoolean) {
                mainFastAdapter = mainFastAdapter!!.withSavedInstanceState(savedInstanceState)
            } else {
                batchFastAdapter = batchFastAdapter!!.withSavedInstanceState(savedInstanceState)
            }
        }
        binding!!.recyclerView.layoutManager = LinearLayoutManager(this)
        binding!!.refreshLayout.setOnRefreshListener { cleanRefresh() }
    }

    private fun setupNavigation() {
        val navHostFragment = (supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment?)!!
        navController = navHostFragment.navController
        binding!!.bottomNavigation.setOnNavigationItemSelectedListener { item: MenuItem ->
            if (item.itemId == binding!!.bottomNavigation.selectedItemId) return@setOnNavigationItemSelectedListener false
            navController!!.navigate(item.itemId)
            true
        }
        navController!!.addOnDestinationChangedListener { _: NavController?, destination: NavDestination, _: Bundle? ->
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
        binding!!.batchBar.visibility = View.GONE
        binding!!.modeBar.visibility = View.GONE
        binding!!.recyclerView.adapter = mainFastAdapter
    }

    private fun navigateBatch() {
        mainBoolean = false
        binding!!.batchBar.visibility = View.VISIBLE
        binding!!.modeBar.visibility = View.VISIBLE
        binding!!.buttonAction.setText(if (backupBoolean) R.string.backup else R.string.restore)
        binding!!.recyclerView.adapter = batchFastAdapter
        binding!!.buttonAction.setOnClickListener { actionOnClick(backupBoolean) }
        apkCheckedList!!.clear()
        dataCheckedList!!.clear()
    }

    private fun setupOnClicks() {
        binding!!.buttonSettings.setOnClickListener { startActivity(Intent(applicationContext, PrefsActivity::class.java)) }
        binding!!.buttonScheduler.setOnClickListener { startActivity(Intent(applicationContext, SchedulerActivityX::class.java)) }
        binding!!.cbAll.setOnClickListener { v: View -> onCheckAllChanged(v) }
        binding!!.buttonSortFilter.setOnClickListener {
            if (sheetSortFilter == null) sheetSortFilter = SortFilterSheet(SortFilterModel(getFilterPreferences(this).toString()))
            sheetSortFilter!!.show(supportFragmentManager, "SORTFILTERSHEET")
        }
        mainFastAdapter!!.onClickListener = { _: View?, _: IAdapter<MainItemX>?, item: MainItemX?, position: Int? ->
            if (sheetApp != null) sheetApp!!.dismissAllowingStateLoss()
            sheetApp = AppSheet(item!!, position!!)
            sheetApp!!.showNow(supportFragmentManager, "APPSHEET")
            false
        }
        batchFastAdapter!!.onClickListener = { _: View?, _: IAdapter<BatchItemX>?, item: BatchItemX, _: Int? ->
            val oldChecked = item.isChecked
            item.isApkChecked = !oldChecked
            item.isDataChecked = !oldChecked
            if (item.isChecked) {
                if (!apkCheckedList!!.contains(item.app.packageName)) {
                    apkCheckedList!!.add(item.app.packageName)
                }
                if (!dataCheckedList!!.contains(item.app.packageName)) {
                    dataCheckedList!!.add(item.app.packageName)
                }
            } else {
                apkCheckedList!!.remove(item.app.packageName)
                dataCheckedList!!.remove(item.app.packageName)
            }
            batchFastAdapter!!.notifyAdapterDataSetChanged()
            updateCheckAll()
            false
        }
        binding!!.apkBatch.setOnClickListener {
            val checkBoolean = apkCheckedList!!.size != batchItemAdapter.itemList.size()
            apkCheckedList!!.clear()
            batchItemAdapter.adapterItems.forEach(Consumer { batchItemX: BatchItemX ->
                val packageName = batchItemX.app.packageName
                batchItemX.isApkChecked = checkBoolean
                if (checkBoolean) apkCheckedList!!.add(packageName)
            }
            )
            batchFastAdapter!!.notifyAdapterDataSetChanged()
            updateCheckAll()
        }
        binding!!.dataBatch.setOnClickListener {
            val checkBoolean = dataCheckedList!!.size != batchItemAdapter.itemList.size()
            dataCheckedList!!.clear()
            batchItemAdapter.adapterItems.forEach(Consumer { batchItemX: BatchItemX ->
                val packageName = batchItemX.app.packageName
                batchItemX.isDataChecked = checkBoolean
                if (checkBoolean) dataCheckedList!!.add(packageName)
            }
            )
            batchFastAdapter!!.notifyAdapterDataSetChanged()
            updateCheckAll()
        }
        batchFastAdapter!!.addEventHook(OnApkCheckBoxClickHook())
        batchFastAdapter!!.addEventHook(OnDataCheckBoxClickHook())
    }

    private fun onCheckAllChanged(v: View) {
        val startIsChecked = (v as AppCompatCheckBox).isChecked
        binding!!.cbAll.isChecked = startIsChecked
        for (item in batchItemAdapter.adapterItems) {
            item.isApkChecked = startIsChecked
            item.isDataChecked = startIsChecked
            if (startIsChecked) {
                if (!apkCheckedList!!.contains(item.app.packageName)) {
                    apkCheckedList!!.add(item.app.packageName)
                }
                if (!dataCheckedList!!.contains(item.app.packageName)) {
                    dataCheckedList!!.add(item.app.packageName)
                }
            } else {
                apkCheckedList!!.remove(item.app.packageName)
                dataCheckedList!!.remove(item.app.packageName)
            }
        }
        batchFastAdapter!!.notifyAdapterDataSetChanged()
    }

    private fun updateCheckAll() {
        binding!!.cbAll.isChecked = apkCheckedList!!.size == batchItemAdapter.itemList.size() && dataCheckedList!!.size == batchItemAdapter.itemList.size()
    }

    inner class OnApkCheckBoxClickHook : ClickEventHook<BatchItemX>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            return viewHolder.itemView.findViewById(R.id.apkCheckBox)
        }

        override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<BatchItemX>, item: BatchItemX) {
            item.isApkChecked = !item.isApkChecked
            if (item.isApkChecked && !apkCheckedList!!.contains(item.app.packageName)) {
                apkCheckedList!!.add(item.app.packageName)
            } else {
                apkCheckedList!!.remove(item.app.packageName)
            }
            batchFastAdapter!!.notifyAdapterDataSetChanged()
            updateCheckAll()
        }
    }

    inner class OnDataCheckBoxClickHook : ClickEventHook<BatchItemX>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            return viewHolder.itemView.findViewById(R.id.dataCheckbox)
        }

        override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<BatchItemX>, item: BatchItemX) {
            item.isDataChecked = !item.isDataChecked
            if (item.isDataChecked && !dataCheckedList!!.contains(item.app.packageName)) {
                dataCheckedList!!.add(item.app.packageName)
            } else {
                dataCheckedList!!.remove(item.app.packageName)
            }
            batchFastAdapter!!.notifyAdapterDataSetChanged()
            updateCheckAll()
        }
    }

    private fun showEncryptionDialog() {
        val defPrefs = getDefaultSharedPreferences(this)
        val dontShowAgain = defPrefs.getBoolean(Constants.PREFS_ENCRYPTION, false) && defPrefs.getString(Constants.PREFS_PASSWORD, "")!!.isNotEmpty()
        if (dontShowAgain) return
        val dontShowCounter = prefs!!.getInt(Constants.PREFS_SKIPPEDENCRYPTION, 0)
        prefs!!.edit().putInt(Constants.PREFS_SKIPPEDENCRYPTION, dontShowCounter + 1).apply()
        if (dontShowCounter % 10 == 0) {
            AlertDialog.Builder(this)
                    .setTitle(R.string.enable_encryption_title)
                    .setMessage(R.string.enable_encryption_message)
                    .setPositiveButton(R.string.dialog_approve) { _: DialogInterface?, _: Int -> startActivity(Intent(applicationContext, PrefsActivity::class.java).putExtra(".toEncryption", true)) }
                    .show()
        }
    }

    private fun actionOnClick(backupBoolean: Boolean) {
        val selectedList = batchItemAdapter.adapterItems
                .filter(BatchItemX::isChecked)
                .map { item: BatchItemX -> item.app.appInfo }
                .toCollection(ArrayList())
        val selectedListModes = batchItemAdapter.adapterItems
                .filter(BatchItemX::isChecked)
                .map(BatchItemX::actionMode)
                .toCollection(ArrayList())
        val arguments = Bundle()
        arguments.putIntegerArrayList("selectedListModes", selectedListModes)
        arguments.putParcelableArrayList("selectedList", selectedList)
        arguments.putBoolean("backupBoolean", backupBoolean)
        val dialog = BatchConfirmDialog(this)
        dialog.arguments = arguments
        dialog.show(supportFragmentManager, "DialogFragment")
    }

    override fun onConfirmed(selectedList: List<Pair<AppMetaInfo, Int>>) {
        Thread { runBatchTask(selectedList) }.start()
    }

    // TODO 1. optimize/reduce complexity
    fun runBatchTask(selectedItems: List<Pair<AppMetaInfo, Int>>) {
        @SuppressLint("InvalidWakeLockTag") val wl = powerManager!!.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG)
        if (prefs!!.getBoolean("acquireWakelock", true)) {
            wl.acquire(60 * 60 * 1000L /*60 minutes to cope with slower devices*/)
            Log.i(TAG, "wakelock acquired")
        }
        // get the AppInfoX objects again
        val selectedApps: MutableList<Pair<AppInfoX, Int>> = java.util.ArrayList(selectedItems.size)
        for ((first, second) in selectedItems) {
            val foundItem = batchItemAdapter.adapterItems.stream()
                    .filter { item: BatchItemX -> item.app.packageName == first.packageName }
                    .findFirst()
            if (foundItem.isPresent) {
                selectedApps.add(Pair(foundItem.get().app, second))
            } else {
                throw RuntimeException("Selected item for processing went lost from the item adapter.")
            }
        }
        val notificationId = System.currentTimeMillis().toInt()
        val totalOfActions = selectedItems.size
        val backupRestoreHelper = BackupRestoreHelper()
        val mileStones = IntRange(0, 5).map { step: Int -> step * totalOfActions / 5 + 1 }.toList()
        var result: ActionResult?
        val results: MutableList<ActionResult> = java.util.ArrayList(totalOfActions)
        var i = 1
        for ((first, mode) in selectedApps) {
            val message = String.format("%s (%d/%d)", if (backupBoolean) this.getString(R.string.backupProgress) else this.getString(R.string.restoreProgress), i, totalOfActions)
            showNotification(this, MainActivityX::class.java, notificationId, message, first.packageLabel, false)
            if (mileStones.contains(i)) {
                runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
            }
            result = if (backupBoolean) {
                backupRestoreHelper.backup(this, shellHandlerInstance!!, first, mode)
            } else {
                // Latest backup for now
                val selectedBackup = first.latestBackup
                backupRestoreHelper.restore(this, first, selectedBackup!!.backupProperties,
                        selectedBackup.backupLocation, shellHandlerInstance, mode)
            }
            if (!result!!.succeeded) {
                showNotification(this, MainActivityX::class.java, result.hashCode(), first.packageLabel, result.message, false)
            }
            results.add(result)
            i++
        }
        if (wl.isHeld) {
            wl.release()
            Log.i(TAG, "wakelock released")
        }

        // Calculate the overall result
        val errors = results
                .map { result -> result.message }
                .filter { msg: String -> msg.isNotEmpty() }
                .joinToString(separator = "\n")
        val overAllResult = ActionResult(null, null, errors, results.parallelStream().anyMatch(ActionResult::succeeded))

        // Update the notification
        val notificationTitle = if (overAllResult.succeeded) this.getString(R.string.batchSuccess) else this.getString(R.string.batchFailure)
        val notificationMessage = if (backupBoolean) this.getString(R.string.batchbackup) else this.getString(R.string.batchrestore)
        showNotification(this, MainActivityX::class.java, notificationId, notificationTitle, notificationMessage, true)
        runOnUiThread { Toast.makeText(this, String.format("%s: %s)", notificationMessage, notificationTitle), Toast.LENGTH_LONG).show() }

        // show results to the user. Add a save button, if logs should be saved to the application log (in case it's too much)
        showActionResult(this, overAllResult, if (overAllResult.succeeded) null else DialogInterface.OnClickListener { _: DialogInterface?, _: Int -> logErrors(this, errors) })
        cleanRefresh()
    }

    private fun checkUtilBox() {
        // Initialize the ShellHandler for further root checks
        if (!initShellHandler()) {
            showWarning(this, TAG, this.getString(R.string.busyboxProblem)) { _: DialogInterface?, _: Int -> finishAffinity() }
        }
    }

    fun cleanRefresh() {
        refresh(mainBoolean, !mainBoolean && backupBoolean, true)
    }

    fun refreshWithAppSheet() {
        refresh(true, true, true)
    }

    fun batchRefresh() {
        refresh(false, backupBoolean, false)
    }

    fun refresh(mainBoolean: Boolean, backupOrAppSheetBoolean: Boolean, cleanBoolean: Boolean) {
        Log.d(TAG, "refreshing")
        runOnUiThread {
            binding!!.refreshLayout.isRefreshing = true
            searchViewController!!.clean()
        }
        badgeCounter = 0
        if (mainBoolean || cleanBoolean) {
            apkCheckedList!!.clear()
            dataCheckedList!!.clear()
        }
        sheetSortFilter = SortFilterSheet(getFilterPreferences(this))
        Thread {
            try {
                appsList = getApplicationList(this.applicationContext)
                getPrivateSharedPrefs(this).getBoolean(Constants.PREFS_ENABLESPECIALBACKUPS, false)
                val filteredList = applyFilter(appsList!!,
                        getFilterPreferences(this).toString(), this)
                if (mainBoolean) refreshMain(filteredList, backupOrAppSheetBoolean) else refreshBatch(filteredList, backupOrAppSheetBoolean)
            } catch (e: BackupLocationIsAccessibleException) {
                Log.e(TAG, "Could not update application list: $e")
            } catch (e: StorageLocationNotConfiguredException) {
                Log.e(TAG, "Could not update application list: $e")
            }
        }.start()
    }

    private fun refreshMain(filteredList: List<AppInfoX>, appSheetBoolean: Boolean) {
        val mainList = createMainAppsList(filteredList)
        runOnUiThread {
            if (filteredList.isEmpty()) {
                Toast.makeText(baseContext, getString(R.string.empty_filtered_list), Toast.LENGTH_SHORT).show()
                mainItemAdapter.clear()
            }
            set(mainItemAdapter, mainList)
            searchViewController!!.setup()
            if (updatedBadge != null) {
                updatedBadge!!.number = badgeCounter
                updatedBadge!!.isVisible = badgeCounter != 0
            }
            mainFastAdapter!!.notifyAdapterDataSetChanged()
            binding!!.refreshLayout.isRefreshing = false
            if (appSheetBoolean && sheetApp != null) refreshAppSheet()
            OnlyInJava.slideUp(binding!!.bottomBar)
        }
    }

    private fun createMainAppsList(filteredList: List<AppInfoX>): java.util.ArrayList<MainItemX> {
        val list = java.util.ArrayList<MainItemX>()
        if (filteredList.isEmpty()) {
            for (app in applyFilter(appsList!!, "0000", this)) {
                list.add(MainItemX(app))
                if (app.isUpdated) badgeCounter += 1
            }
            saveFilterPreferences(this, SortFilterModel())
        } else {
            for (app in filteredList) {
                list.add(MainItemX(app))
                if (app.isUpdated) badgeCounter += 1
            }
        }
        return list
    }

    private fun refreshAppSheet() {
        val position = sheetApp!!.position
        if (mainItemAdapter.itemList.size() > position) {
            if (sheetApp!!.packageName == mainFastAdapter!!.getItem(position)!!.app.packageName) {
                sheetApp!!.updateApp(mainFastAdapter!!.getItem(position)!!)
            } else {
                sheetApp!!.dismissAllowingStateLoss()
            }
        } else {
            sheetApp!!.dismissAllowingStateLoss()
        }
    }

    private fun refreshBatch(filteredList: List<AppInfoX>, backupBoolean: Boolean) {
        val batchList = createBatchAppsList(filteredList, backupBoolean)
        runOnUiThread {
            if (filteredList.isEmpty()) {
                Toast.makeText(this, getString(R.string.empty_filtered_list), Toast.LENGTH_SHORT).show()
                batchItemAdapter.clear()
            }
            set(batchItemAdapter, batchList)
            searchViewController!!.setup()
            batchFastAdapter!!.notifyAdapterDataSetChanged()
            updateCheckAll()
            binding!!.refreshLayout.isRefreshing = false
            OnlyInJava.slideUp(binding!!.bottomBar)
        }
    }

    private fun createBatchAppsList(filteredList: List<AppInfoX>, backupBoolean: Boolean): java.util.ArrayList<BatchItemX> {
        val list = java.util.ArrayList<BatchItemX>()
        if (filteredList.isEmpty()) {
            for (app in applyFilter(appsList!!, "0000", this)) {
                if (toAddToBatch(backupBoolean, app)) list.add(BatchItemX(app))
            }
            saveFilterPreferences(this, SortFilterModel())
        } else {
            for (app in filteredList) {
                if (toAddToBatch(backupBoolean, app)) {
                    val item = BatchItemX(app)
                    if (apkCheckedList!!.contains(app.packageName)) item.isApkChecked = true
                    if (dataCheckedList!!.contains(app.packageName)) item.isDataChecked = true
                    list.add(item)
                }
            }
        }
        return list
    }

    private fun toAddToBatch(backupBoolean: Boolean, app: AppInfoX): Boolean {
        return if (backupBoolean) app.isInstalled else app.hasBackups()
    }
}