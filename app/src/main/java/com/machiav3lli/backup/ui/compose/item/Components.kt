package com.machiav3lli.backup.ui.compose.item

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Chip
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material.SelectableChipColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
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
import com.machiav3lli.backup.ui.compose.theme.APK
import com.machiav3lli.backup.ui.compose.theme.Data
import com.machiav3lli.backup.ui.compose.theme.DeData
import com.machiav3lli.backup.ui.compose.theme.Exodus
import com.machiav3lli.backup.ui.compose.theme.ExtDATA
import com.machiav3lli.backup.ui.compose.theme.LocalShapes
import com.machiav3lli.backup.ui.compose.theme.Media
import com.machiav3lli.backup.ui.compose.theme.OBB
import com.machiav3lli.backup.ui.compose.theme.Special
import com.machiav3lli.backup.ui.compose.theme.System
import com.machiav3lli.backup.ui.compose.theme.Updated
import com.machiav3lli.backup.ui.compose.theme.User

@Composable
fun PackageIcon(
    item: Package,
    imageData: Any
) {
    AsyncImage(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(LocalShapes.current.medium)),
        model = imageData,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        error = placeholderIconPainter(item),
        placeholder = placeholderIconPainter(item)
    )
}

@Composable
fun placeholderIconPainter(item: Package) = painterResource(
    when {
        item.isSpecial -> R.drawable.ic_placeholder_special
        item.isSystem -> R.drawable.ic_placeholder_system
        else -> R.drawable.ic_placeholder_user
    }
)

@Composable
fun ActionButton(
    modifier: Modifier = Modifier.fillMaxWidth(),
    text: String,
    positive: Boolean = true,
    icon: Painter? = null,
    onClick: () -> Unit
) {
    TextButton(
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (positive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
        ),
        onClick = onClick
    ) {
        Text(
            modifier = Modifier.padding(start = 4.dp),
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall
        )
        if (icon != null) {
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                modifier = Modifier.size(20.dp),
                painter = icon,
                contentDescription = text
            )
        }
    }
}

