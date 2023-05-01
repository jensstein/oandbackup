package com.machiav3lli.backup.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.R
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.X
import com.machiav3lli.backup.ui.compose.item.ActionButton
import com.machiav3lli.backup.ui.compose.item.ElevatedActionButton

@Composable
fun StringInputDialogUI(
    titleText: String,
    initValue: String,
    openDialogCustom: MutableState<Boolean>,
    onSave: ((String) -> Unit) = {},
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val mainFocusRequester = remember { FocusRequester() }

    var savedValue by remember {
        mutableStateOf(TextFieldValue(initValue, TextRange(initValue.length)))
    }
    var isEdited by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    fun submit() {
        focusManager.clearFocus()
        onSave(savedValue.text)
        openDialogCustom.value = false
    }

    SideEffect { mainFocusRequester.requestFocus() }

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(8.dp),
        elevation = CardDefaults.elevatedCardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = titleText, style = MaterialTheme.typography.titleLarge)
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .fillMaxWidth()
                    .weight(1f, false)
            ) {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(mainFocusRequester),
                    value = savedValue,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    ),
                    shape = MaterialTheme.shapes.large,
                    singleLine = true,
                    onValueChange = {
                        isEdited = true
                        if (it.text.contains(RE_finishChars)) submit()
                        else savedValue = it         // only save when no control char
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Text,
                    ),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    trailingIcon = {
                        Row {
                            IconButton(onClick = { savedValue = TextFieldValue("") }) {
                                Icon(
                                    imageVector = Phosphor.X,
                                    contentDescription = stringResource(id = R.string.clear_text),
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    },
                )
            }

            Row(
                Modifier.fillMaxWidth()
            ) {
                ActionButton(text = stringResource(id = R.string.dialogCancel)) {
                    openDialogCustom.value = false
                }
                Spacer(Modifier.weight(1f))
                ElevatedActionButton(text = stringResource(id = R.string.dialogSave)) {
                    submit()
                }
            }
        }
    }
}
