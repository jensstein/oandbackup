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

import android.content.ComponentName
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.os.PowerManager
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.navigation.NavHostController
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.machiav3lli.backup.ALT_MODE_APK
import com.machiav3lli.backup.ALT_MODE_BOTH
import com.machiav3lli.backup.ALT_MODE_DATA
import com.machiav3lli.backup.MAIN_FILTER_DEFAULT
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.OABX.Companion.addInfoLogText
import com.machiav3lli.backup.OABX.Companion.startup
import com.machiav3lli.backup.R
import com.machiav3lli.backup.classAddress
import com.machiav3lli.backup.dialogs.PackagesListDialogFragment
import com.machiav3lli.backup.fragments.BatchPrefsSheet
import com.machiav3lli.backup.fragments.SortFilterSheet
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.WorkHandler
import com.machiav3lli.backup.handler.findBackups
import com.machiav3lli.backup.handler.updateAppTables
import com.machiav3lli.backup.pref_catchUncaughtException
import com.machiav3lli.backup.pref_uncaughtExceptionsJumpToPreferences
import com.machiav3lli.backup.preferences.persist_beenWelcomed
import com.machiav3lli.backup.preferences.persist_ignoreBatteryOptimization
import com.machiav3lli.backup.preferences.persist_skippedEncryptionCounter
import com.machiav3lli.backup.preferences.pref_blackTheme
import com.machiav3lli.backup.tasks.AppActionWork
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.FunnelSimple
import com.machiav3lli.backup.ui.compose.icons.phosphor.GearSix
import com.machiav3lli.backup.ui.compose.icons.phosphor.Prohibit
import com.machiav3lli.backup.ui.compose.item.ActionChip
import com.machiav3lli.backup.ui.compose.item.ExpandableSearchAction
import com.machiav3lli.backup.ui.compose.item.RefreshButton
import com.machiav3lli.backup.ui.compose.item.RoundButton
import com.machiav3lli.backup.ui.compose.item.TopBar
import com.machiav3lli.backup.ui.compose.navigation.MainNavHost
import com.machiav3lli.backup.ui.compose.navigation.NavItem
import com.machiav3lli.backup.ui.compose.navigation.PagerNavBar
import com.machiav3lli.backup.ui.compose.recycler.BusyBackground
import com.machiav3lli.backup.ui.compose.theme.AppTheme
import com.machiav3lli.backup.utils.FileUtils.invalidateBackupLocation
import com.machiav3lli.backup.utils.TraceUtils.classAndId
import com.machiav3lli.backup.utils.TraceUtils.traceBold
import com.machiav3lli.backup.utils.altModeToMode
import com.machiav3lli.backup.utils.checkCallLogsPermission
import com.machiav3lli.backup.utils.checkContactsPermission
import com.machiav3lli.backup.utils.checkSMSMMSPermission
import com.machiav3lli.backup.utils.checkUsageStatsPermission
import com.machiav3lli.backup.utils.getDefaultSharedPreferences
import com.machiav3lli.backup.utils.hasStoragePermissions
import com.machiav3lli.backup.utils.isEncryptionEnabled
import com.machiav3lli.backup.utils.isStorageDirSetAndOk
import com.machiav3lli.backup.utils.postNotificationsPermission
import com.machiav3lli.backup.viewmodels.BatchViewModel
import com.machiav3lli.backup.viewmodels.MainViewModel
import com.machiav3lli.backup.viewmodels.SchedulerViewModel
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.system.exitProcess

class MainActivityX : BaseActivity() {

    private val crScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    private lateinit var navController: NavHostController
    private lateinit var powerManager: PowerManager

    val viewModel by viewModels<MainViewModel> {
        MainViewModel.Factory(OABX.db, application)
    }
    val backupViewModel: BatchViewModel by viewModels {
        BatchViewModel.Factory(application)
    }
    val restoreViewModel: BatchViewModel by viewModels {
        BatchViewModel.Factory(application)
    }
    val schedulerViewModel: SchedulerViewModel by viewModels {
        SchedulerViewModel.Factory(OABX.db.scheduleDao, application)
    }

    private lateinit var sheetSortFilter: SortFilterSheet
    private lateinit var sheetBatchPrefs: BatchPrefsSheet