@Composable
fun ElevatedActionButton(
    modifier: Modifier = Modifier.fillMaxWidth(),
    text: String,
    positive: Boolean = true,
    icon: Painter? = null,
    fullWidth: Boolean = false,
    onClick: () -> Unit
) {
    ElevatedButton(
        modifier = modifier,
        colors = ButtonDefaults.elevatedButtonColors(
            contentColor = if (positive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
            containerColor = if (positive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
        ),
        onClick = onClick
    ) {
        if (icon != null) {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = icon,
                contentDescription = text
            )
        }
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
    icon: Painter,
    description: String = "",
    onClick: () -> Unit
) {
    ElevatedButton(
        modifier = modifier,
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 10.dp),
        shape = MaterialTheme.shapes.medium,
        onClick = { onClick() }
    ) {
        Icon(painter = icon, contentDescription = description)
    }
}


@Composable
fun CardButton(
    modifier: Modifier = Modifier,
    icon: Painter,
    tint: Color,
    description: String,
    onClick: () -> Unit
) {
    ElevatedButton(
        modifier = modifier.padding(4.dp),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = tint,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        contentPadding = PaddingValues(12.dp),
        shape = MaterialTheme.shapes.medium,
        onClick = { onClick() }
    ) {
        Icon(painter = icon, contentDescription = description)
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
    icon: Painter,
    description: String = "",
    onClick: () -> Unit
) {
    IconButton(
        modifier = modifier,
        onClick = { onClick() }
    ) {
        Icon(painter = icon, contentDescription = description)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ActionChip(
    modifier: Modifier = Modifier,
    icon: Painter,
    text: String,
    withText: Boolean = true,
    positive: Boolean = true,
    colored: Boolean = true,
    enabled: Boolean = true,
    fullWidth: Boolean = false,
    onClick: () -> Unit
) {
    Chip(
        modifier = modifier,
        colors = ChipDefaults.chipColors(
            backgroundColor = when {
                positive && colored -> MaterialTheme.colorScheme.primaryContainer
                colored -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surface
            },
            contentColor = when {
                positive && colored -> MaterialTheme.colorScheme.onPrimaryContainer
                colored -> MaterialTheme.colorScheme.onSecondaryContainer
                else -> MaterialTheme.colorScheme.onSurface
            },
            disabledBackgroundColor = MaterialTheme.colorScheme.background,
            disabledContentColor = MaterialTheme.colorScheme.surface
        ),
        enabled = enabled,
        onClick = onClick,
    ) {
        Row(
            Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = icon,
                contentDescription = text
            )
            if (withText) Text(
                modifier = when {
                    fullWidth -> Modifier.weight(1f)
                    else -> Modifier.padding(start = 8.dp)
                },
                text = text,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@Composable
fun StateChip(
    modifier: Modifier = Modifier,
    icon: Painter,
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
            modifier = Modifier.size(20.dp),
            painter = icon,
            contentDescription = text
        )
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CheckChip(
    checked: Boolean,
    textId: Int,
    checkedTextId: Int,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit
) {
    FilterChip(
        modifier = modifier,
        selected = checked,
        colors = ChipDefaults.filterChipColors(
            contentColor = MaterialTheme.colorScheme.onBackground,
            selectedContentColor = MaterialTheme.colorScheme.primary,
            backgroundColor = MaterialTheme.colorScheme.background,
            selectedBackgroundColor = MaterialTheme.colorScheme.primaryContainer
        ),
        selectedIcon = {
            Icon(
                painterResource(id = R.drawable.ic_all),
                contentDescription = "",
                modifier = Modifier.size(24.dp)
            )
        },
        onClick = { onCheckedChange(!checked) }) {
        Row(
            Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
        ) {
            Text(text = if (checked) stringResource(id = checkedTextId) else stringResource(id = textId))
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwitchChip(
    firstTextId: Int,
    firstIconId: Int,
    secondTextId: Int,
    secondIconId: Int,
    firstSelected: Boolean = true,
    colors: SelectableChipColors = ChipDefaults.filterChipColors(
        contentColor = MaterialTheme.colorScheme.onSurface,
        selectedContentColor = MaterialTheme.colorScheme.primary,
        backgroundColor = MaterialTheme.colorScheme.surface,
        selectedBackgroundColor = MaterialTheme.colorScheme.primaryContainer
    ),
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.shapes.small.copy(CornerSize(percent = 50))
            )
            .padding(horizontal = 6.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val (firstSelected, selectFirst) = remember { mutableStateOf(firstSelected) }

        FilterChip(
            modifier = Modifier.weight(1f),
            selected = firstSelected,
            colors = colors,
            onClick = {
                onCheckedChange(true)
                selectFirst(true)
            }) {
            Icon(
                painterResource(id = firstIconId),
                contentDescription = stringResource(id = firstTextId),
                modifier = Modifier.size(24.dp)
            )
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
        FilterChip(
            modifier = Modifier.weight(1f),
            selected = !firstSelected,
            colors = colors,
            onClick = {
                onCheckedChange(false)
                selectFirst(false)
            }) {
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
            Icon(
                painterResource(id = secondIconId),
                contentDescription = stringResource(id = secondTextId),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun HorizontalExpandingVisibility(
    expanded: Boolean = false,
    expandedView: @Composable (AnimatedVisibilityScope.() -> Unit),
    collapsedView: @Composable (AnimatedVisibilityScope.() -> Unit)
) {
    AnimatedVisibility(
        visible = !expanded,
        enter = expandHorizontally(expandFrom = Alignment.Start),
        exit = shrinkHorizontally(shrinkTowards = Alignment.Start),
        content = collapsedView
    )
    AnimatedVisibility(
        visible = expanded,
        enter = expandHorizontally(expandFrom = Alignment.End),
        exit = shrinkHorizontally(shrinkTowards = Alignment.End),
        content = expandedView
    )
}

@Composable
fun PackageLabels(
    item: Package
) {
    AnimatedVisibility(visible = item.isUpdated) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.ic_updated),
            contentDescription = stringResource(id = R.string.radio_updated),
            tint = Updated
        )
    }
    AnimatedVisibility(visible = item.hasMediaData) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.ic_media_data),
            contentDescription = stringResource(id = R.string.radio_mediadata),
            tint = Media
        )
    }
    AnimatedVisibility(visible = item.hasObbData) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.ic_obb_data),
            contentDescription = stringResource(id = R.string.radio_obbdata),
            tint = OBB
        )
    }
    AnimatedVisibility(visible = item.hasExternalData) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.ic_external_data),
            contentDescription = stringResource(id = R.string.radio_externaldata),
            tint = ExtDATA
        )
    }
    AnimatedVisibility(visible = item.hasDevicesProtectedData) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.ic_de_data),
            contentDescription = stringResource(id = R.string.radio_deviceprotecteddata),
            tint = DeData
        )
    }
    AnimatedVisibility(visible = item.hasAppData) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.ic_data),
            contentDescription = stringResource(id = R.string.radio_data),
            tint = Data
        )
    }
    AnimatedVisibility(visible = item.hasApk) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.ic_apk),
            contentDescription = stringResource(id = R.string.radio_apk),
            tint = APK
        )
    }
    Icon(
        modifier = Modifier.size(24.dp),
        painter = painterResource(
            id = when {
                item.isSpecial -> R.drawable.ic_special
                item.isSystem -> R.drawable.ic_system
                else -> R.drawable.ic_user
            }
        ),
        contentDescription = stringResource(id = R.string.app_s_type_title),
        tint = when {
            item.isSpecial -> Special
            item.isSystem -> System
            else -> User
        }
    )
}

