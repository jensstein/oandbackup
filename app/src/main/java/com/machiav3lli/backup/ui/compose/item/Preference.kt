package com.machiav3lli.backup.ui.compose.item

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
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
import com.machiav3lli.backup.PrefsDependencies
import com.machiav3lli.backup.ui.compose.ifThen
import com.machiav3lli.backup.ui.item.BooleanPref
import com.machiav3lli.backup.ui.item.EnumPref
import com.machiav3lli.backup.ui.item.IntPref
import com.machiav3lli.backup.ui.item.ListPref
import com.machiav3lli.backup.ui.item.Pref
import com.machiav3lli.backup.utils.getDefaultSharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun BasePreference(
    modifier: Modifier = Modifier,
    key: String,
    summary: String? = null,
    @StringRes titleId: Int = -1,
    @StringRes summaryId: Int = -1,
    isEnabled: Boolean = true,
    index: Int = 0,
    groupSize: Int = 1,
    icon: (@Composable () -> Unit)? = null,
    endWidget: (@Composable () -> Unit)? = null,
    bottomWidget: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val base = index.toFloat() / groupSize
    val rank = (index + 1f) / groupSize

    Column(
        modifier = Modifier
            .clip(
                RoundedCornerShape(
                    topStart = if (base == 0f) 16.dp else 6.dp,
                    topEnd = if (base == 0f) 16.dp else 6.dp,
                    bottomStart = if (rank == 1f) 16.dp else 6.dp,
                    bottomEnd = if (rank == 1f) 16.dp else 6.dp
                )
            )
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation((rank * 24).dp))
            .ifThen(onClick != null) {
                clickable(enabled = isEnabled, onClick = onClick!!)
            }
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.let {
                icon()
                Spacer(modifier = Modifier.requiredWidth(8.dp))
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .ifThen(!isEnabled) {
                        alpha(0.3f)
                    }
            ) {
                Text(
                    text = if (titleId != -1) stringResource(id = titleId) else key,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp
                )
                var summaryText = if (summaryId != -1) stringResource(id = summaryId) else ""
                if (summaryText.isNotEmpty() && !summary.isNullOrEmpty())
                    summaryText += " : "
                summaryText += summary ?: ""
                Text(
                    text = summaryText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                bottomWidget?.let {
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    bottomWidget()
                }
            }
            endWidget?.let {
                Spacer(modifier = Modifier.requiredWidth(8.dp))
                endWidget()
            }
        }
    }
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
    val context = LocalContext.current
    var isEnabled by remember(context.PrefsDependencies[pref]) {
        mutableStateOf(context.PrefsDependencies[pref] ?: true)
    }

    SideEffect {
        CoroutineScope(Dispatchers.Default).launch {
            context.getDefaultSharedPreferences()
                .registerOnSharedPreferenceChangeListener { _, _ ->
                    isEnabled = context.PrefsDependencies[pref] ?: true
                }
        }
    }

    BasePreference(
        modifier = modifier,
        key = pref.key,
        summary = summary,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        icon = {
            if (pref.icon != null) PrefIcon(
                icon = pref.icon,
                text = stringResource(id = pref.titleId),
            )
            else Spacer(modifier = Modifier.requiredWidth(36.dp))
        },
        isEnabled = isEnabled,
        index = index,
        groupSize = groupSize,
        onClick = onClick
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
    val context = LocalContext.current
    var isEnabled by remember(context.PrefsDependencies[pref]) {
        mutableStateOf(context.PrefsDependencies[pref] ?: true)
    }

    SideEffect {
        CoroutineScope(Dispatchers.Default).launch {
            context.getDefaultSharedPreferences()
                .registerOnSharedPreferenceChangeListener { _, _ ->
                    isEnabled = context.PrefsDependencies[pref] ?: true
                }
        }
    }

    BasePreference(
        modifier = modifier,
        key = pref.key,
        titleId = pref.titleId,
        summaryId = pref.entries[pref.value] ?: pref.summaryId,
        icon = {
            if (pref.icon != null) PrefIcon(
                icon = pref.icon,
                text = stringResource(id = pref.titleId),
            )
            else Spacer(modifier = Modifier.requiredWidth(36.dp))
        },
        isEnabled = isEnabled,
        index = index,
        groupSize = groupSize,
        onClick = onClick
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
    val context = LocalContext.current
    var isEnabled by remember(context.PrefsDependencies[pref]) {
        mutableStateOf(context.PrefsDependencies[pref] ?: true)
    }

    SideEffect {
        CoroutineScope(Dispatchers.Default).launch {
            context.getDefaultSharedPreferences()
                .registerOnSharedPreferenceChangeListener { _, _ ->
                    isEnabled = context.PrefsDependencies[pref] ?: true
                }
        }
    }

    BasePreference(
        modifier = modifier,
        key = pref.key,
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
        isEnabled = isEnabled,
        index = index,
        groupSize = groupSize,
        onClick = onClick
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
    var isEnabled by remember(context.PrefsDependencies[pref]) {
        mutableStateOf(context.PrefsDependencies[pref] ?: true)
    }
    var checked by remember(pref.value) { mutableStateOf(pref.value) }
    val check = { value: Boolean ->
        pref.value = value
        checked = value
    }
    SideEffect {
        CoroutineScope(Dispatchers.Default).launch {
            context.getDefaultSharedPreferences()
                .registerOnSharedPreferenceChangeListener { _, _ ->
                    isEnabled = context.PrefsDependencies[pref] ?: true
                }
        }
    }

    BasePreference(
        modifier = modifier,
        key = pref.key,
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
        isEnabled = isEnabled,
        index = index,
        groupSize = groupSize,
        onClick = {
            onCheckedChange(!checked)
            check(!checked)
        },
        endWidget = {
            Switch(
                modifier = Modifier
                    .height(24.dp),
                checked = checked,
                onCheckedChange = {
                    onCheckedChange(it)
                    check(it)
                },
                enabled = isEnabled,
            )
        }
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
    val context = LocalContext.current
    var isEnabled by remember(context.PrefsDependencies[pref]) {
        mutableStateOf(context.PrefsDependencies[pref] ?: true)
    }
    var checked by remember(pref.value) { mutableStateOf(pref.value) }
    val check = { value: Boolean ->
        pref.value = value
        checked = value
    }

    SideEffect {
        CoroutineScope(Dispatchers.Default).launch {
            context.getDefaultSharedPreferences()
                .registerOnSharedPreferenceChangeListener { _, _ ->
                    isEnabled = context.PrefsDependencies[pref] ?: true
                }
        }
    }

    BasePreference(
        modifier = modifier,
        key = pref.key,
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
        isEnabled = isEnabled,
        index = index,
        groupSize = groupSize,
        onClick = {
            onCheckedChange(!checked)
            check(!checked)
        },
        endWidget = {
            Checkbox(
                modifier = Modifier
                    .height(24.dp),
                checked = checked,
                onCheckedChange = {
                    onCheckedChange(it)
                    check(it)
                },
                enabled = isEnabled,
            )
        }
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
    val context = LocalContext.current
    var isEnabled by remember(context.PrefsDependencies[pref]) {
        mutableStateOf(context.PrefsDependencies[pref] ?: true)
    }
    val currentValue = pref.value
    var sliderPosition by remember {
        mutableStateOf(
            pref.entries.indexOfFirst { it == currentValue }.let {
                if (it < 0)
                    pref.entries.indexOfFirst { it == pref.defaultValue }
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

    SideEffect {
        CoroutineScope(Dispatchers.Default).launch {
            context.getDefaultSharedPreferences()
                .registerOnSharedPreferenceChangeListener { _, _ ->
                    isEnabled = context.PrefsDependencies[pref] ?: true
                }
        }
    }

    BasePreference(
        modifier = modifier,
        key = pref.key,
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
        isEnabled = isEnabled,
        index = index,
        groupSize = groupSize,
        bottomWidget = {
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
        }
    )
}
