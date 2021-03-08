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
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import com.machiav3lli.backup.*
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.activities.PrefsActivity
import com.machiav3lli.backup.handler.BackendController.getApplicationList
import com.machiav3lli.backup.handler.BackupRestoreHelper
import com.machiav3lli.backup.handler.showNotification
import com.machiav3lli.backup.items.AppInfo
import com.machiav3lli.backup.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.BufferedOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime

class PrefsToolsFragment : PreferenceFragmentCompat() {
    private var appInfoList: List<AppInfo> = ArrayList()

    private lateinit var pref: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_tools, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pref = findPreference(PREFS_BATCH_DELETE)!!
        pref.onPreferenceClickListener = Preference.OnPreferenceClickListener { onClickBatchDelete() }
        pref = findPreference(PREFS_COPYSELF)!!
        pref.onPreferenceClickListener = Preference.OnPreferenceClickListener { onClickCopySelf() }
        pref = findPreference(PREFS_SAVEAPPSLIST)!!
        pref.onPreferenceClickListener = Preference.OnPreferenceClickListener { onClickSaveAppsList() }
        pref = findPreference(PREFS_LOGVIEWER)!!
        pref.onPreferenceClickListener = Preference.OnPreferenceClickListener { launchFragment(LogsFragment()) }
    }

    override fun onResume() {
        super.onResume()
        Thread {
            try {
                appInfoList = getApplicationList(requireContext())
            } catch (e: FileUtils.BackupLocationIsAccessibleException) {
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

    private fun onClickCopySelf(): Boolean {
        try {
            GlobalScope.launch(Dispatchers.IO) {
                if (BackupRestoreHelper.copySelfApk(requireContext(), MainActivityX.shellHandlerInstance!!))
                    showNotification(requireContext(), PrefsActivity::class.java, System.currentTimeMillis().toInt(),
                            getString(R.string.copyOwnApkSuccess), "", false)
                else
                    showNotification(requireContext(), PrefsActivity::class.java, System.currentTimeMillis().toInt(),
                            getString(R.string.copyOwnApkFailed), "", false)
            }
        } catch (e: IOException) {
            Timber.e("${getString(R.string.copyOwnApkFailed)}: $e")
        } finally {
            return true
        }
    }

    private fun onClickSaveAppsList(): Boolean {
        if (appInfoList.isNotEmpty()) {
            AlertDialog.Builder(requireContext())
                    .setTitle(R.string.prefs_saveappslist)
                    .setPositiveButton(R.string.radio_all) { _: DialogInterface, _: Int ->
                        writeAppsListFile(appInfoList.filter { it.isSystem }.map { "${it.packageLabel}: ${it.packageName}" })
                    }
                    .setNeutralButton(R.string.filtered_list) { _: DialogInterface, _: Int ->
                        writeAppsListFile(applyFilter(appInfoList,
                                getFilterPreferences(requireContext()).toString(), requireContext())
                                .map { "${it.packageLabel}: ${it.packageName}" })
                    }
                    .setNegativeButton(R.string.dialogNo, null)
                    .show()
        } else {
            Toast.makeText(requireActivity(), getString(R.string.noAppsListToSave), Toast.LENGTH_LONG).show()
        }
        return true
    }

    @Throws(IOException::class)
    fun writeAppsListFile(appsList: List<String>) {
        val date = LocalDateTime.now()
        val filesText = appsList.joinToString("\n")
        val fileName = "${BACKUP_DATE_TIME_FORMATTER.format(date)}.appslist"
        val listFile = getBackupRoot(requireContext()).createFile("application/octet-stream", fileName)
        BufferedOutputStream(requireContext().contentResolver.openOutputStream(listFile?.uri
                ?: Uri.EMPTY, "w"))
                .use { it.write(filesText.toByteArray(StandardCharsets.UTF_8)) }
        Timber.i("Wrote apps\' list file at $date")
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
            Timber.i("deleting backups of ${it.packageLabel}")
            it.deleteAllBackups(requireContext())
        }
        showNotification(requireContext(), PrefsActivity::class.java, System.currentTimeMillis().toInt(),
                getString(R.string.batchDeleteNotificationTitle), "${getString(R.string.batchDeleteBackupsDeleted)} ${deleteList.size}", false)
    }
}