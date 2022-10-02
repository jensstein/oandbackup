package com.machiav3lli.backup.ui.compose.item

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.machiav3lli.backup.MAIN_FILTER_SPECIAL
import com.machiav3lli.backup.MAIN_FILTER_SYSTEM
import com.machiav3lli.backup.MAIN_FILTER_USER
import com.machiav3lli.backup.MODE_APK
import com.machiav3lli.backup.MODE_DATA
import com.machiav3lli.backup.MODE_DATA_DE
import com.machiav3lli.backup.MODE_DATA_EXT
import com.machiav3lli.backup.MODE_DATA_MEDIA
import com.machiav3lli.backup.MODE_DATA_OBB
import com.machiav3lli.backup.R
import com.machiav3lli.backup.SPECIAL_FILTER_ALL
import com.machiav3lli.backup.SPECIAL_FILTER_DISABLED
import com.machiav3lli.backup.SPECIAL_FILTER_LAUNCHABLE
import com.machiav3lli.backup.SPECIAL_FILTER_OLD
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.dbs.entity.Schedule
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.ArrowSquareOut
import com.machiav3lli.backup.ui.compose.icons.phosphor.AsteriskSimple
import com.machiav3lli.backup.ui.compose.icons.phosphor.Checks
import com.machiav3lli.backup.ui.compose.icons.phosphor.CircleWavyWarning
import com.machiav3lli.backup.ui.compose.icons.phosphor.Clock
import com.machiav3lli.backup.ui.compose.icons.phosphor.DiamondsFour
import com.machiav3lli.backup.ui.compose.icons.phosphor.FloppyDisk
import com.machiav3lli.backup.ui.compose.icons.phosphor.GameController
import com.machiav3lli.backup.ui.compose.icons.phosphor.HardDrives
import com.machiav3lli.backup.ui.compose.icons.phosphor.PlayCircle
import com.machiav3lli.backup.ui.compose.icons.phosphor.ProhibitInset
import com.machiav3lli.backup.ui.compose.icons.phosphor.ShieldCheckered
import com.machiav3lli.backup.ui.compose.icons.phosphor.Spinner
import com.machiav3lli.backup.ui.compose.icons.phosphor.User
import com.machiav3lli.backup.ui.compose.theme.ColorAPK
import com.machiav3lli.backup.ui.compose.theme.ColorData
import com.machiav3lli.backup.ui.compose.theme.ColorDeData
import com.machiav3lli.backup.ui.compose.theme.ColorDisabled
import com.machiav3lli.backup.ui.compose.theme.ColorExodus
import com.machiav3lli.backup.ui.compose.theme.ColorExtDATA
import com.machiav3lli.backup.ui.compose.theme.ColorMedia
import com.machiav3lli.backup.ui.compose.theme.ColorOBB
import com.machiav3lli.backup.ui.compose.theme.ColorSpecial
import com.machiav3lli.backup.ui.compose.theme.ColorSystem
import com.machiav3lli.backup.ui.compose.theme.ColorUpdated
import com.machiav3lli.backup.ui.compose.theme.ColorUser
import com.machiav3lli.backup.ui.compose.theme.LocalShapes
import com.machiav3lli.backup.utils.brighter

@Composable
fun ButtonIcon(
    icon: ImageVector,
    @StringRes textId: Int,
    tint: Color? = null
) {
    Icon(
        imageVector = icon,
        contentDescription = stringResource(id = textId),
        modifier = Modifier.size(24.dp),
        tint = tint ?: LocalContentColor.current
    )
}

@Composable
fun PrefIcon(
    icon: ImageVector,
    text: String,
    tint: Color? = null
) {
    Icon(
        imageVector = icon,
        contentDescription = text,
        modifier = Modifier.size(32.dp),
        tint = tint ?: MaterialTheme.colorScheme.onBackground
    )
}

