package com.machiav3lli.backup.ui.compose.item

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.machiav3lli.backup.ICON_SIZE_SMALL
import com.machiav3lli.backup.preferences.pref_allPrefsShouldLookEqual
import com.machiav3lli.backup.ui.compose.ifThen
import com.machiav3lli.backup.ui.item.BooleanPref
import com.machiav3lli.backup.ui.item.EnumPref
import com.machiav3lli.backup.ui.item.IntPref
import com.machiav3lli.backup.ui.item.ListPref
import com.machiav3lli.backup.ui.item.PasswordPref
import com.machiav3lli.backup.ui.item.Pref
import com.machiav3lli.backup.ui.item.Pref.Companion.prefChangeListeners
import com.machiav3lli.backup.ui.item.StringPref
import kotlin.math.roundToInt

@Composable
fun BasePreference(
    modifier: Modifier = Modifier,
    pref: Pref,
    summary: String? = null,
    @StringRes titleId: Int = -1,
    @StringRes summaryId: Int = -1,
    index: Int = 0,
    groupSize: Int = 1,
    icon: (@Composable () -> Unit)? = null,
    endWidget: (@Composable (isEnabled: Boolean) -> Unit)? = null,
    bottomWidget: (@Composable (isEnabled: Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    var isEnabled by remember {
        mutableStateOf(pref.enableIf?.invoke() ?: true)
    }   //TODO hg42 remove remember ???

    SideEffect {
        pref.enableIf?.run {
            prefChangeListeners.put(pref) {
                isEnabled = pref.enableIf.invoke()
            }
        }
    }

    val base = index.toFloat() / groupSize
    val rank = (index + 1f) / groupSize


    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    topStart = if (base == 0f) MaterialTheme.shapes.large.topStart
                    else MaterialTheme.shapes.small.topStart,
                    topEnd = if (base == 0f) MaterialTheme.shapes.large.topEnd
                    else MaterialTheme.shapes.small.topEnd,
                    bottomStart = if (rank == 1f) MaterialTheme.shapes.large.bottomStart
                    else MaterialTheme.shapes.small.bottomStart,
                    bottomEnd = if (rank == 1f) MaterialTheme.shapes.large.bottomEnd
                    else MaterialTheme.shapes.small.bottomEnd
                )
            )
            .ifThen(onClick != null) {
                clickable(enabled = isEnabled, onClick = onClick!!)
            },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme
                .surfaceColorAtElevation(
                    if (pref_allPrefsShouldLookEqual.value) 24.dp
                    else (rank * 24).dp
                )
                .copy(alpha = 0.8f),
        ),
        leadingContent = icon,
        headlineContent = {
            Text(
                text = if (titleId != -1) stringResource(id = titleId) else pref.key,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 16.sp
            )
        },
        supportingContent = {
            Column(
                modifier = Modifier
                    .ifThen(!isEnabled) {
                        alpha(0.3f)
                    }
            ) {
                if (summary != null) {
                    Text(
                        text = summary,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                if (summaryId != -1) {
                    val summaryText = stringResource(id = summaryId)
                    Text(
                        text = summaryText,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                bottomWidget?.let { widget ->
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    widget(isEnabled)
                }
            }
        },
        trailingContent = if (endWidget != null) {
            { endWidget(isEnabled) }
        } else null,
    )
}

@Composable
fun LaunchPreference(
    modifier: Modifier = Modifier,
    pref: Pref,
    index: Int = 0,
    groupSize: Int = 1,
    summary: String? = null,
    onClick: (() -> Unit) = {},
) {
    BasePreference(
        modifier = modifier,
        pref = pref,
        summary = summary,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        icon = {
            pref.icon?.let { icon ->
                PrefIcon(
                    icon = icon,
                    text = stringResource(id = pref.titleId),
                )
            } ?: run {
                Spacer(modifier = Modifier.requiredWidth(36.dp))
            }
        },
        index = index,
        groupSize = groupSize,
        onClick = onClick,
    )
}

@Composable
fun StringPreference(
    modifier: Modifier = Modifier,
    pref: StringPref,
    index: Int = 0,
    groupSize: Int = 1,
    onClick: (() -> Unit) = {},
) {
    BasePreference(
        modifier = modifier,
        pref = pref,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        summary = pref.value,
        icon = {
            pref.icon?.let { icon ->
                PrefIcon(
                    icon = icon,
                    text = stringResource(id = pref.titleId),
                )
            } ?: run {
                Spacer(modifier = Modifier.requiredWidth(36.dp))
            }
        },
        index = index,
        groupSize = groupSize,
        onClick = onClick,
    )
}

@Composable
fun PasswordPreference(
    modifier: Modifier = Modifier,
    pref: PasswordPref,
    index: Int = 0,
    groupSize: Int = 1,
    onClick: (() -> Unit) = {},
) {
    BasePreference(
        modifier = modifier,
        pref = pref,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        summary = if (pref.value.isNotEmpty()) "*****" else "-----",
        icon = {
            pref.icon?.let { icon ->
                PrefIcon(
                    icon = icon,
                    text = stringResource(id = pref.titleId),
                )
            } ?: run {
                Spacer(modifier = Modifier.requiredWidth(36.dp))
            }
        },
        index = index,
        groupSize = groupSize,
        onClick = onClick,
    )
}

@Composable
fun EnumPreference(
    modifier: Modifier = Modifier,
    pref: EnumPref,
    index: Int = 0,
    groupSize: Int = 1,
    onClick: (() -> Unit) = {},
) {
    BasePreference(
        modifier = modifier,
        pref = pref,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        summary = pref.entries[pref.value]?.let { stringResource(id = it) },
        icon = {
            if (pref.icon != null) PrefIcon(
                icon = pref.icon,
                text = stringResource(id = pref.titleId),
            )
            else Spacer(modifier = Modifier.requiredWidth(36.dp))
        },
        index = index,
        groupSize = groupSize,
        onClick = onClick,
    )
}

@Composable
fun ListPreference(
    modifier: Modifier = Modifier,
    pref: ListPref,
    index: Int = 0,
    groupSize: Int = 1,
    onClick: (() -> Unit) = {},
) {
    BasePreference(
        modifier = modifier,
        pref = pref,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        summary = pref.entries[pref.value],
        icon = {
            if (pref.icon != null) PrefIcon(
                icon = pref.icon,
                text = stringResource(id = pref.titleId),
            )
            else Spacer(modifier = Modifier.requiredWidth(36.dp))
        },
        index = index,
        groupSize = groupSize,
        onClick = onClick,
    )
}

@Composable
fun SwitchPreference(
    modifier: Modifier = Modifier,
    pref: BooleanPref,
    index: Int = 0,
    groupSize: Int = 1,
    onCheckedChange: ((Boolean) -> Unit) = {},
) {
    val context = LocalContext.current
    var checked by remember(pref.value) { mutableStateOf(pref.value) }  //TODO hg42 remove remember ???
    val check = { value: Boolean ->
        pref.value = value
        checked = value
    }

    BasePreference(
        modifier = modifier,
        pref = pref,
        summary = pref.summary,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        icon = {
            if (pref.icon != null) PrefIcon(
                icon = pref.icon,
                text = stringResource(id = pref.titleId),
            )
            else Spacer(modifier = Modifier.requiredWidth(36.dp))
        },
        index = index,
        groupSize = groupSize,
        onClick = {
            onCheckedChange(!checked)
            check(!checked)
        },
        endWidget = { isEnabled ->
            Switch(
                modifier = Modifier
                    .height(ICON_SIZE_SMALL),
                checked = checked,
                onCheckedChange = {
                    onCheckedChange(it)
                    check(it)
                },
                enabled = isEnabled,
            )
        },
    )
}

@Composable
fun CheckboxPreference(
    modifier: Modifier = Modifier,
    pref: BooleanPref,
    index: Int = 0,
    groupSize: Int = 1,
    onCheckedChange: ((Boolean) -> Unit) = {},
) {
    var checked by remember(pref.value) { mutableStateOf(pref.value) }  //TODO hg42 remove remember ???
    val check = { value: Boolean ->
        pref.value = value
        checked = value
    }

    BasePreference(
        modifier = modifier,
        pref = pref,
        summary = pref.summary,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        icon = {
            if (pref.icon != null) PrefIcon(
                icon = pref.icon,
                text = stringResource(id = pref.titleId),
            )
            else Spacer(modifier = Modifier.requiredWidth(36.dp))
        },
        index = index,
        groupSize = groupSize,
        onClick = {
            onCheckedChange(!checked)
            check(!checked)
        },
        endWidget = { isEnabled ->
            Checkbox(
                modifier = Modifier
                    .height(ICON_SIZE_SMALL),
                checked = checked,
                onCheckedChange = {
                    onCheckedChange(it)
                    check(it)
                },
                enabled = isEnabled,
            )
        },
    )
}

@Composable
fun BooleanPreference(
    modifier: Modifier = Modifier,
    pref: BooleanPref,
    index: Int = 0,
    groupSize: Int = 1,
    onCheckedChange: ((Boolean) -> Unit) = {},
) {
    SwitchPreference(
        modifier = modifier,
        pref = pref,
        index = index,
        groupSize = groupSize,
        onCheckedChange = onCheckedChange,
    )
}

@Composable
fun SeekBarPreference(
    modifier: Modifier = Modifier,
    pref: IntPref,
    index: Int = 0,
    groupSize: Int = 1,
    onValueChange: ((Int) -> Unit) = {},
) {
    var sliderPosition by remember {    //TODO hg42 remove remember ???
        mutableIntStateOf(
            pref.entries.indexOfFirst { it >= pref.value }.let {
                if (it < 0)
                    pref.entries.indexOfFirst { it >= (pref.defaultValue as Int) }
                else
                    it
            }.let {
                if (it < 0)
                    0
                else
                    it
            }
        )
    }
    val savePosition = { pos: Int ->
        val value = pref.entries[pos]
        pref.value = value
        sliderPosition = pos
    }
    val last = pref.entries.size - 1

    BasePreference(
        modifier = modifier,
        pref = pref,
        summary = pref.summary,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        icon = {
            if (pref.icon != null) PrefIcon(
                icon = pref.icon,
                text = stringResource(id = pref.titleId),
            )
            else Spacer(modifier = Modifier.requiredWidth(36.dp))
        },
        index = index,
        groupSize = groupSize,
        bottomWidget = { isEnabled ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Slider(
                    modifier = Modifier.weight(1f, false),
                    value = sliderPosition.toFloat(),
                    valueRange = 0.toFloat()..last.toFloat(),
                    onValueChange = { sliderPosition = it.roundToInt() },
                    onValueChangeFinished = {
                        onValueChange(sliderPosition)
                        savePosition(sliderPosition)
                    },
                    steps = last - 1,
                    enabled = isEnabled
                )
                Spacer(modifier = Modifier.requiredWidth(8.dp))
                Text(
                    text = pref.entries[sliderPosition].toString(),
                    modifier = Modifier.widthIn(min = 48.dp)
                )
            }
        },
    )
}

@Composable
fun IntPreference(
    modifier: Modifier = Modifier,
    pref: IntPref,
    index: Int = 0,
    groupSize: Int = 1,
    onValueChange: ((Int) -> Unit) = {},
) {
    SeekBarPreference(
        modifier = modifier,
        pref = pref,
        index = index,
        groupSize = groupSize,
        onValueChange = onValueChange,
    )
}
