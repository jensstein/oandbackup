package com.machiav3lli.backup.ui.compose.item

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT
import android.content.Intent.FLAG_ACTIVITY_MULTIPLE_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.ERROR_PREFIX
import com.machiav3lli.backup.ICON_SIZE_SMALL
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.OABX.Companion.beginBusy
import com.machiav3lli.backup.OABX.Companion.endBusy
import com.machiav3lli.backup.OABX.Companion.isDebug
import com.machiav3lli.backup.PREFS_BACKUP_FILE
import com.machiav3lli.backup.handler.LogsHandler.Companion.logException
import com.machiav3lli.backup.handler.findBackups
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.items.UndeterminedStorageFile
import com.machiav3lli.backup.pref_autoLogAfterSchedule
import com.machiav3lli.backup.pref_autoLogExceptions
import com.machiav3lli.backup.pref_autoLogSuspicious
import com.machiav3lli.backup.pref_catchUncaughtException
import com.machiav3lli.backup.pref_logToSystemLogcat
import com.machiav3lli.backup.pref_maxLogLines
import com.machiav3lli.backup.pref_trace
import com.machiav3lli.backup.preferences.DevPrefGroups
import com.machiav3lli.backup.preferences.LogsPage
import com.machiav3lli.backup.preferences.TerminalButton
import com.machiav3lli.backup.preferences.TerminalPage
import com.machiav3lli.backup.preferences.TerminalText
import com.machiav3lli.backup.preferences.logRel
import com.machiav3lli.backup.preferences.supportInfoLogShare
import com.machiav3lli.backup.preferences.ui.PrefsGroup
import com.machiav3lli.backup.traceDebug
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.MagnifyingGlass
import com.machiav3lli.backup.ui.compose.icons.phosphor.X
import com.machiav3lli.backup.ui.item.LaunchPref
import com.machiav3lli.backup.ui.item.Pref
import com.machiav3lli.backup.ui.item.Pref.Companion.preferencesFromSerialized
import com.machiav3lli.backup.ui.item.Pref.Companion.preferencesToSerialized
import com.machiav3lli.backup.utils.TraceUtils.trace
import com.machiav3lli.backup.utils.getBackupRoot
import com.machiav3lli.backup.utils.recreateActivities
import com.machiav3lli.backup.viewmodels.LogViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

var devToolsTab = mutableStateOf("")

val devToolsTabs = listOf<Pair<String, @Composable () -> Any>>(
    "logs" to { DevLogsTab() },
    "log" to { DevLogTab() },
    "infolog" to { DevInfoLogTab() },
    "tools" to { DevToolsTab() },
    "term" to { TerminalPage() },
    "devsett" to { DevSettingsTab() },
    "SUPPORT" to { DevSupportTab() },
) + if (isDebug) listOf<Pair<String, @Composable () -> Any>>(
    //"refreshScreen" to { OABX.context.recreateActivities(); devToolsTab.value = "" },
    //"invBackupLoc" to { FileUtils.invalidateBackupLocation() ; devToolsTab.value = "" },
    //"updateAppTables" to { OABX.context.updateAppTables() ; devToolsTab.value = "" },
    //"findBackups" to { OABX.context.findBackups() ; devToolsTab.value = "" },
) else emptyList()


@Composable
fun DevInfoLogTab() {

    TerminalText(OABX.infoLogLines)
}

@Composable
fun DevLogsTab() {

    LogsPage(LogViewModel(OABX.NB))
}

