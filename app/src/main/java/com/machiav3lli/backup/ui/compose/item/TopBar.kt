package com.machiav3lli.backup.ui.compose.item

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.flowlayout.FlowRow
import com.machiav3lli.backup.ERROR_PREFIX
import com.machiav3lli.backup.ICON_SIZE_SMALL
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.dialogs.BaseDialog
import com.machiav3lli.backup.handler.findBackups
import com.machiav3lli.backup.preferences.DevPrefGroups
import com.machiav3lli.backup.preferences.LogsPage
import com.machiav3lli.backup.preferences.TerminalButton
import com.machiav3lli.backup.preferences.TerminalPage
import com.machiav3lli.backup.preferences.TerminalText
import com.machiav3lli.backup.preferences.pref_showInfoLogBar
import com.machiav3lli.backup.preferences.supportInfoLogShare
import com.machiav3lli.backup.preferences.ui.PrefsGroup
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.MagnifyingGlass
import com.machiav3lli.backup.ui.compose.icons.phosphor.X
import com.machiav3lli.backup.ui.compose.ifThen
import com.machiav3lli.backup.ui.compose.vertical
import com.machiav3lli.backup.ui.item.LaunchPref
import com.machiav3lli.backup.ui.item.Pref
import com.machiav3lli.backup.viewmodels.LogViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Float.max

@Preview
@Composable
fun DefaultPreview() {
    var count by remember { mutableStateOf(0) }
    val busy by remember { OABX.busy }

    val maxCount = 4

    OABX.clearInfoText()
    repeat(10) { OABX.addInfoText("line $it") }
    OABX.setProgress(count, maxCount)

    LaunchedEffect(OABX) {
        MainScope().launch {
            while (count < maxCount) {
                OABX.beginBusy()
                OABX.addInfoText("count is $count busy is $busy")
                delay(1000)
                count = (count + 1) % (maxCount + 2)
                OABX.endBusy()
                if (count > maxCount)
                    OABX.setProgress()
                OABX.addInfoText("count is $count busy is $busy")
                delay(1000)
            }
        }
    }

    TopBar(title = "Busy $busy", modifier = Modifier.background(color = Color.LightGray)) {
        Button(
            onClick = {
                OABX.beginBusy()
            }
        ) {
            Text("+")
        }
        Button(
            onClick = {
                OABX.endBusy()
            }
        ) {
            Text("-")
        }
        Button(
            onClick = {
                count = (count + 1) % (maxCount + 2)
                OABX.setProgress(count, maxCount)
                if (count > maxCount)
                    OABX.setProgress()
                OABX.addInfoText("test $count")
            }
        ) {
            Text("$count")
        }
    }
}

@Preview
@Composable
fun TestPreview() {
    Row(
        modifier = Modifier.wrapContentSize()
    ) {
        Text(
            modifier = Modifier
                .vertical()
                .rotate(-90f),
            fontWeight = FontWeight.Bold,
            text = "vertical text"
        )
        Text(text = "horizontal")
    }
}

@Composable
fun ProgressIndicator() {
    val progress by remember { OABX.progress }
    AnimatedVisibility(visible = progress.first) {
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            trackColor = MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp),
            color = MaterialTheme.colorScheme.primary,
            progress = max(0.02f, progress.second)
        )
    }
}

@Composable
fun GlobalIndicators() {
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        ProgressIndicator()
    }
}

