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
package com.machiav3lli.backup.preferences

import android.os.Process
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.flowlayout.FlowRow
import com.machiav3lli.backup.BuildConfig
import com.machiav3lli.backup.ICON_SIZE_SMALL
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.OABX.Companion.beginBusy
import com.machiav3lli.backup.OABX.Companion.endBusy
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.LogsHandler.Companion.share
import com.machiav3lli.backup.handler.ShellHandler.Companion.runAsRoot
import com.machiav3lli.backup.handler.ShellHandler.Companion.utilBox
import com.machiav3lli.backup.handler.ShellHandler.FileInfo.Companion.utilBoxInfo
import com.machiav3lli.backup.items.Log
import com.machiav3lli.backup.ui.compose.SelectionContainerX
import com.machiav3lli.backup.ui.compose.ifThen
import com.machiav3lli.backup.ui.compose.item.RoundButton
import com.machiav3lli.backup.ui.item.Pref
import com.machiav3lli.backup.utils.SystemUtils.getApplicationIssuer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


val padding = 5.dp

fun info(): List<String> {
    return listOf(
        "",
        "--- > info",
        BuildConfig.APPLICATION_ID,
        BuildConfig.VERSION_NAME,
        OABX.context.getApplicationIssuer()?.let { "signed by $it" } ?: "",
        "",
        "--- shell utility box"
    ).filterNotNull() + utilBoxInfo()
}

fun shell(command: String): List<String> {
    try {
        //val env = "EPKG=\"${OABX.lastErrorPackage}\" ECMD=\"${OABX.lastErrorCommand}\""
        //val result = runAsRoot("$env $command")
        val result = runAsRoot(command)
        return listOf(
            "",
            "--- # $command -> ${result.code}"
        ) + result.err.map { "? $it" } + result.out
    } catch (e: Throwable) {
        return listOf(
            "",
            "--- # $command -> ERROR",
            e::class.simpleName,
            e.message,
            e.cause?.message
        ).filterNotNull()
    }
}

fun envInfo() =
    info() +
            shell("su --help") +
            shell("echo ${utilBox.name}") +
            shell("${utilBox.name} --version") +
            shell("${utilBox.name} --help")

fun logInt() =
    listOf("--- > last internal log messages") +
            OABX.lastLogMessages

val maxLogcat = "" //""-t 10000"

fun logApp() =
    shell("logcat -d ${maxLogcat} --pid=${Process.myPid()}")

fun logSys() =
    shell("logcat -d ${maxLogcat}")

fun dumpPrefs() =
    Pref.preferences.map {
        val (group, prefs) = it
        prefs.map {
            if (it.private)
                null
            else
                "${it.group}.${it.key} = ${it}"
        }.filterNotNull()
    }.flatten()

fun dumpAlarms() =
    shell("dumpsys alarm | sed -n '/Alarm.*machiav3lli[.]backup/,/PendingIntent/{p}'")

fun accessTest() =
    shell("echo \"\$(ls /data/user/0/ | wc -l) packages (apk)\"") +
            shell("echo \"$(ls /data/user/0/ | wc -l) packages (data)\"") +
            shell("echo \"\$(ls -l /data/misc/ | wc -l) misc data\"")

fun lastErrorPkg(): List<String> {
    val pkg = OABX.lastErrorPackage
    return if (pkg.isNotEmpty()) {
        listOf("--- last error package: $pkg") +
                shell("ls -l /data/user/0/$pkg") +
                shell("ls -l /data/user_de/0/$pkg") +
                shell("ls -l /sdcard/Android/*/$pkg")
    } else {
        listOf("--- ? no last error package")
    }
}

fun lastErrorCommand(): List<String> {
    val cmd = OABX.lastErrorCommand
    return if (cmd.isNotEmpty())
        listOf(
            "--- last error command",
            cmd
        )
    else
        emptyList()
}

fun supportInfo(): List<String> {
    beginBusy("supportInfo")
    val lines =
        listOf("=== support log", "") +
                envInfo() +
                dumpPrefs() +
                dumpAlarms() +
                accessTest() +
                lastErrorPkg() +
                lastErrorCommand() +
                logInt() +
                logApp()
    endBusy("supportInfo")
    return lines
}

fun textLogShare(lines: List<String>) {
    LogsHandler.writeToLogFile(lines.joinToString("\n"))?.let { file ->
        Log(file).let { log ->
            if (lines.isNotEmpty()) {
                share(log, asFile = true)
            }
        }
    }
}

