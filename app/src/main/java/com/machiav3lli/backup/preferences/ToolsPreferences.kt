package com.machiav3lli.backup.preferences

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.PREFS_BATCH_DELETE
import com.machiav3lli.backup.PREFS_COPYSELF
import com.machiav3lli.backup.PREFS_LOGVIEWER
import com.machiav3lli.backup.PREFS_SAVEAPPSLIST
import com.machiav3lli.backup.PREFS_SCHEDULESEXPORTIMPORT
import com.machiav3lli.backup.R
import com.machiav3lli.backup.ui.compose.item.LaunchPreference
import com.machiav3lli.backup.ui.compose.theme.AppTheme
import com.machiav3lli.backup.ui.compose.theme.DeData
import com.machiav3lli.backup.ui.compose.theme.Exodus
import com.machiav3lli.backup.ui.compose.theme.ExtDATA
import com.machiav3lli.backup.ui.item.Pref

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

val CopySelfPref = Pref.LinkPref(
    key = PREFS_COPYSELF,
    titleId = R.string.prefs_copyselfapk,
    summaryId = R.string.prefs_copyselfapk_summary,
    iconId = R.drawable.ic_andy,
    //iconTint = MaterialTheme.colorScheme.primary
)

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