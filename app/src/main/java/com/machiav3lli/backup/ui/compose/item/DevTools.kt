package com.machiav3lli.backup.ui.compose.item

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT
import android.content.Intent.FLAG_ACTIVITY_MULTIPLE_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.google.accompanist.flowlayout.FlowRow
import com.machiav3lli.backup.ERROR_PREFIX
import com.machiav3lli.backup.ICON_SIZE_SMALL
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.OABX.Companion.beginBusy
import com.machiav3lli.backup.OABX.Companion.endBusy
import com.machiav3lli.backup.handler.LogsHandler.Companion.logException
import com.machiav3lli.backup.handler.findBackups
import com.machiav3lli.backup.items.StorageFile
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
import com.machiav3lli.backup.preferences.supportInfoLogShare
import com.machiav3lli.backup.preferences.ui.PrefsGroup
import com.machiav3lli.backup.traceDebug
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.MagnifyingGlass
import com.machiav3lli.backup.ui.compose.icons.phosphor.X
import com.machiav3lli.backup.ui.item.LaunchPref
import com.machiav3lli.backup.ui.item.Pref
import com.machiav3lli.backup.utils.getBackupRoot
import com.machiav3lli.backup.viewmodels.LogViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


var devToolsTab = mutableStateOf("devsett")

val devToolsTabs = listOf<Pair<String, @Composable () -> Unit>>(
    "logs" to { DevLogsTab() },
    "" to {},
    "log" to { DevLogTab() },
    "infolog" to { DevInfoLogTab() },
    "" to {},
    "tools" to { DevToolsTab() },
    "term" to { TerminalPage() },
    "" to {},
    "devsett" to { DevSettingsTab() },
    "" to {},
    "SUPPORT" to { DevSupportTab() },
)

@Composable
fun DevInfoLogTab() {

    var recompose by remember { mutableStateOf(0) }

    LaunchedEffect(recompose) {
        launch {
            delay(1000)
            recompose++
        }
    }

    TerminalText(OABX.infoLogLines)
}

@Composable
fun DevLogsTab() {

    LogsPage(LogViewModel(OABX.app))
}

@Composable
fun DevLogTab() {

    var recompose by remember { mutableStateOf(0) }

    LaunchedEffect(recompose) {
        launch {
            delay(1000)
            recompose++
        }
    }

    TerminalText(OABX.lastLogMessages.toList())
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
            colors = TextFieldDefaults.textFieldColors(
                textColor = color,
                containerColor = Color.Transparent,
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
            if (search.isNullOrEmpty())
                DevPrefGroups()
            else
                PrefsGroup(
                    prefs =
                    Pref.preferences.values.flatten()
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

fun openFileManager(folder: StorageFile) {
    folder.uri?.let { uri ->
        MainScope().launch(Dispatchers.Default) {
            try {
                traceDebug { "uri = $uri" }
                when (1) {
                    0    -> {
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
                    0    -> {
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
                    0    -> {
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
                    0    -> {
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
                    1    -> {
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
            } catch(e: Throwable) {
                logException(e, backTrace = true)
            }
        }
    }
}

fun testOnStart() {
    MainScope().launch(Dispatchers.Main) {
        delay(3000)
        //openFileManager(OABX.context.getBackupRoot())
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

    val prefs = Pref.preferences["dev-tool"] ?: listOf()

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
    Pref.preferences["dev-trace"]?.forEach {
        Pref.setPrefFlag(it.key, it.defaultValue as Boolean)
    }
    pref_trace.value = true
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
    Pref.preferences["dev-trace"]?.forEach {
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

    val prefs = Pref.preferences["dev-support"] ?: listOf()

    Column(
        modifier = Modifier
            .verticalScroll(scroll)
    ) {
        PrefsGroup(prefs = prefs) { pref ->
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DevTools(
    expanded: MutableState<Boolean>,
) {
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
                    onLongClick = { tab = "devsett" }
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
                    tab = name
                }
            }

            FlowRow(modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp, 0.dp, 8.dp, 4.dp)
                .combinedClickable(
                    onClick = { expanded.value = false },
                    onLongClick = { tab = "devsett" }
                )
            ) {
                devToolsTabs.forEach {
                    if (it.first.isEmpty())
                        Spacer(modifier = Modifier.width(8.dp))
                    else
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
