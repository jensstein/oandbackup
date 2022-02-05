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

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Looper
import android.os.PersistableBundle
import android.os.Process
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.machiav3lli.backup.*
import com.machiav3lli.backup.OABX.Companion.appsSuspendedChecked
import com.machiav3lli.backup.databinding.ActivityMainXBinding
import com.machiav3lli.backup.dbs.AppExtras
import com.machiav3lli.backup.dbs.AppExtrasDatabase
import com.machiav3lli.backup.dbs.BlocklistDatabase
import com.machiav3lli.backup.fragments.ProgressViewController
import com.machiav3lli.backup.fragments.RefreshViewController
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.ShellHandler.Companion.runAsRoot
import com.machiav3lli.backup.items.SortFilterModel
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.services.CommandReceiver
import com.machiav3lli.backup.tasks.AppActionWork
import com.machiav3lli.backup.utils.*
import com.machiav3lli.backup.viewmodels.MainViewModel
import com.machiav3lli.backup.viewmodels.MainViewModelFactory
import com.topjohnwu.superuser.Shell
import timber.log.Timber
import java.lang.ref.WeakReference


class MainActivityX : BaseActivity() {

    companion object {

        var activityRef : WeakReference<MainActivityX> = WeakReference(null)
        var activity : MainActivityX?
            get() {
                return activityRef.get()
            }
            set(activity) {
                activityRef = WeakReference(activity)
            }

        var statusNotificationId = 0

        class Counters(
            var startTime: Long = 0L,

            var workCount: Int = 0,
            var workEnqueued: Int = 0,
            var workBlocked: Int = 0,
            var workRunning: Int = 0,
            var workFinished: Int = 0,
            var workAttempts: Int = 0,
            var workRetries: Int = 0,

            var running: Int = 0,
            var queued: Int = 0,
            var shortText: String = "",
            var bigText: String = "",
            var retries: Int = 0,
            var maxRetries: Int = 0,
            var succeeded: Int = 0,
            var failed: Int = 0,
            var canceled: Int = 0
        )

        fun showRunningStatus(manager: WorkManager? = null, work: MutableList<WorkInfo>? = null) {
            if(manager == null || work == null)
                return

            if (statusNotificationId == 0)
                statusNotificationId = System.currentTimeMillis().toInt()

            var batches = mutableMapOf<String, Counters>()

            val appContext = OABX.context
            val workInfos = manager.getWorkInfosByTag(
                AppActionWork::class.qualifiedName!!
            ).get()

            Thread {
                workInfos.forEach { info ->
                    var data = info.progress
                    if(data.getString("batchName").isNullOrEmpty())
                        data = info.outputData
                    var batchName = data.getString("batchName")
                    var packageName = data.getString("packageName")
                    var packageLabel = data.getString("packageLabel")
                    var backupBoolean = data.getBoolean("backupBoolean", true)
                    var operation = data.getString("operation")
                    var failures = data.getInt("failures", -1)

                    val maxRetries = OABX.prefInt("maxRetriesPerPackage", 3)

                    //Timber.d("%%%%% $batchName $packageName $operation $backupBoolean ${info.state} fail=$failures max=$maxRetries")

                    if(batchName.isNullOrEmpty()) {
                        info.tags.forEach {
                            val parts = it.toString().split(':', limit = 2)
                            if(parts.size > 1) {
                                val (key, value) = parts
                                when (key) {
                                    "name" -> {
                                        batchName = value
                                        //Timber.d("%%%%% name from tag -> $batchName")
                                        return@forEach
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }
                    if(batchName.isNullOrEmpty()) {
                        Timber.d("????????????????????????????????????????? empty batch name, canceling")
                        when(info.state) {
                            WorkInfo.State.CANCELLED ->
                                batchName = "CANCELLED"
                            WorkInfo.State.ENQUEUED  ->
                                batchName = "ENQUEUED"
                            else -> {
                                batchName = "UNDEF"
                            }
                        }
                        Timber.d("?????????????????????????? name from state -> $batchName (to be canceled)")
                        manager.cancelWorkById(info.id)
                    }

                    //Timber.d("===== $batchName $packageName $operation $backupBoolean ${info.state} fail=$failures max=$maxRetries")

                    batches.getOrPut(batchName!!) { Counters() }.run {
                        if(startTime == 0L)
                            startTime = System.currentTimeMillis()

                        workCount++
                        workAttempts = info.runAttemptCount
                        if (info.runAttemptCount > 1)
                            workRetries++
                        if (failures > 1) {
                            retries++
                            if (failures > maxRetries)
                                this.maxRetries++
                        }

                        when(info.state) {
                            WorkInfo.State.SUCCEEDED -> {
                                succeeded++
                                workFinished++
                            }
                            WorkInfo.State.FAILED -> {
                                failed++
                                workFinished++
                            }
                            WorkInfo.State.CANCELLED -> {
                                canceled++
                                workFinished++
                            }
                            WorkInfo.State.ENQUEUED -> {
                                queued++
                                workEnqueued++
                            }
                            WorkInfo.State.BLOCKED -> {
                                queued++
                                workBlocked++
                            }
                            WorkInfo.State.RUNNING -> {
                                workRunning++
                                when (operation) {
                                    "..." -> queued++
                                    else -> {
                                        running++
                                        if (!packageName.isNullOrEmpty() and !operation.isNullOrEmpty())
                                            bigText += "${
                                                if (backupBoolean) "B" else "R"
                                            }${
                                                if(workRetries>0) " ${workRetries}" else ""
                                            } $operation : $packageName\n"
                                    }
                                }
                            }
                        }
                    }
                }

                var allProcessed = 0
                var allRemaining = 0
                var allCount = 0

                batches.forEach { batchName, counters ->
                    counters.run {
                        val notificationId = batchName.hashCode()

                        val processed = succeeded + failed
                        allProcessed += processed
                        val remaining = running + queued
                        allRemaining += remaining
                        allCount += workCount

                        var title = "$batchName"
                        shortText = "✔$succeeded${if(failed>0) "❓$failed" else ""}/$workCount"
                        if(remaining > 0)
                            shortText += " 🏃$running 👭${queued}"
                        else {
                            shortText += " ${OABX.context.getString(R.string.finished)}"

                            val duration = ((System.currentTimeMillis() - startTime) / 1000 + 0.5).toInt()
                            if(duration > 10) {
                                val min = (duration / 60).toInt()
                                val sec = duration - min*60
                                bigText = "$min min $sec sec"
                            }
                            startTime = 0L
                        }
                        if(canceled > 0)
                            shortText += " 🚫$canceled"
                        bigText = "$shortText\n$bigText"

                        Timber.d("%%%%% -----------------> $title $shortText")

                        if(workCount>0) {
                            val notificationManager = NotificationManagerCompat.from(appContext)
                            val notificationChannel = NotificationChannel(
                                classAddress("NotificationHandler"),
                                classAddress("NotificationHandler"),
                                NotificationManager.IMPORTANCE_LOW
                            )
                            notificationManager.createNotificationChannel(notificationChannel)
                            val resultIntent = Intent(appContext, MainActivityX::class.java)
                            resultIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                            val resultPendingIntent = PendingIntent.getActivity(
                                appContext,
                                0,
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )

                            var notificationBuilder =
                                NotificationCompat.Builder(
                                    appContext,
                                    classAddress("NotificationHandler")
                                )
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                    .setSmallIcon(R.drawable.ic_app)
                                    .setContentTitle(title)
                                    .setStyle(
                                        NotificationCompat.BigTextStyle().bigText(bigText)
                                    )
                                    .setContentText(shortText)
                                    .setAutoCancel(true)
                                    .setContentIntent(resultPendingIntent)

                            if(remaining > 0) {
                                val cancelIntent = Intent(appContext, CommandReceiver::class.java).apply {
                                    action = "cancel"
                                    putExtra("name", batchName)
                                }
                                val cancelPendingIntent = PendingIntent.getBroadcast(
                                    appContext,
                                    batchName.hashCode(),
                                    cancelIntent,
                                    PendingIntent.FLAG_IMMUTABLE
                                )
                                val cancelAllIntent = Intent(appContext, CommandReceiver::class.java).apply {
                                    action = "cancel"
                                    //putExtra("name", "")
                                }
                                val cancelAllPendingIntent = PendingIntent.getBroadcast(
                                    appContext,
                                    "ALL".hashCode(),
                                    cancelAllIntent,
                                    PendingIntent.FLAG_IMMUTABLE
                                )
                                notificationBuilder
                                    .setProgress(workCount, processed, false)
                                    .addAction(
                                        R.drawable.ic_close,
                                        appContext.getString(R.string.dialogCancel),
                                        cancelPendingIntent
                                    )
                                    .addAction(
                                        R.drawable.ic_close,
                                        "Cancel all",
                                        cancelAllPendingIntent
                                    )
                            }

                            val notification = notificationBuilder.build()
                            notificationManager.notify(notificationId, notification)
                            //activity.updateProgress(processed, workCount)

                            if (remaining <= 0) {
                                //activity.hideProgress()
                                // don't remove notification, the result may be interesting for the user
                                //notificationManager.cancel(statusNotificationId)
                                //statusNotificationId = 0
                            }
                        }
                    }
                }
                if (allRemaining > 0) {
                    Timber.d("%%%%% $allProcessed < $allRemaining < $allCount")
                    activity?.runOnUiThread { activity?.updateProgress(allProcessed, allCount) }
                } else {
                    Timber.d("%%%%% HIDE PROGRESS")
                    activity?.runOnUiThread { activity?.hideProgress() }
                    Timber.d("%%%%% PRUNE")
                    OABX.work.prune()
                }
            }.start()
        }
    }

    private lateinit var prefs: SharedPreferences
    private lateinit var refreshViewController: RefreshViewController
    private lateinit var progressViewController: ProgressViewController

    lateinit var binding: ActivityMainXBinding
    lateinit var viewModel: MainViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        val context = this
        activity = this
        OABX.activity = this

        setCustomTheme()
        super.onCreate(savedInstanceState)

        appsSuspendedChecked = false

        if (OABX.prefFlag("catchUncaughtException", true)) {
            Thread.setDefaultUncaughtExceptionHandler { thread, e ->
                try {
                    LogsHandler.unhandledException(e)
                    LogsHandler(context).writeToLogFile(
                        "uncaught exception happened:\n\n" +
                                "\n${BuildConfig.APPLICATION_ID} ${BuildConfig.VERSION_NAME}"
                                + "\n" +
                                runAsRoot(
                                    "logcat -d --pid=${Process.myPid()}"  // -d = dump and exit
                                ).out.joinToString("\n")
                    )
                    object : Thread() {
                        override fun run() {
                            Looper.prepare()
                            repeat(5) {
                                Toast.makeText(
                                    activity,
                                    "Uncaught Exception\n${e.message}\nrestarting application...",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            Looper.loop()
                        }
                    }.start()
                    Thread.sleep(5000)
                } finally {
                    System.exit(2)
                }
            }
        }

        Shell.getShell()
        binding = ActivityMainXBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        val blocklistDao = BlocklistDatabase.getInstance(this).blocklistDao
        val appExtrasDao = AppExtrasDatabase.getInstance(this).appExtrasDao
        prefs = getPrivateSharedPrefs()

        val viewModelFactory = MainViewModelFactory(appExtrasDao, blocklistDao, application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
        if (!isRememberFiltering) {
            this.sortFilterModel = SortFilterModel()
            this.sortOrder = false
        }
        viewModel.blocklist.observe(this) {
            viewModel.refreshList()
        }
        viewModel.refreshNow.observe(this) {
            if (it) refreshView()
        }
        runOnUiThread { showEncryptionDialog() }
        setContentView(binding.root)

        if (doIntent(intent))
            return
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        setupOnClicks()
        setupNavigation()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        if (isNeedRefresh) {
            viewModel.refreshList()
            isNeedRefresh = false
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onRestart() {
        super.onRestart()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onBackPressed() {
        finishAffinity()
    }

    override fun onNewIntent(intent: Intent?) {
        doIntent(intent)
        super.onNewIntent(intent)
    }

    fun doIntent(intent: Intent?): Boolean {
        if (intent != null) {
            val command = intent.action
            when (command) {
                else -> {
                    activity?.showToast("unknown command '$command'")
                }
            }
        }
        return false
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigation.setOnItemSelectedListener { item: MenuItem ->
            if (item.itemId == binding.bottomNavigation.selectedItemId) return@setOnItemSelectedListener false
            if (binding.bottomNavigation.selectedItemId.itemIdToOrder() < item.itemId.itemIdToOrder())
                navController.navigateRight(item.itemId)
            else
                navController.navigateLeft(item.itemId)
            true
        }
    }

    private fun setupOnClicks() {
        binding.buttonSettings.setOnClickListener {
            viewModel.appInfoList.value?.let { OABX.app.cache.put("appInfoList", it) }
            startActivity(
                Intent(applicationContext, PrefsActivity::class.java)
            )
        }
    }

    private fun showEncryptionDialog() {
        val dontShowAgain = isEncryptionEnabled()
        if (dontShowAgain) return
        val dontShowCounter = prefs.getInt(PREFS_SKIPPEDENCRYPTION, 0)
        prefs.edit().putInt(PREFS_SKIPPEDENCRYPTION, dontShowCounter + 1).apply()
        if (dontShowCounter % 10 == 0) {
            AlertDialog.Builder(this)
                .setTitle(R.string.enable_encryption_title)
                .setMessage(R.string.enable_encryption_message)
                .setPositiveButton(R.string.dialog_approve) { _: DialogInterface?, _: Int ->
                    startActivity(
                        Intent(applicationContext, PrefsActivity::class.java).putExtra(
                            ".toEncryption",
                            true
                        )
                    )
                }
                .show()
        }
    }

    fun updatePackage(packageName: String) {
        StorageFile.invalidateCache()
        viewModel.updatePackage(packageName)
    }

    fun updateAppExtras(appExtras: AppExtras) {
        viewModel.updateExtras(appExtras)
    }

    fun setRefreshViewController(refreshViewController: RefreshViewController) {
        this.refreshViewController = refreshViewController
    }

    fun refreshView() {
        if (::refreshViewController.isInitialized) refreshViewController.refreshView()
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
        binding.snackbarText.apply {
            text = message
            visibility = View.VISIBLE
        }
    }

    fun dismissSnackBar() {
        binding.snackbarText.visibility = View.GONE
    }

    fun whileShowingSnackBar(message: String, todo: () -> Unit) {
        activity?.runOnUiThread {
            activity?.showSnackBar(message)
        }
        todo()
        activity?.runOnUiThread {
            activity?.dismissSnackBar()
        }
    }

}
