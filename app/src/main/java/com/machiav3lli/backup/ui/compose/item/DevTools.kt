package com.machiav3lli.backup.ui.compose.item

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
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.machiav3lli.backup.activities.RefreshButton
import com.machiav3lli.backup.handler.findBackups
import com.machiav3lli.backup.preferences.DevPrefGroups
import com.machiav3lli.backup.preferences.LogsPage
import com.machiav3lli.backup.preferences.TerminalButton
import com.machiav3lli.backup.preferences.TerminalPage
import com.machiav3lli.backup.preferences.TerminalText
import com.machiav3lli.backup.preferences.supportInfoLogShare
import com.machiav3lli.backup.preferences.ui.PrefsGroup
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.MagnifyingGlass
import com.machiav3lli.backup.ui.compose.icons.phosphor.X
import com.machiav3lli.backup.ui.item.LaunchPref
import com.machiav3lli.backup.ui.item.Pref
import com.machiav3lli.backup.viewmodels.LogViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    summary = "rename damaged backups from xxx to ${ERROR_PREFIX}xxx (e.g. damaged properties file, properties without directory, directory wihtout properties)"
) {
    MainScope().launch(Dispatchers.IO) {
        beginBusy("renameDamagedToERROR")
        OABX.context.findBackups(renameDamaged = true)
        endBusy("renameDamagedToERROR")
    }
}

val pref_undoDamagedToERROR = LaunchPref(
    key = "dev-tool.undoDamagedToERROR",
    summary = "rename all ${ERROR_PREFIX}xxx back to xxx"
) {
    MainScope().launch(Dispatchers.IO) {
        beginBusy("undoDamagedToERROR")
        OABX.context.findBackups(renameDamaged = false)
        endBusy("undoDamagedToERROR")
    }
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

val pref_shareSupportLog = LaunchPref(
    key = "dev-support.shareSupportLog",
    summary = "create and share a support log"
) {
    MainScope().launch {
        supportInfoLogShare()
    }
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
    var tab by rememberSaveable { mutableStateOf("devsett") }
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
                Box(modifier = Modifier.weight(1f).wrapContentHeight()) {
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

            val tabs = listOf<Pair<String, @Composable () -> Unit>>(
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
                "" to {},
                "" to {},
                "" to {},
                "" to {},
                "" to {},
                "SUPPORT" to { DevSupportTab() },
            )

            FlowRow(modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp, 0.dp, 8.dp, 4.dp)
                .combinedClickable(
                    onClick = { expanded.value = false },
                    onLongClick = { tab = "devsett" }
                )
            ) {
                tabs.forEach {
                    if (it.first.isEmpty())
                        Spacer(modifier = Modifier.width(8.dp))
                    else
                        TabButton(it.first)
                }
            }

            tabs.find { it.first == tab }?.let {
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