@Composable
fun DevLogTab() {

    val lines = remember { mutableStateOf<List<String>>(listOf()) }

    LaunchedEffect(true) {
        launch {
            lines.value = OABX.lastLogMessages.toList()
            if (lines.value.isEmpty())
                lines.value = logRel()
        }
    }

    TerminalText(lines.value)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevSettingsTab() {

    val scroll = rememberScrollState(0)
    var search by remember { mutableStateOf("") }

    val color = MaterialTheme.colorScheme.onSurface

    Column {
        TextField(modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp),
            value = search,
            singleLine = true,
            //placeholder = { Text(text = "search", color = Color.Gray) },
            colors = TextFieldDefaults.colors(
                focusedTextColor = color,
                unfocusedTextColor = color,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                unfocusedTrailingIconColor = color,
                focusedTrailingIconColor = color, //if (search.length > 0) Color.Transparent else overlayColor
            ),
            textStyle = MaterialTheme.typography.bodyMedium,
            trailingIcon = {
                if (search.isEmpty())
                    Icon(
                        imageVector = Phosphor.MagnifyingGlass,
                        contentDescription = "search",
                        modifier = Modifier.size(ICON_SIZE_SMALL)
                        //tint = tint,
                        //contentDescription = description
                    )
                else
                    Icon(
                        imageVector = Phosphor.X,
                        contentDescription = "search",
                        modifier = Modifier
                            .size(ICON_SIZE_SMALL)
                            .clickable { search = "" }
                        //tint = tint,
                        //contentDescription = description,
                    )
            },
            keyboardOptions = KeyboardOptions(
                autoCorrect = false,
                //imeAction = ImeAction.Done
            ),
            //keyboardActions = KeyboardActions(
            //    onDone = {
            //        todo
            //        search = ""
            //    }
            //),
            onValueChange = {
                search = it
            }
        )

        Column(
            modifier = Modifier
                .verticalScroll(scroll)
                .weight(1f)
        ) {
            if (search.isEmpty())
                DevPrefGroups()
            else
                PrefsGroup(
                    prefs =
                    Pref.prefGroups.values.flatten()
                        .filter {
                            it.key.contains(search, ignoreCase = true)
                                    && it.group !in listOf("persist", "kill")
                        }
                )
        }
    }
}

val pref_renameDamagedToERROR = LaunchPref(
    key = "dev-tool.renameDamagedToERROR",
    summary = "rename damaged backups from xxx to ${ERROR_PREFIX}xxx (e.g. damaged properties file, properties without directory, directory without properties).\nHint: search recursively for ${ERROR_PREFIX} in a capable file manager"
) {
    MainScope().launch(Dispatchers.IO) {
        beginBusy("renameDamagedToERROR")
        OABX.context.findBackups(damagedOp = "ren")
        devToolsTab.value = "infolog"
        endBusy("renameDamagedToERROR")
    }
}

val pref_undoDamagedToERROR = LaunchPref(
    key = "dev-tool.undoDamagedToERROR",
    summary = "rename all ${ERROR_PREFIX}xxx back to xxx"
) {
    MainScope().launch(Dispatchers.IO) {
        beginBusy("undoDamagedToERROR")
        OABX.context.findBackups(damagedOp = "undo")
        devToolsTab.value = "infolog"
        endBusy("undoDamagedToERROR")
    }
}

val pref_deleteERROR = LaunchPref(
    key = "dev-tool.deleteERROR",
    summary = "delete all ${ERROR_PREFIX}xxx"
) {
    MainScope().launch(Dispatchers.IO) {
        beginBusy("deleteERROR")
        OABX.context.findBackups(damagedOp = "del")
        devToolsTab.value = "infolog"
        endBusy("deleteERROR")
    }
}

val pref_savePreferences = LaunchPref(
    key = "dev-tool.savePreferences",
    summary = "save preferences to $PREFS_BACKUP_FILE"
) {
    MainScope().launch(Dispatchers.IO) {
        val serialized = preferencesToSerialized()
        if (serialized.isNotEmpty()) {
            runCatching {
                val backupRoot = OABX.context.getBackupRoot()
                UndeterminedStorageFile(backupRoot, PREFS_BACKUP_FILE).let {
                    it.writeText(serialized)?.let {
                        OABX.addInfoLogText("saved ${it.name}")
                    }
                }
            }
        }
    }
}

val pref_loadPreferences = LaunchPref(
    key = "dev-tool.loadPreferences",
    summary = "load preferences from $PREFS_BACKUP_FILE"
) {
    MainScope().launch(Dispatchers.IO) {
        runCatching {
            val backupRoot = OABX.context.getBackupRoot()
            backupRoot.findFile(PREFS_BACKUP_FILE)?.let {
                val serialized = it.readText()
                preferencesFromSerialized(serialized)
                OABX.addInfoLogText("loaded ${it.name}")
                OABX.context.recreateActivities()
            }
        }
    }
}

fun testOnStart() {
    if (isDebug) {
        if (1 == 0)
            MainScope().launch(Dispatchers.Main) {
                trace { "############################################################ testOnStart: waiting..." }
                delay(3000)
                trace { "############################################################ testOnStart: running..." }

                //openFileManager(OABX.context.getBackupRoot())

                //pref_savePreferences.onClick()
                trace { "############################################################ testOnStart: end." }
            }
    }
}

