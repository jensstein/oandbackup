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
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import com.machiav3lli.backup.Constants
import com.machiav3lli.backup.Constants.classTag
import com.machiav3lli.backup.R
import com.machiav3lli.backup.activities.PrefsActivity
import com.machiav3lli.backup.handler.BackendController.getApplicationList
import com.machiav3lli.backup.handler.NotificationHelper.showNotification
import com.machiav3lli.backup.handler.ShellCommands
import com.machiav3lli.backup.handler.ShellCommands.ShellActionFailedException
import com.machiav3lli.backup.items.AppInfoX
import com.machiav3lli.backup.utils.FileUtils.BackupLocationIsAccessibleException
import com.machiav3lli.backup.utils.PrefUtils.StorageLocationNotConfiguredException
import com.machiav3lli.backup.utils.UIUtils.showError
import java.util.*

class PrefsToolsFragment : PreferenceFragmentCompat() {
    private var appInfoList: List<AppInfoX> = ArrayList()
    var pref: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_tools, rootKey)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val users = requireActivity().intent.getStringArrayListExtra("com.machiav3lli.backup.users")
        val shellCommands = ShellCommands(users)
        pref = findPreference(Constants.PREFS_QUICK_REBOOT)
        pref!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { onClickQuickReboot(shellCommands) }
        pref = findPreference(Constants.PREFS_BATCH_DELETE)
        pref!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { onClickBatchDelete() }
        pref = findPreference(Constants.PREFS_LOGVIEWER)
        pref!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { launchFragment(LogsFragment()) }
    }

    override fun onResume() {
        super.onResume()
        Thread {
            try {
                appInfoList = getApplicationList(requireContext())
            } catch (e: BackupLocationIsAccessibleException) {
                e.printStackTrace()
            } catch (e: StorageLocationNotConfiguredException) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun onClickQuickReboot(shellCommands: ShellCommands): Boolean {
        AlertDialog.Builder(requireActivity())
                .setTitle(R.string.prefs_quickreboot)
                .setMessage(R.string.quickRebootMessage)
                .setPositiveButton(R.string.dialogYes) { _: DialogInterface?, _: Int ->
                    try {
                        shellCommands.quickReboot()
                    } catch (e: ShellActionFailedException) {
                        showError(requireActivity(), e.message)
                    }
                }
                .setNegativeButton(R.string.dialogNo, null)
                .show()
        return true
    }

    private fun onClickBatchDelete(): Boolean {
        val deleteList = ArrayList<AppInfoX>()
        val message = StringBuilder()
        if (appInfoList.isNotEmpty()) {
            for (appInfo in appInfoList) {
                if (!appInfo.isInstalled) {
                    deleteList.add(appInfo)
                    message.append(appInfo.packageLabel).append("\n")
                }
            }
        }
        if (deleteList.isNotEmpty()) {
            AlertDialog.Builder(requireContext())
                    .setTitle(R.string.prefs_batchdelete)
                    .setMessage(message.toString().trim { it <= ' ' })
                    .setPositiveButton(R.string.dialogYes) { _: DialogInterface?, _: Int ->
                        changesMade()
                        Thread { deleteBackups(deleteList) }.start()
                    }
                    .setNegativeButton(R.string.dialogNo, null)
                    .show()
        } else {
            Toast.makeText(requireActivity(), getString(R.string.batchDeleteNothingToDelete), Toast.LENGTH_LONG).show()
        }
        return true
    }

    private fun launchFragment(fragment: Fragment): Boolean {
        requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.prefsFragment, fragment)
                .addToBackStack(null)
                .commit()
        return true
    }

    private fun changesMade() {
        val result = Intent()
        result.putExtra("changesMade", true)
        requireActivity().setResult(RESULT_OK, result)
    }

    private fun deleteBackups(deleteList: List<AppInfoX>) {
        deleteList.forEach { appInfo ->
            runOnUiThread { Toast.makeText(requireContext(), "${appInfo.packageLabel}: ${getString(R.string.batchDeleteMessage)}", Toast.LENGTH_SHORT).show() }
            Log.i(TAG, "deleting backups of ${appInfo.packageLabel}")
            appInfo.deleteAllBackups()
            appInfo.refreshBackupHistory()
        }
        showNotification(requireContext(), PrefsActivity::class.java, System.currentTimeMillis().toInt(), getString(R.string.batchDeleteNotificationTitle), getString(R.string.batchDeleteBackupsDeleted) + " " + deleteList.size, false)
    }

    companion object {
        private val TAG = classTag(".PrefsToolsFragment")
        private const val RESULT_OK = 0
    }
}