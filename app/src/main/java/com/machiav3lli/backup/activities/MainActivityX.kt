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
import android.os.Process
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.machiav3lli.backup.BuildConfig
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.OABX.Companion.appsSuspendedChecked
import com.machiav3lli.backup.PREFS_CATCHUNCAUGHT
import com.machiav3lli.backup.PREFS_SKIPPEDENCRYPTION
import com.machiav3lli.backup.R
import com.machiav3lli.backup.databinding.ActivityMainXBinding
import com.machiav3lli.backup.dbs.ODatabase
import com.machiav3lli.backup.dbs.entity.AppExtras
import com.machiav3lli.backup.fragments.ProgressViewController
import com.machiav3lli.backup.fragments.RefreshViewController
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.ShellHandler.Companion.runAsRoot
import com.machiav3lli.backup.items.SortFilterModel
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.utils.getPrivateSharedPrefs
import com.machiav3lli.backup.utils.isEncryptionEnabled
import com.machiav3lli.backup.utils.isNeedRefresh
import com.machiav3lli.backup.utils.isRememberFiltering
import com.machiav3lli.backup.utils.itemIdToOrder
import com.machiav3lli.backup.utils.navigateLeft
import com.machiav3lli.backup.utils.navigateRight
import com.machiav3lli.backup.utils.setCustomTheme
import com.machiav3lli.backup.utils.showToast
import com.machiav3lli.backup.utils.sortFilterModel
import com.machiav3lli.backup.utils.sortOrder
import com.machiav3lli.backup.viewmodels.MainViewModel
import com.topjohnwu.superuser.Shell
import timber.log.Timber
import java.lang.ref.WeakReference
import kotlin.system.exitProcess


class MainActivityX : BaseActivity() {

    companion object {

        var activityRef: WeakReference<MainActivityX> = WeakReference(null)
        var activity: MainActivityX?
            get() {
                return activityRef.get()
            }
            set(activity) {
                activityRef = WeakReference(activity)
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

        if (OABX.prefFlag(PREFS_CATCHUNCAUGHT, true)) {
            Thread.setDefaultUncaughtExceptionHandler { _, e ->
                try {
                    val maxCrashLines = OABX.prefInt("maxCrashLines", 100)
                    LogsHandler.unhandledException(e)
                    LogsHandler(context).writeToLogFile(
                        "uncaught exception happened:\n\n" +
                                "\n${BuildConfig.APPLICATION_ID} ${BuildConfig.VERSION_NAME}"
                                + "\n" +
                                runAsRoot(
                                    "logcat -d -t $maxCrashLines --pid=${Process.myPid()}"  // -d = dump and exit
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
                } catch (e: Throwable) {
                    // ignore
                } finally {
                    exitProcess(2)
                }
            }
        }

        Shell.getShell()

        binding = ActivityMainXBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        val database = ODatabase.getInstance(this)
        prefs = getPrivateSharedPrefs()

        val viewModelFactory = MainViewModel.Factory(database, application)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
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

    override fun onStart() {
        super.onStart()
        setupOnClicks()
        setupNavigation()
    }

    override fun onResume() {
        super.onResume()
        if (isNeedRefresh) {
            viewModel.refreshList()
            isNeedRefresh = false
        }
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
        Timber.i("Command: command $command")
        when (command) {
            null -> {
                // ignore?
            }
            else -> {
                activity?.showToast("Main: unknown command '$command'")
            }
        }
        return false
    }

    private fun setupNavigation() {
        try {
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
        } catch (e: ClassCastException) {
            finish()
            startActivity(intent)
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
