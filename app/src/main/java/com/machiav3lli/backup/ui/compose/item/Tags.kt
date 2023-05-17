package com.machiav3lli.backup.ui.compose.item

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.R
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.PlusCircle
import com.machiav3lli.backup.ui.compose.icons.phosphor.X
import com.machiav3lli.backup.ui.compose.icons.phosphor.XCircle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagsBlock(
    modifier: Modifier = Modifier,
    tags: Set<String>?,
    onRemove: (String) -> Unit,
    onAdd: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FlowRow(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            tags?.forEach { tag -> TagItem(tag = tag, onClick = onRemove) }
            TagItem(
                tag = stringResource(id = R.string.add_tag),
                icon = Phosphor.PlusCircle,
                action = true,
                onClick = { onAdd() },
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagItem(
    modifier: Modifier = Modifier,
    tag: String,
    icon: ImageVector = Phosphor.XCircle,
    action: Boolean = false,
    onClick: (String) -> Unit,
) {
    InputChip(
        modifier = modifier,
        selected = false,
        colors = InputChipDefaults.inputChipColors(
            containerColor = if (action) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            labelColor = if (action) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            trailingIconColor = if (action) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.tertiary,
        ),
        shape = MaterialTheme.shapes.small,
        border = null,
        trailingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(id = R.string.dialogCancel),
            )
        },
        onClick = {
            onClick(tag)
        },
        label = {
            Text(
                text = tag,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    )
}


@Composable
fun AddTagView(
    modifier: Modifier = Modifier,
    onCancel: () -> Unit,
    onAdd: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val textFieldFocusRequester = remember { FocusRequester() }
    SideEffect { textFieldFocusRequester.requestFocus() }

    var tagName by remember { mutableStateOf("") }
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(tagName, TextRange(tagName.length)))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.background,
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
            shape = MaterialTheme.shapes.medium,
            singleLine = true,
            label = { Text(text = stringResource(id = R.string.add_tag)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        )
        IconButton(onClick = {
            onAdd(tagName)
            textFieldValue = TextFieldValue("", TextRange(0))
            tagName = ""
        }) {
            Icon(
                imageVector = Phosphor.PlusCircle,
                contentDescription = stringResource(id = R.string.add_tag)
            )
        }
    }
}