package com.machiav3lli.backup.ui.compose.item

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.ui.compose.ifThen
import com.machiav3lli.backup.ui.item.Pref
import kotlin.math.roundToInt

@Composable
fun BasePreference(
    modifier: Modifier = Modifier,
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    summary: String? = null,
    isEnabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
    endWidget: (@Composable () -> Unit)? = null,
    bottomWidget: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .ifThen(onClick != null) {
                clickable(enabled = isEnabled, onClick = onClick!!)
            }


    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp),
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
                    text = stringResource(id = titleId),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp
                )
                if (summaryId != -1 || summary != null) {
                    Text(
                        text = summary ?: stringResource(id = summaryId),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
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
    pref: Pref.LinkPref,
    isEnabled: Boolean = true,
    summary: String? = null,
    onClick: (() -> Unit) = {},
) {
    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        summary = summary,
        icon = {
            if (pref.iconId != -1) PrefIcon(
                iconId = pref.iconId,
                text = stringResource(id = pref.titleId),
                tint = pref.iconTint
            )
            else Spacer(modifier = Modifier.requiredWidth(36.dp))
        },
        isEnabled = isEnabled,
        onClick = onClick // TODO add Composable annotation
    )
}

@Composable
fun SwitchPreference(
    modifier: Modifier = Modifier,
    pref: Pref.BooleanPref,
    isEnabled: Boolean = true,
    onCheckedChange: ((Boolean) -> Unit) = {},
) {
    var checked by remember(OABX.prefFlag(pref.key, pref.defaultValue)) {
        mutableStateOf(OABX.prefFlag(pref.key, pref.defaultValue))
    }
    val check = { value: Boolean ->
        OABX.setPrefFlag(pref.key, value)
        checked = value
    }

    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        icon = {
            if (pref.iconId != -1) PrefIcon(
                iconId = pref.iconId,
                text = stringResource(id = pref.titleId),
                tint = pref.iconTint
            )
            else Spacer(modifier = Modifier.requiredWidth(36.dp))
        },
        isEnabled = isEnabled,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckboxPreference(
    modifier: Modifier = Modifier,
    pref: Pref.BooleanPref,
    isEnabled: Boolean = true,
    onCheckedChange: ((Boolean) -> Unit) = {},
) {
    var checked by remember(OABX.prefFlag(pref.key, pref.defaultValue)) {
        mutableStateOf(OABX.prefFlag(pref.key, pref.defaultValue))
    }
    val check = { checks: Boolean ->
        OABX.setPrefFlag(pref.key, checks)
        checked = checks
    }

    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        icon = {
            if (pref.iconId != -1) PrefIcon(
                iconId = pref.iconId,
                text = stringResource(id = pref.titleId),
                tint = pref.iconTint
            )
            else Spacer(modifier = Modifier.requiredWidth(36.dp))
        },
        isEnabled = isEnabled,
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
    pref: Pref.IntPref,
    isEnabled: Boolean = true,
    onValueChange: ((Int) -> Unit) = {},
) {
    var sliderPosition by remember(OABX.prefInt(pref.key, pref.defaultValue)) {
        mutableStateOf(OABX.prefInt(pref.key, pref.defaultValue).toFloat())
    }
    val savePosition = { value: Float ->
        OABX.setPrefInt(pref.key, value.roundToInt())
        sliderPosition = value
    }
    val base = pref.entries.min()
    val top = pref.entries.max()
    val range = pref.entries.size

    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        icon = {
            if (pref.iconId != -1) PrefIcon(
                iconId = pref.iconId,
                text = stringResource(id = pref.titleId),
                tint = pref.iconTint
            )
            else Spacer(modifier = Modifier.requiredWidth(36.dp))
        },
        isEnabled = isEnabled,
        bottomWidget = {
            Row {
                Slider(
                    modifier = Modifier
                        .requiredHeight(24.dp)
                        .weight(1f),
                    value = sliderPosition,
                    valueRange = base.toFloat()..top.toFloat(),
                    onValueChange = { sliderPosition = it },
                    onValueChangeFinished = {
                        onValueChange(sliderPosition.roundToInt())
                        savePosition(sliderPosition)
                    },
                    steps = range - 2,
                    enabled = isEnabled
                )
                Spacer(modifier = Modifier.requiredWidth(8.dp))
                Text(
                    text = sliderPosition.roundToInt().toString(),
                    modifier = Modifier.widthIn(min = 24.dp)
                )
            }
        }
    )
}