fun supportInfoLogShare() {
    textLogShare(supportInfo())
}

@Composable
fun TerminalButton(name: String, important: Boolean = false, action: () -> Unit) {
    val color = if (important)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (important)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSurfaceVariant
    SmallFloatingActionButton(
        modifier = Modifier
            .wrapContentWidth(),
        containerColor = color,
        onClick = action
    ) {
        Text(
            modifier = Modifier
                .padding(3.dp),
            text = name,
            color = textColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalPage() {
    val output = remember { mutableStateListOf<String>() }
    var command by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    fun launch(todo: () -> Unit) {
        scope.launch {
            todo()
        }
    }

    fun append(lines: List<String>) {
        launch {
            runCatching {
                focusManager.clearFocus()
            }
            output.addAll(lines)
        }
    }

    fun run(command: String) {
        append(shell(command))
    }

    Column(verticalArrangement = Arrangement.Top) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
        ) {
            OutlinedTextField(modifier = Modifier
                .padding(padding)
                .fillMaxWidth(),
                value = command,
                singleLine = false,
                placeholder = { Text(text = "shell command", color = Color.Gray) },
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    //imeAction = ImeAction.Done
                ),
                //keyboardActions = KeyboardActions(
                //    onDone = {
                //        run(command)
                //        command = ""
                //    }
                //),
                onValueChange = {
                    if (it.endsWith("\n")) {
                        run(command)
                        command = ""
                    } else
                        command = it
                }
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding)
            ) {
                TerminalButton("SUPPORT", important = true) { launch { supportInfoLogShare() } }
                Spacer(Modifier.width(8.dp))
                TerminalButton("share", important = true) { launch { textLogShare(output) } }
                Spacer(Modifier.width(8.dp))
                TerminalButton("clear", important = true) { output.clear() }
                Spacer(Modifier.width(8.dp))
                TerminalButton("info") { append(envInfo()) }
                TerminalButton("log/int") { append(logInt()) }
                TerminalButton("log/app") { append(logApp()) }
                TerminalButton("log/all") { append(logSys()) }
                TerminalButton("prefs") { append(dumpPrefs()) }
                TerminalButton("alarms") { append(dumpAlarms()) }
                TerminalButton("access") { append(accessTest()) }
                TerminalButton("errInfo") { append(lastErrorPkg() + lastErrorCommand()) }
                TerminalButton("err->cmd") { command = OABX.lastErrorCommand }
                if (BuildConfig.DEBUG) {
                }
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(0.dp)
                .fillMaxHeight()
        ) {
            TerminalText(output, limitLines = 0, scrollOnAdd = true)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalText(text: List<String>, limitLines: Int = 0, scrollOnAdd: Boolean = true) {

    val hscroll = rememberScrollState()
    val listState = rememberLazyListState()
    val nLines = text.size
    var wrap by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val fontLineFactor = 1.4     //TODO hg42 factor 1.4 is empiric, how is it correctly?
    val searchFontFactor = 1.4
    val fontSize = 10.sp
    val lineHeightSp = fontSize * fontLineFactor
    val lineSpacing = 1.dp
    val lineHeight = with(LocalDensity.current) { lineHeightSp.toDp() }
    val totalLineHeight = lineHeight + lineSpacing

    var search by remember { mutableStateOf("") }

    if (scrollOnAdd)
        LaunchedEffect(nLines) {
            listState.animateScrollToItem(index = nLines)
        }

    Box(
        modifier = Modifier
            .ifThen(limitLines == 0) { fillMaxHeight() }
            .fillMaxWidth()
            .background(color = Color.Transparent),
        contentAlignment = Alignment.BottomEnd
    ) {
        Box(
            modifier = Modifier
                .ifThen(limitLines == 0) { fillMaxHeight() }
                .fillMaxWidth()
                .padding(0.dp)
                .ifThen(!wrap) { horizontalScroll(hscroll) }
                .background(color = Color(0.2f, 0.2f, 0.3f))
        ) {
            SelectionContainerX {
                LazyColumn(
                    modifier = Modifier
                        .padding(8.dp, 0.dp, 0.dp, 0.dp)
                        .ifThen(limitLines == 0) { fillMaxHeight() }
                        .ifThen(limitLines > 0) {
                            heightIn(
                                0.dp,
                                totalLineHeight * limitLines + lineSpacing
                            )
                        },
                    verticalArrangement = Arrangement.spacedBy(lineSpacing),
                    state = listState
                ) {
                    items(text) {
                        if (it.contains(search, ignoreCase = true)) {
                            val color =
                                when {
                                    it.contains("error", ignoreCase = true)   -> Color(1f, 0f, 0f)
                                    it.contains("warning", ignoreCase = true) -> Color(1f, 0.5f, 0f)
                                    it.contains("***")                        -> Color(0f, 1f, 1f)
                                    it.startsWith("===")                      -> Color(1f, 1f, 0f)
                                    it.startsWith("---")                      -> Color(
                                        0.8f,
                                        0.8f,
                                        0f
                                    )
                                    else                                      -> Color.White
                                }
                            Text(
                                if (it == "") " " else it,     //TODO hg42 workaround
                                fontFamily = FontFamily.Monospace,
                                fontSize = fontSize,
                                lineHeight = lineHeightSp,
                                softWrap = wrap,
                                color = color,
                                modifier = Modifier.padding(0.dp)
                            )
                        }
                    }
                }
            }
        }

        val overlayColor = Color(1f, 0.5f, 1f, 1f)

        @Composable
        fun SmallButton(icon: ImageVector, onClick: () -> Unit) {
            RoundButton(
                icon = icon,
                onClick = onClick,
                tint = overlayColor
            )
        }

        Row(
            modifier = Modifier
                //.fillMaxWidth()
                .background(color = Color.Transparent),
            horizontalArrangement = Arrangement.End
        ) {
            //val focusManager = LocalFocusManager.current

            TextField(modifier = Modifier
                .padding(0.dp), //.weight(1f),
                value = search,
                singleLine = true,
                //placeholder = { Text(text = "search", color = Color.Gray) },
                colors = TextFieldDefaults.textFieldColors(
                    textColor = overlayColor,
                    containerColor = Color.Transparent,
                    unfocusedLeadingIconColor = overlayColor,
                    focusedLeadingIconColor = if (search.length > 0) Color.Transparent else overlayColor
                ),
                textStyle = TextStyle(fontSize = fontSize * searchFontFactor,
                    lineHeight = lineHeightSp * searchFontFactor),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "search",
                        modifier = Modifier.size(ICON_SIZE_SMALL)
                        //tint = tint,
                        //contentDescription = description
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
            SmallButton(icon = if (wrap) Icons.Rounded.MoreVert else Icons.Rounded.List) {
                wrap = !wrap
            }
            SmallButton(icon = Icons.Rounded.KeyboardArrowUp) {
                scope.launch { listState.animateScrollToItem(0) }
            }
            SmallButton(icon = Icons.Rounded.KeyboardArrowDown) {
                scope.launch { listState.animateScrollToItem(text.size) }
            }
        }
    }
}


@Preview
@Composable
fun Preview_Terminal() {

    Box(modifier = Modifier.height(600.dp)) {
        TerminalPage()
    }
}

@Preview
@Composable
fun Preview_TerminalText() {

    val text = remember {
        mutableStateListOf(
            "aaaa aaaa aaaa aaaa aaaa aaaa aaaa aaaa aaaa aaaa aaaa aaaa aaaa aaaa aaaa aaaa aaaa aaaa aaaa aaaa.",
            "bbbb bbbb bbbb bbbb bbbb bbbb bbbb bbbb bbbb bbbb bbbb bbbb bbbb bbbb bbbb bbbb bbbb bbbb bbbb bbbb.",
            "cccc cccc cccc cccc cccc cccc cccc cccc cccc cccc cccc cccc cccc cccc cccc cccc cccc cccc cccc cccc.",
            "dddd dddd dddd dddd dddd dddd dddd dddd dddd dddd dddd dddd dddd dddd dddd dddd dddd dddd dddd dddd.",
            "eeee eeee eeee eeee eeee eeee eeee eeee eeee eeee eeee eeee eeee eeee eeee eeee eeee eeee eeee eeee.",
            "=== yyy",
            "--- xxx",
            "*** zzz",
            "this is an error",
            "this is an ERROR",
            "this is a warning",
            "this is a WARNING",
            *((13..30).map { "line $it" }.toTypedArray())
        )
    }

    LaunchedEffect(true) {
        launch {
            delay(3000)
            text.add("some added text")
        }
    }

    Box(
        modifier = Modifier
            //.height(500.dp)
            .padding(0.dp)
            .background(color = Color(0.2f, 0.2f, 0.3f))
    ) {
        TerminalText(text, limitLines = 20, scrollOnAdd = false)
    }
}

