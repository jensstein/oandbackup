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
import android.os.Looper
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.machiav3lli.backup.MAIN_FILTER_DEFAULT
import com.machiav3lli.backup.NAV_MAIN
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.OABX.Companion.addInfoText
import com.machiav3lli.backup.R
import com.machiav3lli.backup.dbs.ODatabase
import com.machiav3lli.backup.dialogs.PackagesListDialogFragment
import com.machiav3lli.backup.fragments.SortFilterSheet
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.WorkHandler
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.items.SortFilterModel
import com.machiav3lli.backup.preferences.persist_skippedEncryptionCounter
import com.machiav3lli.backup.preferences.pref_catchUncaughtException
import com.machiav3lli.backup.tasks.AppActionWork
import com.machiav3lli.backup.tasks.FinishWork
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.ArrowsClockwise
import com.machiav3lli.backup.ui.compose.icons.phosphor.FunnelSimple
import com.machiav3lli.backup.ui.compose.icons.phosphor.GearSix
import com.machiav3lli.backup.ui.compose.icons.phosphor.Prohibit
import com.machiav3lli.backup.ui.compose.item.ElevatedActionButton
import com.machiav3lli.backup.ui.compose.item.ExpandableSearchAction
import com.machiav3lli.backup.ui.compose.item.RoundButton
import com.machiav3lli.backup.ui.compose.item.TopBar
import com.machiav3lli.backup.ui.compose.navigation.BottomNavBar
import com.machiav3lli.backup.ui.compose.navigation.MainNavHost
import com.machiav3lli.backup.ui.compose.navigation.NavItem
import com.machiav3lli.backup.ui.compose.theme.AppTheme
import com.machiav3lli.backup.utils.FileUtils.invalidateBackupLocation
import com.machiav3lli.backup.utils.applyFilter
import com.machiav3lli.backup.utils.destinationToItem
import com.machiav3lli.backup.utils.getPrivateSharedPrefs
import com.machiav3lli.backup.utils.getStats
import com.machiav3lli.backup.utils.isEncryptionEnabled
import com.machiav3lli.backup.utils.setCustomTheme
import com.machiav3lli.backup.utils.sortFilterModel
import com.machiav3lli.backup.viewmodels.MainViewModel
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.system.exitProcess

class MainActivityX : BaseActivity() {

    private lateinit var prefs: SharedPreferences
    private val crScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    private lateinit var refreshViewController: RefreshViewController // TODO replace
    private lateinit var progressViewController: ProgressViewController // TODO replace

    val viewModel by viewModels<MainViewModel> {
        MainViewModel.Factory(ODatabase.getInstance(OABX.context), application)
    }

    var needRefresh: Boolean
        get() = viewModel.isNeedRefresh.value ?: false
        set(value) = viewModel.isNeedRefresh.postValue(value)

    private val _searchQuery = MutableSharedFlow<String>()
    val searchQuery = _searchQuery.asSharedFlow()

    private lateinit var sheetSortFilter: SortFilterSheet
    private val _modelSortFilter = MutableSharedFlow<SortFilterModel>()
    val modelSortFilter = _modelSortFilter.asSharedFlow()

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val context = this
        OABX.activity = this
        OABX.main = this

        setCustomTheme()
        super.onCreate(savedInstanceState)

        OABX.appsSuspendedChecked = false

        if (pref_catchUncaughtException.value) {
            Thread.setDefaultUncaughtExceptionHandler { _, e ->
                try {
                    Timber.i("\n\n" + "=".repeat(60))
                    LogsHandler.unhandledException(e)
                    LogsHandler(context).writeToLogFile("uncaught: ${e.message}")
                    object : Thread() {
                        override fun run() {
                            Looper.prepare()
                            Looper.loop()
                        }
                    }.start()
                } catch (e: Throwable) {
                    // ignore
                } finally {
                    exitProcess(2)
                }
            }
        }

        Shell.getShell()

        prefs = getPrivateSharedPrefs()

        viewModel.blocklist.observe(this) {
            needRefresh = true
        }
        viewModel.packageList.observe(this) { }
        viewModel.backupsMap.observe(this) { }
        viewModel.isNeedRefresh.observe(this) {
            if (it && viewModel.refreshing.value == 0)
                invalidateBackupLocation()
        }

