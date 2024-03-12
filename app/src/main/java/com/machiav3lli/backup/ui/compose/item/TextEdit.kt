package com.machiav3lli.backup.ui.compose.item

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.R
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.CheckCircle
import com.machiav3lli.backup.ui.compose.icons.phosphor.X

@Composable
fun MorphableTextField(
    text: String?,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    textAlignment: TextAlign? = null,
    onCancel: () -> Unit,
    onSave: (String) -> Unit,
) {
    val (expanded, onExpanded) = remember {
        mutableStateOf(expanded)
    }
    Box(modifier) {
        VerticalFadingVisibility(
            expanded = expanded,
            expandedView = {
                TextEditBlock(
                    text = text,
                    onCancel = onCancel,
                    onExpanded = onExpanded,
                    onSave = onSave
                )
            },
            collapsedView = {
                TextViewBlock(text = text, textAlignment = textAlignment, onExpanded = onExpanded)
            }
        )
    }
}

@Composable
fun TextViewBlock(
    text: String?,
    modifier: Modifier = Modifier,
    textAlignment: TextAlign? = null,
    onExpanded: (Boolean) -> Unit,
) {
    OutlinedCard(
        modifier = modifier
            .clickable { onExpanded(true) },
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color.Transparent
        ),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Text(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 17.dp),
            text = text ?: " ",
            textAlign = textAlignment,
        )
    }
}

@Composable
fun TextEditBlock(
    text: String?,
    modifier: Modifier = Modifier,
    onCancel: () -> Unit,
    onExpanded: (Boolean) -> Unit,
    onSave: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val textFieldFocusRequester = remember { FocusRequester() }
    SideEffect { textFieldFocusRequester.requestFocus() }

    var tagName by remember { mutableStateOf(text ?: "") }
    var textFieldValue by remember(text) {
        mutableStateOf(TextFieldValue(tagName, TextRange(tagName.length)))
    }

    Row(
        modifier = modifier
            .background(
                color = Color.Transparent,
                shape = MaterialTheme.shapes.large
            )
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                shape = MaterialTheme.shapes.large
            ),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {
            onCancel()
            onExpanded(false)
        }) {
            Icon(
                imageVector = Phosphor.X,
                contentDescription = stringResource(id = R.string.dialogCancel)
            )
        }
        TextField(
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
                tagName = it.text
            },
            modifier = Modifier
                .weight(1f)
                .focusRequester(textFieldFocusRequester),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            shape = MaterialTheme.shapes.large,
            singleLine = false,
            label = { Text(text = stringResource(id = R.string.edit_note)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        )
        IconButton(onClick = {
            onSave(tagName)
            onExpanded(false)
        }) {
            Icon(
                imageVector = Phosphor.CheckCircle,
                contentDescription = stringResource(id = R.string.dialogSave)
            )
        }
    }
}