fun openFileManager(folder: StorageFile) {
    folder.uri?.let { uri ->
        MainScope().launch(Dispatchers.Default) {
            try {
                traceDebug { "uri = $uri" }
                when (1) {
                    0 -> {
                        val intent =
                            Intent().apply {
                                action = Intent.ACTION_VIEW
                                flags = FLAG_ACTIVITY_NEW_TASK or
                                        FLAG_ACTIVITY_MULTIPLE_TASK or
                                        FLAG_ACTIVITY_LAUNCH_ADJACENT
                                setData(uri)
                                //setDataAndType(uri, "*/*")
                                //putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                                addCategory(Intent.CATEGORY_BROWSABLE)
                            }
                        OABX.activity?.startActivity(intent)
                    }

                    0 -> {
                        val intent =
                            Intent().apply {
                                action = Intent.ACTION_GET_CONTENT
                                flags = FLAG_ACTIVITY_NEW_TASK or
                                        FLAG_ACTIVITY_MULTIPLE_TASK or
                                        FLAG_ACTIVITY_LAUNCH_ADJACENT
                                setData(uri)
                                //setDataAndType(uri, "*/*")
                                //putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                                addCategory(Intent.CATEGORY_BROWSABLE)
                            }
                        val chooser = Intent.createChooser(intent, "Browse")
                        OABX.activity?.startActivity(chooser)
                    }

                    0 -> {
                        val intent =
                            Intent().apply {
                                action = Intent.ACTION_GET_CONTENT
                                flags = FLAG_ACTIVITY_NEW_TASK or
                                        FLAG_ACTIVITY_MULTIPLE_TASK or
                                        FLAG_ACTIVITY_LAUNCH_ADJACENT
                                setDataAndType(uri, "*/*")
                                //putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                                addCategory(Intent.CATEGORY_OPENABLE)
                            }
                        val chooser = Intent.createChooser(intent, "Browse")
                        OABX.activity?.startActivity(chooser)
                    }

                    0 -> {
                        val intent =
                            Intent().apply {
                                action = Intent.ACTION_OPEN_DOCUMENT_TREE
                                //flags =
                                //    FLAG_ACTIVITY_NEW_TASK or
                                //            FLAG_ACTIVITY_MULTIPLE_TASK or
                                //            FLAG_ACTIVITY_LAUNCH_ADJACENT
                                setData(uri)
                                //setDataAndType(uri, "*/*")
                                //setDataAndType(uri, "resource/folder")
                                //setDataAndType(uri, "vnd.android.document/directory")
                                //setDataAndType(uri, EXTRA_MIME_TYPES)
                                //addCategory(CATEGORY_APP_FILES)
                            }
                        OABX.activity?.startActivity(intent)
                    }

                    1 -> {
                        val intent =
                            Intent().apply {
                                action = Intent.ACTION_VIEW
                                flags =
                                    FLAG_ACTIVITY_NEW_TASK or
                                            FLAG_ACTIVITY_MULTIPLE_TASK or
                                            FLAG_ACTIVITY_LAUNCH_ADJACENT
                                //setData(uri)
                                //setDataAndType(uri, "*/*")
                                //setDataAndType(uri, "resource/folder")
                                setDataAndType(uri, "vnd.android.document/directory")
                                //putExtra(EXTRA_MIME_TYPES, arrayOf(
                                //    "vnd.android.document/directory",
                                //    "resource/folder",
                                //))
                                //addCategory(CATEGORY_APP_FILES)
                                //addCategory(Intent.CATEGORY_BROWSABLE)
                            }
                        OABX.context.startActivity(intent)
                    }

                    else -> {}
                }
                traceDebug { "ok" }
            } catch (e: Throwable) {
                logException(e, backTrace = true)
            }
        }
    }
}

val pref_openBackupDir = LaunchPref(
    key = "dev-tool.openBackupDir",
    summary = "open backup directory in associated app"
) {
    OABX.context.getBackupRoot().let { openFileManager(it) }
}

@Composable
fun DevToolsTab() {

    val scroll = rememberScrollState(0)

    val prefs = Pref.prefGroups["dev-tool"] ?: listOf()

    Column(
        modifier = Modifier
            .verticalScroll(scroll)
    ) {
        PrefsGroup(prefs = prefs) { pref ->
        }
    }
}