        runOnUiThread { showEncryptionDialog() }

        setContent {
            AppTheme {
                val navController = rememberAnimatedNavController()
                val query by searchQuery.collectAsState(initial = "")
                val list by viewModel.packageList.observeAsState(null)
                var pageTitle by remember {
                    mutableStateOf(NavItem.Home.title)
                }

                navController.addOnDestinationChangedListener { _, destination, _ ->
                    pageTitle = destination.destinationToItem()?.title ?: NavItem.Home.title
                }

                SideEffect {
                    crScope.launch { _searchQuery.emit("") }
                    crScope.launch { _modelSortFilter.emit(sortFilterModel) }
                }

                Scaffold(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    topBar = {

                        if (navController.currentDestination?.route == NavItem.Scheduler.destination)
                            TopBar(title = stringResource(id = pageTitle)) {
                                RoundButton(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .size(32.dp),
                                    icon = Phosphor.Prohibit,
                                    description = stringResource(id = R.string.sched_blocklist)
                                ) {
                                    GlobalScope.launch(Dispatchers.IO) {
                                        val blocklistedPackages =
                                            context.viewModel.blocklist.value
                                                ?.mapNotNull { it.packageName }.orEmpty()

                                        PackagesListDialogFragment(
                                            blocklistedPackages,
                                            MAIN_FILTER_DEFAULT,
                                            true
                                        ) { newList: Set<String> ->
                                            context.viewModel.updateBlocklist(
                                                newList
                                            )
                                        }.show(
                                            context.supportFragmentManager,
                                            "BLOCKLIST_DIALOG"
                                        )
                                    }
                                }
                                RoundButton(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .size(32.dp),
                                    description = stringResource(id = R.string.prefs_title),
                                    icon = Phosphor.GearSix
                                ) { navController.navigate(NavItem.Settings.destination) }
                            }
                        else Column() {
                            TopBar(title = stringResource(id = pageTitle)) {
                                ExpandableSearchAction(
                                    query = query,
                                    onQueryChanged = { newQuery ->
                                        if (newQuery != query)
                                            crScope.launch { _searchQuery.emit(newQuery) }
                                    },
                                    onClose = {
                                        crScope.launch { _searchQuery.emit("") }
                                    }
                                )
                                RoundButton(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .size(32.dp),
                                    description = stringResource(id = R.string.refresh),
                                    icon = Phosphor.ArrowsClockwise
                                ) { OABX.main?.needRefresh = true }
                                RoundButton(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .size(32.dp),
                                    description = stringResource(id = R.string.prefs_title),
                                    icon = Phosphor.GearSix
                                ) { navController.navigate(NavItem.Settings.destination) }
                            }
                            Row(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                ElevatedActionButton(
                                    icon = Phosphor.Prohibit,
                                    text = stringResource(id = R.string.sched_blocklist),
                                    positive = false
                                ) {
                                    GlobalScope.launch(Dispatchers.IO) {
                                        val blocklistedPackages = viewModel.blocklist.value
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
                                    icon = Phosphor.FunnelSimple,
                                    text = stringResource(id = R.string.sort_and_filter),
                                    positive = true
                                ) {
                                    sheetSortFilter = SortFilterSheet(
                                        sortFilterModel,
                                        getStats(
                                            list?.applyFilter(sortFilterModel, context)
                                                ?: emptyList()
                                        ) // TODO apply page's filter too
                                    )
                                    sheetSortFilter.showNow(
                                        supportFragmentManager,
                                        "SORTFILTER_SHEET"
                                    )
                                }
                            }
                        }
                    },
                    bottomBar = { BottomNavBar(page = NAV_MAIN, navController = navController) }
                ) { paddingValues ->
                    MainNavHost(
                        modifier = Modifier.padding(paddingValues),
                        navController = navController,
                        application = application
                    )
                }
            }
        }

        if (doIntent(intent))
            return
    }

    override fun onResume() {
        OABX.activity = this    // just in case 'this' object is recreated
        OABX.main = this
        super.onResume()
    }

    override fun onDestroy() {
        OABX.main = null
        super.onDestroy()
    }

    override fun onBackPressed() {
        finishAffinity()
    }

    override fun onNewIntent(intent: Intent?) {
        doIntent(intent)
        super.onNewIntent(intent)
    }

