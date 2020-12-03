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
import com.machiav3lli.backup.handler.NotificationHandler.showNotification
import com.machiav3lli.backup.items.AppInfo
import com.machiav3lli.backup.utils.FileUtils.BackupLocationIsAccessibleException
import com.machiav3lli.backup.utils.StorageLocationNotConfiguredException

class PrefsToolsFragment : PreferenceFragmentCompat() {
    private val TAG = classTag(".PrefsToolsFragment")
    private var appInfoList: List<AppInfo> = ArrayList()

    private lateinit var pref: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_tools, rootKey)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        pref = findPreference(Constants.PREFS_BATCH_DELETE)!!
        pref.onPreferenceClickListener = Preference.OnPreferenceClickListener { onClickBatchDelete() }
        pref = findPreference(Constants.PREFS_LOGVIEWER)!!
        pref.onPreferenceClickListener = Preference.OnPreferenceClickListener { launchFragment(LogsFragment()) }
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

    private fun onClickBatchDelete(): Boolean {
        val deleteList = ArrayList<AppInfo>()
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

    private fun deleteBackups(deleteList: List<AppInfo>) {
        deleteList.forEach {
            runOnUiThread { Toast.makeText(requireContext(), "${it.packageLabel}: ${getString(R.string.batchDeleteMessage)}", Toast.LENGTH_SHORT).show() }
            Log.i(TAG, "deleting backups of ${it.packageLabel}")
            it.deleteAllBackups(requireContext())
        }
        showNotification(requireContext(), PrefsActivity::class.java, System.currentTimeMillis().toInt(), getString(R.string.batchDeleteNotificationTitle), getString(R.string.batchDeleteBackupsDeleted) + " " + deleteList.size, false)
    }
}