val pref_prepareSupport = LaunchPref(
    key = "dev-support.prepareSupport",
    summary = "prepare settings for usual support purposes"
) {
    Pref.prefGroups["dev-trace"]?.forEach {
        Pref.setPrefFlag(it.key, it.defaultValue as Boolean)
    }
    pref_trace.value = true
    traceDebug.pref.value = true
    pref_maxLogLines.value = 20_000
    pref_logToSystemLogcat.value = true
    pref_catchUncaughtException.value = true
    pref_autoLogExceptions.value = true
    pref_autoLogSuspicious.value = true
    pref_autoLogAfterSchedule.value = true
}

val pref_shareSupportLog = LaunchPref(
    key = "dev-support.shareSupportLog",
    summary = "create and share a support log"
) {
    MainScope().launch {
        supportInfoLogShare()
    }
}

val pref_afterSupport = LaunchPref(
    key = "dev-support.afterSupport",
    summary = "set settings to normal"
) {
    Pref.prefGroups["dev-trace"]?.forEach {
        Pref.setPrefFlag(it.key, it.defaultValue as Boolean)
    }
    pref_trace.value = true
    pref_maxLogLines.apply { value = defaultValue as Int }
    pref_logToSystemLogcat.apply { value = defaultValue as Boolean }
    pref_catchUncaughtException.apply { value = defaultValue as Boolean }
    pref_autoLogExceptions.apply { value = defaultValue as Boolean }
    pref_autoLogSuspicious.apply { value = defaultValue as Boolean }
    pref_autoLogAfterSchedule.apply { value = defaultValue as Boolean }
}

@Composable
fun DevSupportTab() {
    val scroll = rememberScrollState(0)

    val prefs = Pref.prefGroups["dev-support"] ?: listOf()

    Column(
        modifier = Modifier
            .verticalScroll(scroll)
    ) {
        PrefsGroup(prefs = prefs) { pref ->
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun DevTools(
    expanded: MutableState<Boolean>,
) {
    LaunchedEffect(true) {
        if (devToolsTab.value.isEmpty())
            devToolsTab.value = "devsett"
    }

    var tab by devToolsTab
    val tempShowInfo = remember { mutableStateOf(false) }
    val showInfo = OABX.showInfoLog || tempShowInfo.value

    Surface(
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = AbsoluteRoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(modifier = Modifier
                //.wrapContentSize()
                .padding(8.dp, 4.dp, 8.dp, 0.dp)
                .combinedClickable(
                    onClick = { expanded.value = false },
                    onLongClick = { tab = "" }
                )
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentHeight()
                ) {
                    TitleOrInfoLog(
                        title = "DevTools",
                        showInfo = showInfo,
                        tempShowInfo = tempShowInfo,
                        modifier = Modifier
                            .wrapContentHeight()
                            .combinedClickable(
                                onClick = {
                                    OABX.showInfoLog = OABX.showInfoLog.not()
                                    if (OABX.showInfoLog.not())
                                        tempShowInfo.value = false
                                },
                                onLongClick = {
                                }
                            )
                    )
                }
                //Text(text = tab, modifier = Modifier)
                RefreshButton(hideIfNotBusy = true)
                TerminalButton(
                    "          close          "
                ) {
                    expanded.value = false
                }
            }

            @Composable
            fun TabButton(name: String) {
                TerminalButton(
                    name = name,
                    important = (tab == name),
                ) {
                    if (tab != name)
                        tab = name
                    else {
                        tab = ""
                        MainScope().launch {
                            yield()
                            tab = name
                        }
                    }
                }
            }

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 8.dp,
                        top = 0.dp,
                        end = 8.dp,
                        bottom = 4.dp
                    )
                    .combinedClickable(
                        onClick = { expanded.value = false },
                        onLongClick = { tab = "" }
                    ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                devToolsTabs.forEach {
                    TabButton(it.first)
                }
            }

            devToolsTabs.find { it.first == tab }?.let {
                it.second()
            }
        }
    }
}

@Preview
@Composable
fun DevToolsPreview() {
    val expanded = remember { mutableStateOf(true) }
    var count by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .width(500.dp)
            .height(1000.dp)
    ) {
        Row {
            TerminalButton(if (expanded.value) "close" else "open") {
                expanded.value = expanded.value.not()
            }
            TerminalButton("count") {
                count++
                OABX.addInfoLogText("line $count")
            }
        }
        if (expanded.value)
            DevTools(expanded)
    }
}
