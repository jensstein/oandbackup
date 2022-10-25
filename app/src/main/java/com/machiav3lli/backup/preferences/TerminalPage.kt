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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.flowlayout.FlowRow
import com.machiav3lli.backup.BuildConfig
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.ShellHandler.Companion.runAsRoot
import com.machiav3lli.backup.handler.ShellHandler.Companion.utilBox
import kotlinx.coroutines.launch


val padding = 5.dp

@Preview
@Composable
fun DefaultPreview() {
    TerminalPage()
}

fun info(): List<String> {
    return listOf(
        "",
        "---------- > info",
        "${ BuildConfig.APPLICATION_ID} ${ BuildConfig.VERSION_NAME}",
        "${utilBox.name} ${utilBox.version} ${
            if (utilBox.isTestedVersion) "tested" else "untested"
        }${
            if (utilBox.hasBugDotDotDir) " bugDotDotDir" else ""
        } -> score ${utilBox.score}",
    )
}

fun shell(command: String): List<String> {
    try {
        //val env = "EPKG=\"${OABX.lastErrorPackage}\" ECMD=\"${OABX.lastErrorCommand}\""
        //val result = runAsRoot("$env $command")
        val result = runAsRoot(command)
        return listOf(
            "",
            "---------- # $command -> ${result.code}"
        ) + result.err.map { "? $it" } + result.out
    } catch(e: Throwable) {
        return listOf(
            "",
            "---------- # $command -> ERROR",
            e::class.simpleName, e.message, e.cause?.message
        ).filterNotNull()
    }
}

@Composable
fun TerminalButton(name: String, important : Boolean = false, action: () -> Unit) {
    val color = if (important)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (important)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSurfaceVariant
    SmallFloatingActionButton(modifier = Modifier
        .width(IntrinsicSize.Min),
        containerColor = color,
        onClick = action
    ) {
        Text(modifier = Modifier
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
    val listState = rememberLazyListState()
    var command by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    fun add(lines: List<String>) {
        scope.launch {
            try { focusManager.clearFocus() } catch(_: Throwable) {}
            output.addAll(lines)
            listState.animateScrollToItem(index = output.size)
        }
    }

    fun run(command: String) {
        scope.launch {
            add(shell(command))
        }
    }

    Column(verticalArrangement = Arrangement.Top) {
        Column(modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
        ) {
            OutlinedTextField(modifier = Modifier
                .padding(padding)
                .fillMaxWidth(),
                value = command,
                singleLine = true,
                placeholder = { Text(text = "shell command", color = Color.Gray) },
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        run(command)
                        command = ""
                    }
                ),
                onValueChange = {
                    //if (it.endsWith("\n")) {
                    //    run(command)
                    //    command = ""
                    //} else
                        command = it
                }
            )
            FlowRow(modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
            ) {
                TerminalButton("->Log", important = true) {
                    LogsHandler.writeToLogFile(output.joinToString("\n"))
                    output.clear()
                }
                Spacer(Modifier.width(5.dp))
                TerminalButton("Clear", important = true) {
                    output.clear()
                }
                Spacer(Modifier.width(5.dp))
                TerminalButton("info") {
                    add(info())
                    run("su --help")
                    run("echo ${utilBox.name}")
                    run("${utilBox.name} --version")
                    run("${utilBox.name} --help")
                }
                TerminalButton("log/app") {
                    run("logcat -d -t ${2 * 60 /* sec */}.0 --pid=${Process.myPid()}")
                }
                TerminalButton("log/all") {
                    run("logcat -d -t ${2 * 60 /* sec */}.0")
                }
                TerminalButton("access") {
                    run("echo \"\$(ls /data/user/0/ | wc -l) packages (apk)\"")
                    run("echo \"$(ls /data/user/0/ | wc -l) packages (data)\"")
                    run("echo \"\$(ls -l /data/misc/ | wc -l) misc data\"")
                }
                TerminalButton("epkg") {
                    val pkg = OABX.lastErrorPackage
                    if (pkg != "") {
                        run("ls -l /data/user/0/$pkg")
                        run("ls -l /data/user/0/$pkg")
                        run("ls -l /sdcard/Android/*/$pkg")
                    } else {
                        add(listOf("--- no last error package"))
                    }
                }
                TerminalButton("ecmd") {
                    command = OABX.lastErrorCommand
                }
            }
        }
        Box(modifier = Modifier
            .padding(0.dp)
            .weight(1f, true)
            .background(color = Color.Black)
        ) {
            Box(modifier = Modifier
                .padding(10.dp)
                .fillMaxSize()
                .background(color = Color.Transparent)
            ) {
                SelectionContainer {
                    LazyColumn(modifier = Modifier
                        .fillMaxSize(),
                        state = listState
                    ) {
                        items(output) {
                            if (it.startsWith("===") or it.startsWith("---"))
                                Text(it,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = Color.Yellow
                                )
                            else
                                Text(it,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = Color.White
                                )
                        }
                    }
                }
            }
        }
    }
}