@Composable
fun BackupLabels(
    item: Backup
) {
    AnimatedVisibility(visible = item.hasMediaData) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.ic_media_data),
            contentDescription = stringResource(id = R.string.radio_mediadata),
            tint = Media
        )
    }
    AnimatedVisibility(visible = item.hasObbData) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.ic_obb_data),
            contentDescription = stringResource(id = R.string.radio_obbdata),
            tint = OBB
        )
    }
    AnimatedVisibility(visible = item.hasExternalData) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.ic_external_data),
            contentDescription = stringResource(id = R.string.radio_externaldata),
            tint = ExtDATA
        )
    }
    AnimatedVisibility(visible = item.hasDevicesProtectedData) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.ic_de_data),
            contentDescription = stringResource(id = R.string.radio_deviceprotecteddata),
            tint = DeData
        )
    }
    AnimatedVisibility(visible = item.hasAppData) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.ic_data),
            contentDescription = stringResource(id = R.string.radio_data),
            tint = Data
        )
    }
    AnimatedVisibility(visible = item.hasApk) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.ic_apk),
            contentDescription = stringResource(id = R.string.radio_apk),
            tint = APK
        )
    }
}


@Composable
fun ScheduleTypes(item: Schedule) {
    AnimatedVisibility(visible = item.mode and MODE_DATA_MEDIA == MODE_DATA_MEDIA) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.ic_media_data),
            contentDescription = stringResource(id = R.string.radio_mediadata),
            tint = Media
        )
    }
    AnimatedVisibility(visible = item.mode and MODE_DATA_OBB == MODE_DATA_OBB) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.ic_obb_data),
            contentDescription = stringResource(id = R.string.radio_obbdata),
            tint = OBB
        )
    }
    AnimatedVisibility(visible = item.mode and MODE_DATA_EXT == MODE_DATA_EXT) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.ic_external_data),
            contentDescription = stringResource(id = R.string.radio_externaldata),
            tint = ExtDATA
        )
    }
    AnimatedVisibility(visible = item.mode and MODE_DATA_DE == MODE_DATA_DE) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.ic_de_data),
            contentDescription = stringResource(id = R.string.radio_deviceprotecteddata),
            tint = DeData
        )
    }
    AnimatedVisibility(visible = item.mode and MODE_DATA == MODE_DATA) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.ic_data),
            contentDescription = stringResource(id = R.string.radio_data),
            tint = Data
        )
    }
    AnimatedVisibility(visible = item.mode and MODE_APK == MODE_APK) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.ic_apk),
            contentDescription = stringResource(id = R.string.radio_apk),
            tint = APK
        )
    }
}


@Composable
fun ScheduleFilters(
    item: Schedule
) {
    AnimatedVisibility(visible = item.filter and MAIN_FILTER_SYSTEM == MAIN_FILTER_SYSTEM) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.ic_system),
            contentDescription = stringResource(id = R.string.radio_system),
            tint = System
        )
    }
    AnimatedVisibility(visible = item.filter and MAIN_FILTER_USER == MAIN_FILTER_USER) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.ic_user),
            contentDescription = stringResource(id = R.string.radio_user),
            tint = User
        )
    }
    AnimatedVisibility(visible = item.filter and MAIN_FILTER_SPECIAL == MAIN_FILTER_SPECIAL) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.ic_special),
            contentDescription = stringResource(id = R.string.radio_special),
            tint = Special
        )
    }
    AnimatedVisibility(visible = item.specialFilter and SPECIAL_FILTER_ALL != SPECIAL_FILTER_ALL) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(
                id = when (item.specialFilter) {
                    SPECIAL_FILTER_DISABLED -> R.drawable.ic_exclude
                    SPECIAL_FILTER_LAUNCHABLE -> R.drawable.ic_launchable
                    SPECIAL_FILTER_OLD -> R.drawable.ic_old
                    else -> R.drawable.ic_updated
                }
            ),
            contentDescription = stringResource(id = R.string.app_s_type_title),
            tint = when (item.specialFilter) {
                SPECIAL_FILTER_DISABLED -> DeData
                SPECIAL_FILTER_LAUNCHABLE -> OBB
                SPECIAL_FILTER_OLD -> Exodus
                else -> Updated
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
    icon: Painter,
    iconColor: Color = MaterialTheme.colorScheme.onBackground,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        elevation = CardDefaults.cardElevation(0.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painter = icon, contentDescription = text, tint = iconColor)
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}