    fun doIntent(intent: Intent?): Boolean {
        if (intent == null) return false
        val command = intent.action
        Timber.i("Main: command $command")
        when (command) {
            null -> {}
            "android.intent.action.MAIN" -> {}
            else -> {
                addInfoText("Main: command '$command'")
            }
        }
        return false
    }

    private fun showEncryptionDialog() {
        val dontShowAgain = isEncryptionEnabled()
        if (dontShowAgain) return
        val dontShowCounter = persist_skippedEncryptionCounter.value
        persist_skippedEncryptionCounter.value = dontShowCounter + 1
        if (dontShowCounter % 10 == 0 && dontShowCounter <= 30) {
            AlertDialog.Builder(this)
                .setTitle(R.string.enable_encryption_title)
                .setMessage(R.string.enable_encryption_message)
                .setPositiveButton(R.string.dialog_approve) { _: DialogInterface?, _: Int ->
                    startActivity(
                        Intent(applicationContext, PrefsActivityX::class.java).putExtra(
                            ".toEncryption",
                            true
                        )
                    )
                }
                .show()
        }
    }

    fun updatePackage(packageName: String) {
        viewModel.updatePackage(packageName)
    }

    fun setRefreshViewController(refreshViewController: RefreshViewController) {
        this.refreshViewController = refreshViewController
    }

    fun refreshView() {
        crScope.launch { _modelSortFilter.emit(sortFilterModel) }
        if (::refreshViewController.isInitialized) refreshViewController.refreshView(viewModel.packageList.value)
    }

    fun setProgressViewController(progressViewController: ProgressViewController) {
        this.progressViewController = progressViewController
    }

    fun updateProgress(progress: Int, max: Int) {
        if (::progressViewController.isInitialized)
            this.progressViewController.updateProgress(progress, max)
    }

    fun hideProgress() {
        if (::progressViewController.isInitialized)
            this.progressViewController.hideProgress()
    }

    fun showSnackBar(message: String) {
    }

    fun dismissSnackBar() {
    }

    fun whileShowingSnackBar(message: String, todo: () -> Unit) {
        runOnUiThread {
            showSnackBar(message)
        }
        todo()
        runOnUiThread {
            dismissSnackBar()
        }
    }

    fun startBatchAction(
        backupBoolean: Boolean,
        selectedPackages: List<String?>,
        selectedModes: List<Int>,
        onSuccessfulFinish: Observer<WorkInfo>.(LiveData<WorkInfo>) -> Unit
    ) {
        val now = System.currentTimeMillis()
        val notificationId = now.toInt()
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
                AppActionWork.Request(
                    packageName,
                    mode,
                    backupBoolean,
                    notificationId,
                    batchName,
                    true
                )
            worksList.add(oneTimeWorkRequest)

            val oneTimeWorkLiveData = WorkManager.getInstance(OABX.context)
                .getWorkInfoByIdLiveData(oneTimeWorkRequest.id)
            oneTimeWorkLiveData.observeForever(object : Observer<WorkInfo> {
                override fun onChanged(t: WorkInfo?) {
                    if (t?.state == WorkInfo.State.SUCCEEDED) {
                        counter += 1

                        val (succeeded, packageLabel, error) = AppActionWork.getOutput(t)
                        if (error.isNotEmpty()) errors = "$errors$packageLabel: ${
                            LogsHandler.handleErrorMessages(
                                OABX.context,
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

        val finishWorkLiveData = WorkManager.getInstance(OABX.context)
            .getWorkInfoByIdLiveData(finishWorkRequest.id)
        finishWorkLiveData.observeForever(object : Observer<WorkInfo> {
            override fun onChanged(t: WorkInfo?) {
                if (t?.state == WorkInfo.State.SUCCEEDED) {
                    onSuccessfulFinish(finishWorkLiveData)
                }
            }
        })

        if (worksList.isNotEmpty()) {
            WorkManager.getInstance(OABX.context)
                .beginWith(worksList)
                .then(finishWorkRequest)
                .enqueue()
        }
    }
}

interface RefreshViewController {
    fun refreshView(list: MutableList<Package>?)
}

interface ProgressViewController {
    fun updateProgress(progress: Int, max: Int)
    fun hideProgress()
}
