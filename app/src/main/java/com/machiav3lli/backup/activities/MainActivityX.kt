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
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.machiav3lli.backup.MAIN_FILTER_DEFAULT
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.OABX.Companion.addInfoText
import com.machiav3lli.backup.R
import com.machiav3lli.backup.dialogs.PackagesListDialogFragment
import com.machiav3lli.backup.fragments.BatchPrefsSheet
import com.machiav3lli.backup.fragments.SortFilterSheet
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.WorkHandler
import com.machiav3lli.backup.pref_catchUncaughtException
import com.machiav3lli.backup.pref_uncaughtExceptionsJumpToPreferences
import com.machiav3lli.backup.preferences.persist_skippedEncryptionCounter
import com.machiav3lli.backup.preferences.pref_blackTheme
import com.machiav3lli.backup.tasks.AppActionWork
import com.machiav3lli.backup.tasks.FinishWork
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.ArrowsClockwise
import com.machiav3lli.backup.ui.compose.icons.phosphor.FunnelSimple
import com.machiav3lli.backup.ui.compose.icons.phosphor.GearSix
import com.machiav3lli.backup.ui.compose.icons.phosphor.Prohibit
import com.machiav3lli.backup.ui.compose.item.ActionChip
import com.machiav3lli.backup.ui.compose.item.ExpandableSearchAction
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
import com.machiav3lli.backup.utils.getDefaultSharedPreferences
import com.machiav3lli.backup.utils.isEncryptionEnabled
import com.machiav3lli.backup.utils.setCustomTheme
import com.machiav3lli.backup.utils.sortFilterModel
import com.machiav3lli.backup.viewmodels.BatchViewModel
import com.machiav3lli.backup.viewmodels.MainViewModel
import com.machiav3lli.backup.viewmodels.SchedulerViewModel
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.system.exitProcess

fun Modifier.angledGradientBackground(colors: List<Color>, degrees: Float, factor: Float = 1f) =
    this.then(
        drawBehind {

            val (w, h) = size
            val dim = max(w, h) * factor

            val degreesNormalised = (degrees % 360).let { if (it < 0) it + 360 else it }

            val alpha = (degreesNormalised * PI / 180).toFloat()

            val centerOffsetX = cos(alpha) * dim / 2
            val centerOffsetY = sin(alpha) * dim / 2

            drawRect(
                brush = Brush.linearGradient(
                    colors = colors,
                    // negative here so that 0 degrees is left -> right
                    // and 90 degrees is top -> bottom
                    start = Offset(center.x - centerOffsetX, center.y - centerOffsetY),
                    end = Offset(center.x + centerOffsetX, center.y + centerOffsetY)
                ),
                size = size
            )
        }
    )

class MainActivityX : BaseActivity() {

    private val crScope: CoroutineScope = CoroutineScope(Dispatchers.Default)

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
        ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class,
        ExperimentalPagerApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        val context = this
        val mainChanged = (this != OABX.mainSaved)
        OABX.activity = this
        OABX.main = this

        var freshStart = (savedInstanceState == null)   //TODO use some lifecycle method?

        Timber.w(
            "======================================== activity ${
                classAndId(this)
            }${
                if (freshStart) ", fresh start" else ""
            }${
                if (mainChanged and (!freshStart or (OABX.mainSaved != null)))
                    ", main changed (was ${classAndId(OABX.mainSaved)})"
                else
                    ""
            }"
        )

        setCustomTheme()
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

        if (freshStart) {
            runOnUiThread { showEncryptionDialog() }
            //refreshPackages()
        }

        setContent {
            AppTheme {
                val pagerState = rememberPagerState()
                val navController = rememberAnimatedNavController()
                val pages = listOf(
                    NavItem.Home,
                    NavItem.Backup,
                    NavItem.Restore,
                    NavItem.Scheduler,
                )
                val currentPage by remember(pagerState.currentPage) { mutableStateOf(pages[pagerState.currentPage]) }   //TODO hg42 remove remember ???

                var query by rememberSaveable { mutableStateOf(viewModel.searchQuery.value) }
                //val query by viewModel.searchQuery.flow.collectAsState(viewModel.searchQuery.initial)  // doesn't work with rotate (not saveable)...
                val searchExpanded = query.isNotEmpty()

                Timber.d("compose: query = '$query'")

                Timber.d("search: ${viewModel.searchQuery.value} filter: ${viewModel.modelSortFilter.value}")

                if (freshStart) {
                    freshStart = false
                    LaunchedEffect(viewModel) {
                        traceBold { "******************** freshStart LaunchedEffect(viewModel) ********************" }
                        // it isn't necessary with MutableStateFlow under the hood
                        // keeping the compile conditions, even if they are always false if using MutableComposableStateFlow
                        //if (viewModel.searchQuery is MutableComposableSharedFlow<*>)
                        //    viewModel.searchQuery.value = ""
                        //if (viewModel.modelSortFilter is MutableComposableSharedFlow<*>)
                        //    viewModel.modelSortFilter.value = OABX.context.sortFilterModel

                        //refreshPackages()     // not a good idea -> using init { ... } in viewModel
                    }
                }

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
                            if (currentPage.destination == NavItem.Scheduler.destination)
                                TopBar(
                                    title = stringResource(id = currentPage.title)
                                ) {

                                    RoundButton(
                                        icon = Phosphor.Prohibit,
                                        description = stringResource(id = R.string.sched_blocklist)
                                    ) {
                                        GlobalScope.launch(Dispatchers.IO) {
                                            val blocklistedPackages = viewModel.blocklist.value
                                                .mapNotNull { it.packageName }

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
                            else Column() {
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
                                    RoundButton(
                                        description = stringResource(id = R.string.refresh),
                                        icon = Phosphor.ArrowsClockwise
                                    ) { refreshPackages() }
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
                                            val blocklistedPackages = viewModel.blocklist.value
                                                .mapNotNull { it.packageName }

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
                        },
                        bottomBar = {
                            PagerNavBar(pageItems = pages, pagerState = pagerState)
                        }
                    ) { paddingValues ->
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
        OABX.activity = this    // just in case 'this' object is recreated
        OABX.main = this
        super.onResume()
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
                addInfoText("Main: command '$command'")
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

    fun refreshView() {    //TODO hg42 is currently unused (and should always be?)
        crScope.launch { viewModel.modelSortFilter.flow.emit(sortFilterModel) }
    }

    fun refreshPackages() {
        invalidateBackupLocation()
    }

    fun showSnackBar(message: String) {
    }

    fun dismissSnackBar() {
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
        onSuccessfulFinish: Observer<WorkInfo>.(LiveData<WorkInfo>) -> Unit,
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
