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
import com.machiav3lli.backup.*
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.activities.PrefsActivity
import com.machiav3lli.backup.handler.BackupRestoreHelper
import com.machiav3lli.backup.handler.ExportsHandler
import com.machiav3lli.backup.handler.showNotification
import com.machiav3lli.backup.items.AppInfo
import com.machiav3lli.backup.utils.applyFilter
import com.machiav3lli.backup.utils.getBackupDir
import com.machiav3lli.backup.utils.sortFilterModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.BufferedOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime

// TODO hide navBar on launching tools' fragments
class PrefsToolsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_tools, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findPreference<Preference>(PREFS_BATCH_DELETE)?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { onClickUninstalledBackupsDelete() }
        findPreference<Preference>(PREFS_COPYSELF)?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { onClickCopySelf() }
        findPreference<Preference>(PREFS_SCHEDULESEXPORTIMPORT)?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { onClickSchedulesExportImport() }
        findPreference<Preference>(PREFS_SAVEAPPSLIST)?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { onClickSaveAppsList() }
        findPreference<Preference>(PREFS_LOGVIEWER)?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { launchFragment(LogsFragment()) }
    }

    override fun onResume() {
        super.onResume()
        requirePrefsActivity().refreshAppsList()
    }

    private fun onClickUninstalledBackupsDelete(): Boolean {
        val deleteList = ArrayList<AppInfo>()
        val message = StringBuilder()
        if (requirePrefsActivity().appInfoList.isNotEmpty()) {
            for (appInfo in requirePrefsActivity().appInfoList) {
                if (!appInfo.isInstalled) {
                    deleteList.add(appInfo)
                    message.append(appInfo.packageLabel).append("\n")
                }
            }
        }
        if (requirePrefsActivity().appInfoList.isNotEmpty()) {
            if (deleteList.isNotEmpty()) {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.prefs_batchdelete)
                    .setMessage(message.toString().trim { it <= ' ' })
                    .setPositiveButton(R.string.dialogYes) { _: DialogInterface?, _: Int ->
                        deleteBackups(deleteList)
                        requirePrefsActivity().refreshAppsList()
                    }
                    .setNegativeButton(R.string.dialogNo, null)
                    .show()
            } else {
                Toast.makeText(
                    requireActivity(),
                    getString(R.string.batchDeleteNothingToDelete),
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(
                requireActivity(),
                getString(R.string.wait_noappslist),
                Toast.LENGTH_LONG
            ).show()
        }
        return true
    }

    private fun deleteBackups(deleteList: List<AppInfo>) {
        val notificationId = System.currentTimeMillis().toInt()
        deleteList.forEachIndexed { i, ai ->
            showNotification(
                requireContext(),
                PrefsActivity::class.java,
                notificationId,
                "${getString(R.string.batchDeleteMessage)} ($i/${deleteList.size})",
                ai.packageLabel,
                false
            )
            Timber.i("deleting backups of ${ai.packageLabel}")
            ai.deleteAllBackups(requireContext())
        }
        showNotification(
            requireContext(),
            PrefsActivity::class.java,
            notificationId,
            getString(R.string.batchDeleteNotificationTitle),
            "${getString(R.string.batchDeleteBackupsDeleted)} ${deleteList.size}",
            false
        )
    }

    private fun onClickCopySelf(): Boolean {
        try {
            GlobalScope.launch(Dispatchers.IO) {
                if (BackupRestoreHelper.copySelfApk(
                        requireContext(),
                        MainActivityX.shellHandlerInstance!!
                    )
                )
                    showNotification(
                        requireContext(),
                        PrefsActivity::class.java,
                        System.currentTimeMillis().toInt(),
                        getString(R.string.copyOwnApkSuccess),
                        "",
                        false
                    )
                else
                    showNotification(
                        requireContext(),
                        PrefsActivity::class.java,
                        System.currentTimeMillis().toInt(),
                        getString(R.string.copyOwnApkFailed),
                        "",
                        false
                    )
            }
        } catch (e: IOException) {
            Timber.e("${getString(R.string.copyOwnApkFailed)}: $e")
        } finally {
            return true
        }
    }

    private fun onClickSchedulesExportImport(): Boolean {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.prefs_schedulesexportimport)
            .setPositiveButton(R.string.dialog_export) { _: DialogInterface, _: Int ->
                GlobalScope.launch(Dispatchers.IO) {
                    ExportsHandler(requireContext()).exportSchedules()
                }
            }
            .setNeutralButton(R.string.dialog_import) { _: DialogInterface, _: Int ->
                launchFragment(ExportsFragment())
            }
            .setNegativeButton(R.string.dialogNo, null)
            .show()
        return true
    }

    private fun onClickSaveAppsList(): Boolean {
        if (requirePrefsActivity().appInfoList.isNotEmpty()) {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.prefs_saveappslist)
                .setPositiveButton(R.string.radio_all) { _: DialogInterface, _: Int ->
                    writeAppsListFile(requirePrefsActivity().appInfoList
                        .filter { it.isSystem }
                        .map { "${it.packageLabel}: ${it.packageName}" }, false
                    )
                    requirePrefsActivity().refreshAppsList()
                }
                .setNeutralButton(R.string.filtered_list) { _: DialogInterface, _: Int ->
                    writeAppsListFile(
                        requirePrefsActivity().appInfoList.applyFilter(
                            requireContext().sortFilterModel,
                            requireContext()
                        ).map { "${it.packageLabel}: ${it.packageName}" },
                        true
                    )
                    requirePrefsActivity().refreshAppsList()
                }
                .setNegativeButton(R.string.dialogNo, null)
                .show()
        } else {
            Toast.makeText(
                requireActivity(),
                getString(R.string.wait_noappslist),
                Toast.LENGTH_LONG
            ).show()
        }
        return true
    }

    @Throws(IOException::class)
    fun writeAppsListFile(appsList: List<String>, filteredBoolean: Boolean) {
        val date = LocalDateTime.now()
        val filesText = appsList.joinToString("\n")
        val fileName = "${BACKUP_DATE_TIME_FORMATTER.format(date)}.appslist"
        val listFile =
            requireContext().getBackupDir().createFile("application/octet-stream", fileName)
        BufferedOutputStream(
            requireContext().contentResolver.openOutputStream(
                listFile?.uri
                    ?: Uri.EMPTY, "w"
            )
        )
            .use { it.write(filesText.toByteArray(StandardCharsets.UTF_8)) }
        showNotification(
            requireContext(), PrefsActivity::class.java, System.currentTimeMillis().toInt(),
            getString(
                if (filteredBoolean) R.string.write_apps_list_filtered
                else R.string.write_apps_list_all
            ), null, false
        )
        Timber.i("Wrote apps\' list file at $date")
    }

    private fun launchFragment(fragment: Fragment): Boolean {
        requireActivity().supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
        return true
    }

    private fun requirePrefsActivity(): PrefsActivity = requireActivity() as PrefsActivity
}