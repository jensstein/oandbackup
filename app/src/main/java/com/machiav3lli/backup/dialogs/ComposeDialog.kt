package com.machiav3lli.backup.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.machiav3lli.backup.R
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.Eye
import com.machiav3lli.backup.ui.compose.icons.phosphor.EyeSlash
import com.machiav3lli.backup.ui.compose.item.ActionButton
import com.machiav3lli.backup.ui.compose.item.ElevatedActionButton
import com.machiav3lli.backup.ui.compose.item.SelectableRow
import com.machiav3lli.backup.ui.item.EnumPref
import com.machiav3lli.backup.ui.item.ListPref
import com.machiav3lli.backup.ui.item.StringPref
import kotlinx.coroutines.delay

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BaseDialog(
    openDialogCustom: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    dialogUI: @Composable (() -> Unit),
) {
    Dialog(
        onDismissRequest = { openDialogCustom.value = false },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        dialogUI()
    }
}

@Composable
fun ActionsDialogUI(
    titleText: String,
    messageText: String,
    openDialogCustom: MutableState<Boolean>,
    primaryText: String,
    primaryIcon: ImageVector? = null,
    primaryAction: (() -> Unit) = {},
    secondaryText: String = "",
    secondaryIcon: ImageVector? = null,
    secondaryAction: (() -> Unit)? = null,
) {
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
            Text(text = messageText, style = MaterialTheme.typography.bodyMedium)

            Row(
                Modifier.fillMaxWidth()
            ) {
                ActionButton(text = stringResource(id = R.string.dialogCancel)) {
                    openDialogCustom.value = false
                }
                Spacer(Modifier.weight(1f))
                if (secondaryAction != null && secondaryText.isNotEmpty()) {
                    ElevatedActionButton(
                        text = secondaryText,
                        icon = secondaryIcon,
                        positive = false
                    ) {
                        secondaryAction()
                        openDialogCustom.value = false
                    }
                    Spacer(Modifier.requiredWidth(8.dp))
                }
                ElevatedActionButton(
                    text = primaryText,
                    icon = primaryIcon,
                ) {
                    primaryAction()
                    openDialogCustom.value = false
                }
            }
        }
    }
}

@Composable
fun EnumDialogUI(
    pref: EnumPref,
    openDialogCustom: MutableState<Boolean>,
    onChanged: (() -> Unit) = {},
) {
    val context = LocalContext.current
    var selected by remember { mutableStateOf(pref.value) }
    val entryPairs = pref.entries.toList()

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
            Text(text = stringResource(pref.titleId), style = MaterialTheme.typography.titleLarge)
            LazyColumn(
                modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
            ) {
                items(items = entryPairs) {
                    val isSelected = rememberSaveable(selected) {
                        mutableStateOf(selected == it.first)
                    }
                    SelectableRow(
                        modifier = Modifier.clip(MaterialTheme.shapes.medium),
                        title = stringResource(id = it.second),
                        selectedState = isSelected
                    ) {
                        selected = it.first
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth()
            ) {
                ActionButton(text = stringResource(id = R.string.dialogCancel)) {
                    openDialogCustom.value = false
                }
                Spacer(Modifier.weight(1f))
                ElevatedActionButton(text = stringResource(id = R.string.dialogSave)) {
                    if (pref.value != selected) {
                        pref.value = selected
                        onChanged()
                    }
                    openDialogCustom.value = false
                }
            }
        }
    }
}

@Composable
fun ListDialogUI(
    pref: ListPref,
    openDialogCustom: MutableState<Boolean>,
    onChanged: (() -> Unit) = {},
) {
    val context = LocalContext.current
    var selected by remember { mutableStateOf(pref.value) }
    val entryPairs = pref.entries.toList()

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
            Text(text = stringResource(pref.titleId), style = MaterialTheme.typography.titleLarge)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 16.dp, bottom = 16.dp)
            ) {
                items(items = entryPairs) {
                    val isSelected = rememberSaveable(selected) {
                        mutableStateOf(selected == it.first)
                    }
                    SelectableRow(
                        modifier = Modifier.clip(MaterialTheme.shapes.medium),
                        title = it.second,
                        selectedState = isSelected
                    ) {
                        selected = it.first
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth()
            ) {
                ActionButton(text = stringResource(id = R.string.dialogCancel)) {
                    openDialogCustom.value = false
                }
                Spacer(Modifier.weight(1f))
                ElevatedActionButton(text = stringResource(id = R.string.dialogSave)) {
                    if (pref.value != selected) {
                        pref.value = selected
                        onChanged()
                    }
                    openDialogCustom.value = false
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StringDialogUI(
    pref: StringPref,
    isPrivate: Boolean = false,
    confirm: Boolean = false,
    openDialogCustom: MutableState<Boolean>,
    onChanged: (() -> Unit) = {},
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val textFieldFocusRequester = remember { FocusRequester() }

    LaunchedEffect(textFieldFocusRequester) {
        delay(100)
        textFieldFocusRequester.requestFocus()
    }
    var savedValue by remember { mutableStateOf(pref.value) }
    var savedValueConfirm by remember { mutableStateOf("") }
    var isEdited by remember { mutableStateOf(false) }
    var notMatching by remember { mutableStateOf(false) }

    val textColor = if (isPrivate) {
        if (savedValue != savedValueConfirm)
            Color.Red
        else
            Color.Green
    } else
        MaterialTheme.colorScheme.onBackground

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(8.dp),
        elevation = CardDefaults.elevatedCardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        // from https://stackoverflow.com/questions/65304229/toggle-password-field-jetpack-compose
        var isPasswordVisible by remember { mutableStateOf(!isPrivate) }  // rememberSavable?

        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = stringResource(pref.titleId), style = MaterialTheme.typography.titleLarge)
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(textFieldFocusRequester),
                value = if (isEdited || !isPrivate) savedValue else "",
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    textColor = textColor
                ),
                shape = MaterialTheme.shapes.medium,
                singleLine = true,
                onValueChange = {
                    isEdited = true
                    savedValue = it
                },
                visualTransformation = if (isPasswordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    keyboardType = if (isPrivate) KeyboardType.Password else KeyboardType.Text
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                trailingIcon = {
                    if (isPrivate)
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Phosphor.EyeSlash else Phosphor.Eye,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                },
                placeholder = {
                    if (isPrivate && pref.value.isNotEmpty() and !isEdited) {
                        if (isPasswordVisible)
                            Text(pref.value)
                        else
                            Text("**********")
                    } else if (!isPrivate) {
                        Text(pref.value)
                    }
                }
            )
            if (isPrivate && confirm) {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = savedValueConfirm,
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        textColor = textColor
                    ),
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true,
                    onValueChange = {
                        isEdited = true
                        savedValueConfirm = it
                    },
                    visualTransformation = if (isPasswordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Password
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                )
            }
            AnimatedVisibility(visible = notMatching) {
                Text(
                    text = stringResource(id = R.string.prefs_password_match_false),
                    color = MaterialTheme.colorScheme.tertiary
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
                    if (!confirm or (savedValue == savedValueConfirm)) {
                        if (pref.value != savedValue) {
                            pref.value = savedValue
                            onChanged()
                        }
                        openDialogCustom.value = false
                    } else {
                        notMatching = true
                    }
                }
            }
        }
    }
}
