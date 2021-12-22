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
import android.content.res.AssetManager
import android.os.Bundle
import android.os.Looper
import android.os.PersistableBundle
import android.os.Process
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.work.*
import com.machiav3lli.backup.*
import com.machiav3lli.backup.R
import com.machiav3lli.backup.actions.BaseAppAction
import com.machiav3lli.backup.databinding.ActivityMainXBinding
import com.machiav3lli.backup.dbs.AppExtras
import com.machiav3lli.backup.dbs.AppExtrasDatabase
import com.machiav3lli.backup.dbs.BlocklistDatabase
import com.machiav3lli.backup.fragments.ProgressViewController
import com.machiav3lli.backup.fragments.RefreshViewController
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.handler.ShellHandler.Companion.runAsRoot
import com.machiav3lli.backup.items.*
import com.machiav3lli.backup.utils.*
import com.machiav3lli.backup.viewmodels.MainViewModel
import com.machiav3lli.backup.viewmodels.MainViewModelFactory
import com.topjohnwu.superuser.Shell
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference


class MainActivityX : BaseActivity() {

    companion object {
        private val VERSION_FILE = "__version__"
        val ASSETS_SUBDIR = "assets"

        var shellHandlerInstance: ShellHandler? = null
            private set

        lateinit var assetDir : File
            private set

        fun initShellHandler() : Boolean {
            return try {
                shellHandlerInstance = ShellHandler()
                true
            } catch (e: ShellHandler.ShellCommandFailedException) {
                false
            }
        }

        var activityRef : WeakReference<MainActivityX> = WeakReference(null)
        var activity : MainActivityX?
            get() {
                return activityRef.get()
            }
            set(activity) {
                activityRef = WeakReference(activity)
            }

        var appsSuspendedChecked = false

        var runningOperations : MutableMap<String, String> = mutableMapOf()
    }

    private lateinit var prefs: SharedPreferences
    private lateinit var refreshViewController: RefreshViewController
    private lateinit var progressViewController: ProgressViewController

    lateinit var binding: ActivityMainXBinding
    lateinit var viewModel: MainViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        val context = this
        activity = this

        appsSuspendedChecked = false

        setCustomTheme()
        super.onCreate(savedInstanceState)
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
        viewModel.blocklist.observe(this, {
            viewModel.refreshList()
        })
        viewModel.refreshNow.observe(this, {
            if (it) refreshView()
        })
        initAssetFiles()
        initShell()
        runOnUiThread { showEncryptionDialog() }
        setContentView(binding.root)
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

    override fun onRestart() {
        super.onRestart()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onBackPressed() {
        finishAffinity()
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
            viewModel.appInfoList.value?.let { oabx.cache.put("appInfoList", it) }
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

    private fun initAssetFiles() {

        // copy scripts to file storage
        MainActivityX.activity?.let { context ->
            assetDir = File(context.filesDir, ASSETS_SUBDIR)
            assetDir.mkdirs()
            // don't copy if the files exist and are from the current app version
            val appVersion = BuildConfig.VERSION_NAME
            val version = try {
                File(assetDir, VERSION_FILE).readText()
            } catch (e: Throwable) {
                ""
            }
            if (version != appVersion) {
                try {
                    // cleans assetDir and copiers asset files
                    context.assets.copyRecursively("files", assetDir)
                    // additional generated files
                    File(assetDir, ShellHandler.EXCLUDE_FILE)
                        .writeText(BaseAppAction.DATA_EXCLUDED_DIRS.map { it + "\n" }
                            .joinToString(""))
                    File(assetDir, ShellHandler.EXCLUDE_CACHE_FILE)
                        .writeText(BaseAppAction.DATA_EXCLUDED_CACHE_DIRS.map { it + "\n" }
                            .joinToString(""))
                    // validate with version file if completed
                    File(assetDir, VERSION_FILE).writeText(appVersion)
                } catch (e: Throwable) {
                    Timber.w("cannot copy scripts to ${assetDir}")
                }
            }
        }
    }

    private fun initShell() {
        // Initialize the ShellHandler for further root checks
        if (!initShellHandler()) {
            showWarning(
                MainActivityX::class.java.simpleName,
                getString(R.string.shell_initproblem)
            ) { _: DialogInterface?, _: Int -> finishAffinity() }
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
}


fun AssetManager.copyRecursively(assetPath: String, targetFile: File) {
    list(assetPath)?.let { list ->
        if (list.isEmpty()) { // assetPath is file
            open(assetPath).use { input ->
                FileOutputStream(targetFile.absolutePath).use { output ->
                    input.copyTo(output)
                    output.flush()
                }
            }

        } else { // assetPath is folder
            targetFile.deleteRecursively()
            targetFile.mkdir()

            list.forEach {
                copyRecursively("$assetPath/$it", File(targetFile, it))
            }
        }
    }
}