    @OptIn(
        ExperimentalAnimationApi::class,
        ExperimentalPagerApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {

        val context = this

        val mainChanged = (this != OABX.mainSaved)
        OABX.main = this

        var freshStart = (savedInstanceState == null)   //TODO use some lifecycle method?

        Timber.w(
            listOf(
                if (freshStart) "fresh start" else "",
                if (mainChanged && (!freshStart || (OABX.mainSaved != null)))
                    "main changed (was ${classAndId(OABX.mainSaved)})"
                else
                    "",
            ).joinToString(", ")
        )

        super.onCreate(savedInstanceState)

        Timber.d(
            "viewModel: ${
                classAndId(viewModel)
            }, was ${
                classAndId(OABX.viewModelSaved)
            }"
        )

        OABX.appsSuspendedChecked = false

        if (pref_catchUncaughtException.value) {
            Thread.setDefaultUncaughtExceptionHandler { _, e ->
                try {
                    Timber.i("\n\n" + "=".repeat(60))
                    LogsHandler.unexpectedException(e)
                    LogsHandler.logErrors("uncaught: ${e.message}")
                    if (pref_uncaughtExceptionsJumpToPreferences.value) {
                        startActivity(
                            Intent.makeRestartActivityTask(
                                ComponentName(this, PrefsActivityX::class.java)
                            )
                        )
                    }
                    object : Thread() {
                        override fun run() {
                            Looper.prepare()
                            Looper.loop()
                        }
                    }.start()
                } catch (_: Throwable) {
                    // ignore
                } finally {
                    exitProcess(2)
                }
            }
        }

        Shell.getShell()

        powerManager = this.getSystemService(POWER_SERVICE) as PowerManager

        setContent {

            AppTheme {
                val pagerState = rememberPagerState()
                navController = rememberAnimatedNavController()
                val pages = listOf(
                    NavItem.Home,
                    NavItem.Backup,
                    NavItem.Restore,
                    NavItem.Scheduler,
                )
                val currentPage by remember(pagerState.currentPage) { mutableStateOf(pages[pagerState.currentPage]) }   //TODO hg42 remove remember ???
                var barVisible by remember { mutableStateOf(true) }

                LaunchedEffect(viewModel) {
                    navController.addOnDestinationChangedListener { _, destination, _ ->
                        barVisible = destination.route == NavItem.Main.destination
                        if (destination.route == NavItem.Main.destination && freshStart) {
                            freshStart = false
                            traceBold { "******************** freshStart && Main ********************" }
                            MainScope().launch(Dispatchers.IO) {
                                runCatching { findBackups() }
                                startup = false     // ensure backups are no more reported as empty
                                runCatching { updateAppTables() }
                                val time = OABX.endBusy(OABX.startupMsg)
                                addInfoLogText("startup: ${"%.3f".format(time / 1E9)} sec")
                            }
                            runOnUiThread { showEncryptionDialog() }
                        }
                    }
                }

                var query by rememberSaveable { mutableStateOf(viewModel.searchQuery.value) }
                //val query by viewModel.searchQuery.flow.collectAsState(viewModel.searchQuery.initial)  // doesn't work with rotate (not saveable)...
                val searchExpanded = query.isNotEmpty()

                Timber.d("compose: query = '$query'")

                Timber.d("search: ${viewModel.searchQuery.value} filter: ${viewModel.modelSortFilter.value}")

                LaunchedEffect(key1 = pref_blackTheme.value) {
                    getDefaultSharedPreferences()
                        .registerOnSharedPreferenceChangeListener { _, key ->
                            when (key) {
                                pref_blackTheme.key -> recreate()
                                else                -> {}
                            }
                        }
                }

                BusyBackground {
                    Scaffold(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onBackground,
                        topBar = {
                            when {
                                currentPage.destination == NavItem.Scheduler.destination -> TopBar(
                                    title = stringResource(id = currentPage.title)
                                ) {

                                    RoundButton(
                                        icon = Phosphor.Prohibit,
                                        description = stringResource(id = R.string.sched_blocklist)
                                    ) {
                                        GlobalScope.launch(Dispatchers.IO) {
                                            val blocklistedPackages = viewModel.getBlocklist()
                                            PackagesListDialogFragment(
                                                blocklistedPackages,
                                                MAIN_FILTER_DEFAULT,
                                                true
                                            ) { newList: Set<String> ->
                                                viewModel.setBlocklist(newList)
                                            }.show(
                                                context.supportFragmentManager,
                                                "BLOCKLIST_DIALOG"
                                            )
                                        }
                                    }
                                    RoundButton(
                                        description = stringResource(id = R.string.prefs_title),
                                        icon = Phosphor.GearSix
                                    ) { navController.navigate(NavItem.Settings.destination) }
                                }
                                barVisible                                               -> Column() {
                                    TopBar(title = stringResource(id = currentPage.title)) {
                                        ExpandableSearchAction(
                                            expanded = searchExpanded,
                                            query = query,
                                            onQueryChanged = { newQuery ->
                                                //if (newQuery != query)  // empty string doesn't work...
                                                query = newQuery
                                                viewModel.searchQuery.value = query
                                            },
                                            onClose = {
                                                query = ""
                                                viewModel.searchQuery.value = ""
                                            }
                                        )

                                        RefreshButton() { refreshPackagesAndBackups() }
                                        RoundButton(
                                            description = stringResource(id = R.string.prefs_title),
                                            icon = Phosphor.GearSix
                                        ) { navController.navigate(NavItem.Settings.destination) }
                                    }
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        ActionChip(
                                            icon = Phosphor.Prohibit,
                                            textId = R.string.sched_blocklist,
                                            positive = false,
                                        ) {
                                            GlobalScope.launch(Dispatchers.IO) {
                                                val blocklistedPackages = viewModel.getBlocklist()

                                                PackagesListDialogFragment(
                                                    blocklistedPackages,
                                                    MAIN_FILTER_DEFAULT,
                                                    true
                                                ) { newList: Set<String> ->
                                                    viewModel.setBlocklist(newList)
                                                }.show(
                                                    context.supportFragmentManager,
                                                    "BLOCKLIST_DIALOG"
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.weight(1f))
                                        ActionChip(
                                            icon = Phosphor.FunnelSimple,
                                            textId = R.string.sort_and_filter,
                                            positive = true,
                                        ) {
                                            sheetSortFilter = SortFilterSheet()
                                            sheetSortFilter.showNow(
                                                supportFragmentManager,
                                                "SORTFILTER_SHEET"
                                            )
                                        }
                                    }
                                }
                                else                                                     ->
                                    TopBar(title = stringResource(id = R.string.app_name)) {}
                            }
                        },
                        bottomBar = {
                            AnimatedVisibility(
                                barVisible,
                                enter = slideInVertically { height -> height } + fadeIn(),
                                exit = slideOutVertically { height -> height } + fadeOut(),
                            ) {
                                PagerNavBar(pageItems = pages, pagerState = pagerState)
                            }
                        }
                    ) { paddingValues ->
                        LaunchedEffect(key1 = viewModel) {
                            if (intent.extras != null) {
                                val destination =
                                    intent.extras!!.getString(
                                        classAddress(".fragmentNumber"),
                                        NavItem.Welcome.destination
                                    )
                                moveTo(destination)
                            }
                        }

                        MainNavHost(
                            modifier = Modifier
                                .padding(paddingValues),
                            navController = navController,
                            pagerState,
                            pages
                        )
                    }
                }
            }
        }

        if (doIntent(intent))
            return
    }

    override fun onResume() {
        OABX.main = this
        super.onResume()
        if (!(hasStoragePermissions && isStorageDirSetAndOk &&
                    checkSMSMMSPermission &&
                    checkCallLogsPermission &&
                    checkContactsPermission &&
                    checkUsageStatsPermission &&
                    postNotificationsPermission &&
                    (persist_ignoreBatteryOptimization.value
                            || powerManager.isIgnoringBatteryOptimizations(packageName)
                            )
                    )
        ) navController.navigate(NavItem.Permissions.destination)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        OABX.viewModelSaved = viewModel
        OABX.mainSaved = OABX.main
        OABX.main = null
        super.onDestroy()
    }

    @Deprecated("Deprecated in Java")   //TDOD hg42 why? how to handle now?
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
                addInfoLogText("Main: command '$command'")
            }
        }
        return false
    }

    private fun showEncryptionDialog() {
        val dontShowAgain = isEncryptionEnabled()
        if (dontShowAgain) return
        val dontShowCounter = persist_skippedEncryptionCounter.value
        if (dontShowCounter > 30) return    // don't increment further (useless touching file)
        persist_skippedEncryptionCounter.value = dontShowCounter + 1
        if (dontShowCounter % 10 == 0) {
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

    fun refreshPackagesAndBackups() {
        CoroutineScope(Dispatchers.IO).launch {
            invalidateBackupLocation()
        }
    }

    fun refreshPackages() {
        CoroutineScope(Dispatchers.IO).launch {
            updateAppTables()
        }
    }

    fun showSnackBar(message: String) {
    }

    fun dismissSnackBar() {
    }

    fun moveTo(destination: String) {
        persist_beenWelcomed.value = destination != NavItem.Welcome.destination
        navController.navigate(destination)
    }

    fun showBatchPrefsSheet(backupBoolean: Boolean) {
        sheetBatchPrefs = BatchPrefsSheet(backupBoolean)
        sheetBatchPrefs.showNow(
            supportFragmentManager,
            "SORTFILTER_SHEET"
        )
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
                    packageName = packageName,
                    mode = mode,
                    backupBoolean = backupBoolean,
                    notificationId = notificationId,
                    batchName = batchName,
                    immediate = true
                )
            worksList.add(oneTimeWorkRequest)

            val oneTimeWorkLiveData = WorkManager.getInstance(OABX.context)
                .getWorkInfoByIdLiveData(oneTimeWorkRequest.id)
            oneTimeWorkLiveData.observeForever(object : Observer<WorkInfo> {    //TODO WECH hg42
                override fun onChanged(t: WorkInfo) {
                    if (t.state == WorkInfo.State.SUCCEEDED) {
                        counter += 1

                        val (succeeded, packageLabel, error) = AppActionWork.getOutput(t)
                        if (error.isNotEmpty()) errors =
                            "$errors$packageLabel: ${      //TODO hg42 add to WorkHandler
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

        if (worksList.isNotEmpty()) {
            WorkManager.getInstance(OABX.context)
                .beginWith(worksList)
                .enqueue()
        }
    }

    fun startBatchRestoreAction(
        packages: List<String>,
        selectedApk: Map<String, Int>,
        selectedData: Map<String, Int>,
    ) {
        val now = System.currentTimeMillis()
        val notificationId = now.toInt()
        val batchType = getString(R.string.restore)
        val batchName = WorkHandler.getBatchName(batchType, now)

        val selectedItems = buildList {
            packages.forEach { pn ->
                when {
                    selectedApk[pn] == selectedData[pn] && selectedApk[pn] != null -> add(
                        Triple(pn, selectedApk[pn]!!, altModeToMode(ALT_MODE_BOTH, false))
                    )
                    else                                                           -> {
                        if ((selectedApk[pn] ?: -1) != -1) add(
                            Triple(pn, selectedApk[pn]!!, altModeToMode(ALT_MODE_APK, false))
                        )
                        if ((selectedData[pn] ?: -1) != -1) add(
                            Triple(pn, selectedData[pn]!!, altModeToMode(ALT_MODE_DATA, false))
                        )
                    }
                }
            }
        }

        var errors = ""
        var resultsSuccess = true
        var counter = 0
        val worksList: MutableList<OneTimeWorkRequest> = mutableListOf()
        OABX.work.beginBatch(batchName)
        selectedItems.forEach { (packageName, bi, mode) ->
            val oneTimeWorkRequest = AppActionWork.Request(
                packageName = packageName,
                mode = mode,
                backupBoolean = false,
                backupIndex = bi,
                notificationId = notificationId,
                batchName = batchName,
                immediate = true,
            )
            worksList.add(oneTimeWorkRequest)

            val oneTimeWorkLiveData = WorkManager.getInstance(OABX.context)
                .getWorkInfoByIdLiveData(oneTimeWorkRequest.id)
            oneTimeWorkLiveData.observeForever(object : Observer<WorkInfo> {
                override fun onChanged(t: WorkInfo) {
                    if (t.state == WorkInfo.State.SUCCEEDED) {
                        counter += 1

                        val (succeeded, packageLabel, error) = AppActionWork.getOutput(t)
                        if (error.isNotEmpty()) errors =
                            "$errors$packageLabel: ${
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
    }
}
