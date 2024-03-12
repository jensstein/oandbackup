package com.machiav3lli.backup.preferences

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.machiav3lli.backup.BACKUP_DATE_TIME_FORMATTER
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.handler.BackupRestoreHelper
import com.machiav3lli.backup.handler.showNotification
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.preferences.ui.PrefsGroup
import com.machiav3lli.backup.ui.compose.blockBorder
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.AndroidLogo
import com.machiav3lli.backup.ui.compose.icons.phosphor.Bug
import com.machiav3lli.backup.ui.compose.icons.phosphor.CalendarX
import com.machiav3lli.backup.ui.compose.icons.phosphor.ListNumbers
import com.machiav3lli.backup.ui.compose.icons.phosphor.TrashSimple
import com.machiav3lli.backup.ui.compose.item.LaunchPreference
import com.machiav3lli.backup.ui.compose.recycler.BusyBackground
import com.machiav3lli.backup.ui.compose.theme.ColorDeData
import com.machiav3lli.backup.ui.compose.theme.ColorExodus
import com.machiav3lli.backup.ui.compose.theme.ColorExtDATA
import com.machiav3lli.backup.ui.item.LinkPref
import com.machiav3lli.backup.ui.item.Pref
import com.machiav3lli.backup.ui.navigation.NavItem
import com.machiav3lli.backup.utils.applyFilter
import com.machiav3lli.backup.utils.getBackupRoot
import com.machiav3lli.backup.utils.show
import com.machiav3lli.backup.utils.sortFilterModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.BufferedOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ToolsPrefsPage(navController: NavHostController) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val prefs = Pref.prefGroups["tool"] ?: listOf()

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {

        BusyBackground(
            modifier = Modifier
                .blockBorder()
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    val size = prefs.size

                    PrefsGroup {
                        prefs.forEachIndexed { index, pref ->
                            LaunchPreference(
                                pref = pref,
                                index = index,
                                groupSize = size,
                            ) {
                                when (pref) {
                                    // TODO use only compose dialogs
                                    pref_batchDelete -> context.onClickUninstalledBackupsDelete(
                                        snackbarHostState,
                                        coroutineScope
                                    )

                                    pref_copySelfApk -> context.onClickCopySelf(
                                        snackbarHostState,
                                        coroutineScope
                                    )

                                    pref_schedulesExportImport -> navController.navigate(NavItem.Exports.destination)
                                    pref_saveAppsList -> context.onClickSaveAppsList(
                                        snackbarHostState,
                                        coroutineScope
                                    )

                                    pref_logViewer -> navController.navigate(NavItem.Logs.destination)
                                    pref_terminal -> navController.navigate(NavItem.Terminal.destination)
                                }
                            }
                            if (index < size - 1) Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

val pref_batchDelete = LinkPref(
    key = "tool.batchDelete",
    titleId = R.string.prefs_batchdelete,
    summaryId = R.string.prefs_batchdelete_summary,
    icon = Phosphor.TrashSimple,
    //iconTint = MaterialTheme.colorScheme.secondary
)

private fun Context.onClickUninstalledBackupsDelete(
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
): Boolean {
    val deleteList = ArrayList<Package>()
    val message = StringBuilder()
    val packageList = OABX.main?.viewModel?.packageList?.value ?: emptyList()
    if (packageList.isNotEmpty()) {
        packageList.forEach { appInfo ->
            if (!appInfo.isInstalled) {
                deleteList.add(appInfo)
                message.append(appInfo.packageLabel).append("\n")
            }
        }
    }
    if (packageList.isNotEmpty()) {
        if (deleteList.isNotEmpty()) {
            AlertDialog.Builder(this)
                .setTitle(R.string.prefs_batchdelete)
                .setMessage(message.toString().trim { it <= ' ' })
                .setPositiveButton(R.string.dialogYes) { _: DialogInterface?, _: Int ->
                    deleteBackups(deleteList)
                    //invalidateBackupLocation()    //TODO hg42 deletebackups *should* be sufficient (to be proved)
                }
                .setNegativeButton(R.string.dialogNo, null)
                .show()
        } else {
            snackbarHostState.show(
                coroutineScope,
                getString(R.string.batchDeleteNothingToDelete)
            )
        }
    } else {
        snackbarHostState.show(
            coroutineScope,
            getString(R.string.wait_noappslist),
        )
    }
    return true
}

private fun Context.deleteBackups(deleteList: List<Package>) {
    val notificationId = System.currentTimeMillis().toInt()
    deleteList.forEachIndexed { i, ai ->
        showNotification(
            this,
            MainActivityX::class.java,
            notificationId,
            "${getString(R.string.batchDeleteMessage)} ($i/${deleteList.size})",
            ai.packageLabel,
            false
        )
        Timber.i("deleting backups of ${ai.packageLabel}")
        ai.deleteAllBackups()
    }
    showNotification(
        this,
        MainActivityX::class.java,
        notificationId,
        getString(R.string.batchDeleteNotificationTitle),
        "${getString(R.string.batchDeleteBackupsDeleted)} ${deleteList.size}",
        false
    )
}

val pref_copySelfApk = LinkPref(
    key = "tool.copySelfApk",
    titleId = R.string.prefs_copyselfapk,
    summaryId = R.string.prefs_copyselfapk_summary,
    icon = Phosphor.AndroidLogo,
    //iconTint = MaterialTheme.colorScheme.primary
)

private fun Context.onClickCopySelf(
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
): Boolean {
    try {
        GlobalScope.launch(Dispatchers.IO) {
            if (BackupRestoreHelper.copySelfApk(
                    this@onClickCopySelf,
                    OABX.shellHandler!!
                )
            ) {
                showNotification(
                    this@onClickCopySelf,
                    MainActivityX::class.java,
                    System.currentTimeMillis().toInt(),
                    getString(R.string.copyOwnApkSuccess),
                    "",
                    false
                )
                snackbarHostState.show(
                    coroutineScope,
                    getString(R.string.copyOwnApkSuccess)
                )
            } else {
                showNotification(
                    this@onClickCopySelf,
                    MainActivityX::class.java,
                    System.currentTimeMillis().toInt(),
                    getString(R.string.copyOwnApkFailed),
                    "",
                    false
                )
                snackbarHostState.show(
                    coroutineScope,
                    getString(R.string.copyOwnApkFailed)
                )
            }
        }
    } catch (e: IOException) {
        Timber.e("${getString(R.string.copyOwnApkFailed)}: $e")
    } finally {
        return true
    }
}

val pref_schedulesExportImport = LinkPref(
    key = "tool.schedulesExportImport",
    titleId = R.string.prefs_schedulesexportimport,
    summaryId = R.string.prefs_schedulesexportimport_summary,
    icon = Phosphor.CalendarX,
    iconTint = ColorExtDATA
)

val pref_saveAppsList = LinkPref(
    key = "tool.saveAppsList",
    titleId = R.string.prefs_saveappslist,
    summaryId = R.string.prefs_saveappslist_summary,
    icon = Phosphor.ListNumbers,
    iconTint = ColorExodus
)


private fun Context.onClickSaveAppsList(
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
): Boolean {
    val packageList = OABX.main?.viewModel?.packageList?.value ?: emptyList()
    if (packageList.isNotEmpty()) {
        AlertDialog.Builder(this)
            .setTitle(R.string.prefs_saveappslist)
            .setPositiveButton(R.string.radio_all) { _: DialogInterface, _: Int ->
                writeAppsListFile(packageList
                    .filter { it.isSystem }
                    .map { "${it.packageLabel}: ${it.packageName} @ ${it.versionName}" },
                    false  //TODO hg42 name first because of ":", better for scripts
                )
            }
            .setNeutralButton(R.string.filtered_list) { _: DialogInterface, _: Int ->
                writeAppsListFile(
                    packageList.applyFilter(sortFilterModel, this)
                        .map { "${it.packageLabel}: ${it.packageName} @ ${it.versionName}" },
                    true
                )
            }
            .setNegativeButton(R.string.dialogNo, null)
            .show()
    } else {
        snackbarHostState.show(
            coroutineScope,
            getString(R.string.wait_noappslist),
        )
    }
    return true
}

@Throws(IOException::class)
fun Context.writeAppsListFile(appsList: List<String>, filteredBoolean: Boolean) {
    val date = LocalDateTime.now()
    val filesText = appsList.joinToString("\n")
    val fileName = "${BACKUP_DATE_TIME_FORMATTER.format(date)}.appslist"
    val listFile = getBackupRoot().createFile(fileName)
    BufferedOutputStream(listFile.outputStream())
        .use { it.write(filesText.toByteArray(StandardCharsets.UTF_8)) }
    showNotification(
        this, MainActivityX::class.java, System.currentTimeMillis().toInt(),
        getString(
            if (filteredBoolean) R.string.write_apps_list_filtered
            else R.string.write_apps_list_all
        ), null, false
    )
    Timber.i("Wrote apps\' list file at $date")
}


val pref_logViewer = LinkPref(
    key = "tool.logViewer",
    titleId = R.string.prefs_logviewer,
    icon = Phosphor.Bug,
    iconTint = ColorDeData
)

val pref_terminal = LinkPref(
    key = "tool.terminal",
    titleId = R.string.prefs_tools_terminal,
    icon = Phosphor.Bug,
    iconTint = ColorDeData
)
