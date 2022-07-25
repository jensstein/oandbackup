package com.machiav3lli.backup.preferences

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.PREFS_BATCH_DELETE
import com.machiav3lli.backup.PREFS_COPYSELF
import com.machiav3lli.backup.PREFS_LOGVIEWER
import com.machiav3lli.backup.PREFS_SAVEAPPSLIST
import com.machiav3lli.backup.PREFS_SCHEDULESEXPORTIMPORT
import com.machiav3lli.backup.R
import com.machiav3lli.backup.activities.PrefsActivityX
import com.machiav3lli.backup.handler.BackupRestoreHelper
import com.machiav3lli.backup.handler.showNotification
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.ui.compose.item.LaunchPreference
import com.machiav3lli.backup.ui.compose.theme.AppTheme
import com.machiav3lli.backup.ui.compose.theme.DeData
import com.machiav3lli.backup.ui.compose.theme.Exodus
import com.machiav3lli.backup.ui.compose.theme.ExtDATA
import com.machiav3lli.backup.ui.item.Pref
import com.machiav3lli.backup.utils.FileUtils.invalidateBackupLocation
import com.machiav3lli.backup.utils.show
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsPrefsPage() {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val prefs = listOf(
        CleanupBackupFolderPref,
        CopySelfPref,
        ExportImportSchedulesPref,
        SaveAppsListPref,
        LogViewerPref
    )

    AppTheme(
        darkTheme = isSystemInDarkTheme()
    ) {
        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = prefs) { pref ->
                    LaunchPreference(pref = pref) {
                        when (pref) {
                            CleanupBackupFolderPref -> context.onClickUninstalledBackupsDelete(
                                snackbarHostState,
                                coroutineScope
                            )
                            CopySelfPref -> context.onClickCopySelf(
                                snackbarHostState,
                                coroutineScope
                            )
                        }
                    }
                }
            }
        }
    }
}

val CleanupBackupFolderPref = Pref.LinkPref(
    key = PREFS_BATCH_DELETE,
    titleId = R.string.prefs_batchdelete,
    summaryId = R.string.prefs_batchdelete_summary,
    iconId = R.drawable.ic_delete,
    //iconTint = MaterialTheme.colorScheme.secondary
)

private fun Context.onClickUninstalledBackupsDelete(
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope
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
                    invalidateBackupLocation()
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
            PrefsActivityX::class.java,
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
        PrefsActivityX::class.java,
        notificationId,
        getString(R.string.batchDeleteNotificationTitle),
        "${getString(R.string.batchDeleteBackupsDeleted)} ${deleteList.size}",
        false
    )
}

val CopySelfPref = Pref.LinkPref(
    key = PREFS_COPYSELF,
    titleId = R.string.prefs_copyselfapk,
    summaryId = R.string.prefs_copyselfapk_summary,
    iconId = R.drawable.ic_andy,
    //iconTint = MaterialTheme.colorScheme.primary
)

private fun Context.onClickCopySelf(
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope
): Boolean {
    try {
        GlobalScope.launch(Dispatchers.IO) {
            if (BackupRestoreHelper.copySelfApk(
                    this@onClickCopySelf,
                    OABX.shellHandlerInstance!!
                )
            ) {
                showNotification(
                    this@onClickCopySelf,
                    PrefsActivityX::class.java,
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
                    PrefsActivityX::class.java,
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

val ExportImportSchedulesPref = Pref.LinkPref(
    key = PREFS_SCHEDULESEXPORTIMPORT,
    titleId = R.string.prefs_schedulesexportimport,
    summaryId = R.string.prefs_schedulesexportimport_summary,
    iconId = R.drawable.ic_scheduler,
    iconTint = ExtDATA
)

val SaveAppsListPref = Pref.LinkPref(
    key = PREFS_SAVEAPPSLIST,
    titleId = R.string.prefs_saveappslist,
    summaryId = R.string.prefs_saveappslist_summary,
    iconId = R.drawable.ic_list_ordered,
    iconTint = Exodus
)

val LogViewerPref = Pref.LinkPref(
    key = PREFS_LOGVIEWER,
    titleId = R.string.prefs_logviewer,
    iconId = R.drawable.ic_log,
    iconTint = DeData
)