@Composable
fun PackageIcon(
    item: Package?,
    imageData: Any
) {
    AsyncImage(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(LocalShapes.current.medium)),
        model = ImageRequest.Builder(LocalContext.current)
            .crossfade(true)
            .data(imageData)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        error = placeholderIconPainter(item),
        placeholder = placeholderIconPainter(item)
    )
}


@Composable
fun placeholderIconPainter(item: Package?) = painterResource(
    when {
        item?.isSpecial == true -> R.drawable.ic_placeholder_special
        item?.isSystem == true -> R.drawable.ic_placeholder_system
        else -> R.drawable.ic_placeholder_user
    }
)

@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    text: String,
    positive: Boolean = true,
    iconOnSide: Boolean = false,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    TextButton(
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (positive) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.tertiary
        ),
        onClick = onClick
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 4.dp),
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall
        )
        if (icon != null) {
            if (iconOnSide) Spacer(modifier = Modifier.weight(1f))
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = icon,
                contentDescription = text
            )
        }
    }
}

@Composable
fun ElevatedActionButton(
    modifier: Modifier = Modifier,
    text: String,
    positive: Boolean = true,
    icon: ImageVector? = null,
    fullWidth: Boolean = false,
    enabled: Boolean = true,
    colored: Boolean = true,
    withText: Boolean = text.isNotEmpty(),
    onClick: () -> Unit
) {
    ElevatedButton(
        modifier = modifier,
        colors = ButtonDefaults.elevatedButtonColors(
            contentColor = when {
                !colored -> MaterialTheme.colorScheme.onSurface
                positive -> MaterialTheme.colorScheme.onPrimaryContainer
                else -> MaterialTheme.colorScheme.onTertiaryContainer
            },
            containerColor = when {
                !colored -> MaterialTheme.colorScheme.surface
                positive -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.tertiaryContainer
            }
        ),
        enabled = enabled,
        onClick = onClick
    ) {
        if (icon != null) {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = icon,
                contentDescription = text
            )
        }
        if (withText)
            Text(
                modifier = when {
                    fullWidth -> Modifier.weight(1f)
                    else -> Modifier.padding(start = 8.dp)
                },
                text = text,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleSmall
            )
    }
}


@Composable
fun TopBarButton(
    modifier: Modifier = Modifier
        .padding(4.dp)
        .size(52.dp),
    icon: ImageVector,
    description: String = "",
    onClick: () -> Unit
) {
    ElevatedButton(
        modifier = modifier,
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 10.dp),
        shape = MaterialTheme.shapes.medium,
        onClick = { onClick() }
    ) {
        Icon(imageVector = icon, contentDescription = description)
    }
}


@Composable
fun CardButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    tint: Color,
    description: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    ElevatedButton(
        modifier = modifier.padding(4.dp),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = tint.brighter(0.2f),
            contentColor = MaterialTheme.colorScheme.background
        ),
        contentPadding = PaddingValues(12.dp),
        shape = MaterialTheme.shapes.medium,
        enabled = enabled,
        onClick = { onClick() }
    ) {
        Icon(imageVector = icon, contentDescription = description)
        /*Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = description,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }*/
    }
}

@Composable
fun RoundButton(
    modifier: Modifier = Modifier
        .padding(4.dp)
        .size(52.dp),
    icon: ImageVector,
    description: String = "",
    onClick: () -> Unit
) {
    IconButton(
        modifier = modifier,
        onClick = { onClick() }
    ) {
        Icon(imageVector = icon, contentDescription = description)
    }
}