@Composable
fun DevInfoTab() {

    var recompose by remember { mutableStateOf(0) }

    LaunchedEffect(recompose) {
        launch {
            delay(1000)
            recompose++
        }
    }

    TerminalText(OABX.infoLines)
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

val prev_renameDamagedToERROR = LaunchPref(
    key = "dev-tool.renameDamagedToERROR",
    summary = "rename damaged backups from xxx to ${ERROR_PREFIX}xxx (e.g. damaged properties file, properties without directory, directory wihtout properties)"
) {
    OABX.context.findBackups(renameDamaged = true)
}

val prev_undoDamagedToERROR = LaunchPref(
    key = "dev-tool.undoDamagedToERROR",
    summary = "rename all ${ERROR_PREFIX}xxx back to xxx"
) {
    OABX.context.findBackups(renameDamaged = false)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DevTools(
    expanded: MutableState<Boolean>,
) {
    var tab by rememberSaveable { mutableStateOf("devsett") }

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
                .fillMaxWidth()
                .padding(8.dp, 4.dp, 8.dp, 0.dp)
                .combinedClickable(
                    onClick = { expanded.value = false },
                    onLongClick = { tab = "devsett" }
                )
            ) {
                Text(text = tab, modifier = Modifier)
                Spacer(modifier = Modifier.weight(1f))
                TerminalButton("    close    ") {
                    expanded.value = false
                }      //TODO hg42 use weight, add modifier to TerminalButton
            }
            FlowRow(modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp, 0.dp, 8.dp, 4.dp)
                .combinedClickable(
                    onClick = { expanded.value = false },
                    onLongClick = { tab = "devsett" }
                )
            ) {
                TerminalButton("SUPPORT", important = true) { MainScope().launch { supportInfoLogShare() } }
                Spacer(modifier = Modifier.weight(1f))
                TerminalButton(" logs ", important = true) { tab = "logs" }
                Spacer(modifier = Modifier.weight(1f))
                TerminalButton(" log ", important = true) { tab = "log" }
                TerminalButton(" info ", important = true) { tab = "info" }
                Spacer(modifier = Modifier.weight(1f))
                TerminalButton(" devsett ", important = true) { tab = "devsett" }
                //Spacer(modifier = Modifier.weight(1f))
                //TerminalButton("  A  ", important = true) {
                //}
                TerminalButton("tools", important = true) {
                    tab = "tools"
                }
                Spacer(modifier = Modifier.weight(1f))
                TerminalButton(" term ", important = true) { tab = "term" }
            }
            when (tab) {
                "log"     -> DevLogTab()
                "info"    -> DevInfoTab()
                "devsett" -> DevSettingsTab()
                "tools"   -> DevToolsTab()
                "term"    -> TerminalPage()
                "logs"    -> LogsPage(LogViewModel(OABX.app))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    title: String,
    actions: @Composable (RowScope.() -> Unit),
) {
    var tempShowInfo by remember { mutableStateOf(false) }
    val longShowInfo = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val infoText =
        if (longShowInfo.value)
            OABX.getInfoText()
        else
            OABX.getInfoText(n = 5, fill = "")
    val scroll = rememberScrollState(0)
    val showInfo =
        !longShowInfo.value && (OABX.showInfo || tempShowInfo) && pref_showInfoLogBar.value

    LaunchedEffect(infoText) {
        tempShowInfo = true
        scope.launch {
            scroll.scrollTo(scroll.maxValue)
            delay(5000)
            tempShowInfo = false
        }
    }

    Box { // overlay page and indicators
        TopAppBar(
            modifier = modifier.wrapContentHeight(),
            title = {
                if (showInfo) {
                    Row(
                        verticalAlignment = if (showInfo) Alignment.Bottom else Alignment.CenterVertically,
                        modifier = Modifier
                            .wrapContentSize()
                            .combinedClickable(
                                onClick = {
                                    OABX.showInfo = !OABX.showInfo
                                    if (!OABX.showInfo)
                                        tempShowInfo = false
                                },
                                onLongClick = {
                                    longShowInfo.value = true
                                }
                            )
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                append(title)
                            },
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Start,
                            fontSize = 11.0.sp,
                            fontWeight = FontWeight(800),
                            modifier = Modifier
                                .absolutePadding(right = 4.dp, bottom = 4.dp)
                                .vertical()
                                .rotate(-90f)
                        )
                        Text(
                            text = infoText,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.0.sp,
                            lineHeight = 10.0.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = MaterialTheme.shapes.extraSmall
                                )
                                .padding(horizontal = 4.dp)
                                .combinedClickable(
                                    onClick = {
                                        OABX.showInfo = !OABX.showInfo
                                        if (!OABX.showInfo)
                                            tempShowInfo = false
                                    },
                                    onLongClick = {
                                        longShowInfo.value = true
                                    }
                                )
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxSize()
                            .ifThen(pref_showInfoLogBar.value) {
                                combinedClickable(
                                    onClick = {
                                        OABX.showInfo = !OABX.showInfo
                                        if (!OABX.showInfo)
                                            tempShowInfo = false
                                    },
                                    onLongClick = {
                                        longShowInfo.value = true
                                    }
                                )
                            }
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineMedium,
                        )
                    }
                    if (longShowInfo.value) {
                        BaseDialog(openDialogCustom = longShowInfo) {
                            DevTools(expanded = longShowInfo)
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.smallTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.3f),
                titleContentColor = MaterialTheme.colorScheme.onBackground,
                actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
            ),
            actions = actions
        )
        GlobalIndicators()
    }
}

@Composable
fun ExpandableSearchAction(
    query: String,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    onClose: () -> Unit,
    onQueryChanged: (String) -> Unit,
) {
    val (expanded, onExpanded) = rememberSaveable {
        mutableStateOf(expanded)
    }

    HorizontalExpandingVisibility(
        expanded = expanded,
        expandedView = {
            ExpandedSearchView(
                query = query,
                modifier = modifier,
                onClose = onClose,
                onExpanded = onExpanded,
                onQueryChanged = onQueryChanged
            )
        },
        collapsedView = {
            RoundButton(
                icon = Phosphor.MagnifyingGlass,
                description = stringResource(id = R.string.search),
                onClick = { onExpanded(true) }
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedSearchView(
    query: String,
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    onExpanded: (Boolean) -> Unit,
    onQueryChanged: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val textFieldFocusRequester = remember { FocusRequester() }
    SideEffect { textFieldFocusRequester.requestFocus() }

    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(query, TextRange(query.length)))
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
                onQueryChanged(it.text)
            },
            modifier = Modifier
                .weight(1f)
                .focusRequester(textFieldFocusRequester),
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = {
                    onExpanded(false)
                    textFieldValue = TextFieldValue("")
                    onQueryChanged("")
                    onClose()
                }) {
                    Icon(
                        imageVector = Phosphor.X,
                        contentDescription = stringResource(id = R.string.dialogCancel)
                    )
                }
            },
            label = { Text(text = stringResource(id = R.string.searchHint)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        )
    }
}