@Composable
fun StateChip(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    text: String,
    color: Color,
    checked: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        modifier = modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp),
        contentPadding = PaddingValues(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (checked) MaterialTheme.colorScheme.onSurface else color,
            containerColor = if (checked) color else Color.Transparent
        ),
        shape = RoundedCornerShape(LocalShapes.current.medium),
        border = BorderStroke(1.dp, color),
        onClick = onClick,
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = icon,
            contentDescription = text
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckChip(
    checked: Boolean,
    textId: Int,
    checkedTextId: Int,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit
) {
    val (checked, check) = remember { mutableStateOf(checked) }

    FilterChip(
        modifier = modifier.padding(vertical = 8.dp, horizontal = 4.dp),
        selected = checked,
        colors = FilterChipDefaults.filterChipColors(
            labelColor = MaterialTheme.colorScheme.onBackground,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            iconColor = MaterialTheme.colorScheme.onBackground,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = MaterialTheme.colorScheme.background,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        leadingIcon = {
            if (checked) ButtonIcon(Phosphor.Checks, R.string.enabled)
        },
        onClick = {
            onCheckedChange(!checked)
            check(!checked)
        },
        label = {
            Row {
                Text(text = if (checked) stringResource(id = checkedTextId) else stringResource(id = textId))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwitchChip(
    firstTextId: Int,
    firstIcon: ImageVector,
    secondTextId: Int,
    secondIcon: ImageVector,
    firstSelected: Boolean = true,
    colors: SelectableChipColors = FilterChipDefaults.filterChipColors(
        labelColor = MaterialTheme.colorScheme.onSurface,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        iconColor = MaterialTheme.colorScheme.onSurface,
        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
        containerColor = MaterialTheme.colorScheme.surface,
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
    ),
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.shapes.small
            )
            .padding(horizontal = 6.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val (firstSelected, selectFirst) = remember { mutableStateOf(firstSelected) }

        FilterChip(
            modifier = Modifier.weight(1f),
            border = FilterChipDefaults.filterChipBorder(
                borderColor = Color.Transparent,
                borderWidth = 0.dp
            ),
            selected = firstSelected,
            colors = colors,
            onClick = {
                onCheckedChange(true)
                selectFirst(true)
            },
            leadingIcon = {
                ButtonIcon(firstIcon, firstTextId)
            },
            label = {
                Row(
                    Modifier
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = stringResource(id = firstTextId),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        )
        FilterChip(
            modifier = Modifier.weight(1f),
            border = FilterChipDefaults.filterChipBorder(
                borderColor = Color.Transparent,
                borderWidth = 0.dp
            ),
            selected = !firstSelected,
            colors = colors,
            onClick = {
                onCheckedChange(false)
                selectFirst(false)
            },
            label = {
                Row(
                    Modifier
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = stringResource(id = secondTextId),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            },
            trailingIcon = {
                ButtonIcon(secondIcon, secondTextId)
            }
        )
    }
}

@Composable
fun SelectableRow(
    modifier: Modifier = Modifier,
    title: String,
    selectedState: MutableState<Boolean>,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                selectedState.value = true
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selectedState.value,
            onClick = {
                selectedState.value = true
                onClick()
            },
            modifier = Modifier.padding(horizontal = 8.dp),
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onSurface
            )
        )
        Text(text = title)
    }
}

@Composable
fun CheckableRow(
    title: String,
    checkedState: MutableState<Boolean>,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                checkedState.value = !checkedState.value
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checkedState.value,
            onCheckedChange = {
                checkedState.value = it
            },
            modifier = Modifier.padding(horizontal = 8.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
            )
        )
        Text(text = title)
    }
}

@Composable
fun StatefulAnimatedVisibility(
    currentState: Boolean = false,
    enterPositive: EnterTransition,
    exitPositive: ExitTransition,
    enterNegative: EnterTransition,
    exitNegative: ExitTransition,
    expandedView: @Composable (AnimatedVisibilityScope.() -> Unit),
    collapsedView: @Composable (AnimatedVisibilityScope.() -> Unit)
) {
    AnimatedVisibility(
        visible = !currentState,
        enter = enterNegative,
        exit = exitNegative,
        content = collapsedView
    )
    AnimatedVisibility(
        visible = currentState,
        enter = enterPositive,
        exit = exitPositive,
        content = expandedView
    )
}

@Composable
fun HorizontalExpandingVisibility(
    expanded: Boolean = false,
    expandedView: @Composable (AnimatedVisibilityScope.() -> Unit),
    collapsedView: @Composable (AnimatedVisibilityScope.() -> Unit)
) = StatefulAnimatedVisibility(
    currentState = expanded,
    enterPositive = expandHorizontally(expandFrom = Alignment.End),
    exitPositive = shrinkHorizontally(shrinkTowards = Alignment.End),
    enterNegative = expandHorizontally(expandFrom = Alignment.Start),
    exitNegative = shrinkHorizontally(shrinkTowards = Alignment.Start),
    collapsedView = collapsedView,
    expandedView = expandedView
)

@Composable
fun VerticalFadingVisibility(
    expanded: Boolean = false,
    expandedView: @Composable (AnimatedVisibilityScope.() -> Unit),
    collapsedView: @Composable (AnimatedVisibilityScope.() -> Unit)
) = StatefulAnimatedVisibility(
    currentState = expanded,
    enterPositive = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
    exitPositive = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom),
    enterNegative = fadeIn() + expandVertically(expandFrom = Alignment.Top),
    exitNegative = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
    collapsedView = collapsedView,
    expandedView = expandedView
)

@Composable
fun PackageLabels(
    item: Package
) {
    AnimatedVisibility(visible = item.isUpdated) {
        ButtonIcon(
            Phosphor.CircleWavyWarning, R.string.radio_updated,
            tint = ColorUpdated
        )
    }
    AnimatedVisibility(visible = item.hasMediaData) {
        ButtonIcon(
            Phosphor.PlayCircle, R.string.radio_mediadata,
            tint = ColorMedia
        )
    }
    AnimatedVisibility(visible = item.hasObbData) {
        ButtonIcon(
            Phosphor.GameController, R.string.radio_obbdata,
            tint = ColorOBB
        )
    }
    AnimatedVisibility(visible = item.hasExternalData) {
        ButtonIcon(
            Phosphor.FloppyDisk, R.string.radio_externaldata,
            tint = ColorExtDATA
        )
    }
    AnimatedVisibility(visible = item.hasDevicesProtectedData) {
        ButtonIcon(
            Phosphor.ShieldCheckered, R.string.radio_deviceprotecteddata,
            tint = ColorDeData
        )
    }
    AnimatedVisibility(visible = item.hasAppData) {
        ButtonIcon(
            Phosphor.HardDrives, R.string.radio_data,
            tint = ColorData
        )
    }
    AnimatedVisibility(visible = item.hasApk) {
        ButtonIcon(
            Phosphor.DiamondsFour, R.string.radio_apk,
            tint = ColorAPK
        )
    }
    ButtonIcon(
        when {
            item.isSpecial -> Phosphor.AsteriskSimple
            item.isSystem -> Phosphor.Spinner
            else -> Phosphor.User
        },
        R.string.app_s_type_title,
        tint = when {
            item.isDisabled -> ColorDisabled
            item.isSpecial -> ColorSpecial
            item.isSystem -> ColorSystem
            else -> ColorUser
        }
    )
}

@Composable
fun BackupLabels(
    item: Backup
) {
    AnimatedVisibility(visible = item.hasMediaData) {
        ButtonIcon(
            Phosphor.PlayCircle, R.string.radio_mediadata,
            tint = ColorMedia
        )
    }
    AnimatedVisibility(visible = item.hasObbData) {
        ButtonIcon(
            Phosphor.GameController, R.string.radio_obbdata,
            tint = ColorOBB
        )
    }
    AnimatedVisibility(visible = item.hasExternalData) {
        ButtonIcon(
            Phosphor.FloppyDisk, R.string.radio_externaldata,
            tint = ColorExtDATA
        )
    }
    AnimatedVisibility(visible = item.hasDevicesProtectedData) {
        ButtonIcon(
            Phosphor.ShieldCheckered, R.string.radio_deviceprotecteddata,
            tint = ColorDeData
        )
    }
    AnimatedVisibility(visible = item.hasAppData) {
        ButtonIcon(
            Phosphor.HardDrives, R.string.radio_data,
            tint = ColorData
        )
    }
    AnimatedVisibility(visible = item.hasApk) {
        ButtonIcon(
            Phosphor.DiamondsFour, R.string.radio_apk,
            tint = ColorAPK
        )
    }
}


@Composable
fun ScheduleTypes(item: Schedule) {
    AnimatedVisibility(visible = item.mode and MODE_DATA_MEDIA == MODE_DATA_MEDIA) {
        ButtonIcon(
            Phosphor.PlayCircle, R.string.radio_mediadata,
            tint = ColorMedia
        )
    }
    AnimatedVisibility(visible = item.mode and MODE_DATA_OBB == MODE_DATA_OBB) {
        ButtonIcon(
            Phosphor.GameController, R.string.radio_obbdata,
            tint = ColorOBB
        )
    }
    AnimatedVisibility(visible = item.mode and MODE_DATA_EXT == MODE_DATA_EXT) {
        ButtonIcon(
            Phosphor.FloppyDisk, R.string.radio_externaldata,
            tint = ColorExtDATA
        )
    }
    AnimatedVisibility(visible = item.mode and MODE_DATA_DE == MODE_DATA_DE) {
        ButtonIcon(
            Phosphor.ShieldCheckered, R.string.radio_deviceprotecteddata,
            tint = ColorDeData
        )
    }
    AnimatedVisibility(visible = item.mode and MODE_DATA == MODE_DATA) {
        ButtonIcon(
            Phosphor.HardDrives, R.string.radio_data,
            tint = ColorData
        )
    }
    AnimatedVisibility(visible = item.mode and MODE_APK == MODE_APK) {
        ButtonIcon(
            Phosphor.DiamondsFour, R.string.radio_apk,
            tint = ColorAPK
        )
    }
}


@Composable
fun ScheduleFilters(
    item: Schedule
) {
    AnimatedVisibility(visible = item.filter and MAIN_FILTER_SYSTEM == MAIN_FILTER_SYSTEM) {
        ButtonIcon(
            Phosphor.Spinner, R.string.radio_system,
            tint = ColorSystem
        )
    }
    AnimatedVisibility(visible = item.filter and MAIN_FILTER_USER == MAIN_FILTER_USER) {
        ButtonIcon(
            Phosphor.User, R.string.radio_user,
            tint = ColorUser
        )
    }
    AnimatedVisibility(visible = item.filter and MAIN_FILTER_SPECIAL == MAIN_FILTER_SPECIAL) {
        ButtonIcon(
            Phosphor.AsteriskSimple, R.string.radio_special,
            tint = ColorSpecial
        )
    }
    AnimatedVisibility(visible = item.specialFilter != SPECIAL_FILTER_ALL) {
        ButtonIcon(
            when (item.specialFilter) {
                SPECIAL_FILTER_DISABLED -> Phosphor.ProhibitInset
                SPECIAL_FILTER_LAUNCHABLE -> Phosphor.ArrowSquareOut
                SPECIAL_FILTER_OLD -> Phosphor.Clock
                else -> Phosphor.CircleWavyWarning
            },
            R.string.app_s_type_title,
            tint = when (item.specialFilter) {
                SPECIAL_FILTER_DISABLED -> ColorDeData
                SPECIAL_FILTER_LAUNCHABLE -> ColorOBB
                SPECIAL_FILTER_OLD -> ColorExodus
                else -> ColorUpdated
            }
        )
    }
}

@Composable
fun TitleText(
    textId: Int,
    modifier: Modifier = Modifier
) = Text(
    text = stringResource(id = textId),
    style = MaterialTheme.typography.titleMedium,
    fontWeight = FontWeight.Bold,
    modifier = modifier
)

@Composable
fun DoubleVerticalText(
    upperText: String,
    bottomText: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = upperText,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = bottomText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium.copy(MaterialTheme.colorScheme.onSurfaceVariant)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardSubRow(
    text: String,
    icon: ImageVector,
    iconColor: Color = MaterialTheme.colorScheme.onBackground,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = text, tint = iconColor)
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}