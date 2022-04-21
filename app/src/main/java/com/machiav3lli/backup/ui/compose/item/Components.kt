package com.machiav3lli.backup.ui.compose.item

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.compose.rememberAsyncImagePainter
import com.machiav3lli.backup.MAIN_FILTER_SPECIAL
import com.machiav3lli.backup.MAIN_FILTER_SYSTEM
import com.machiav3lli.backup.MAIN_FILTER_USER
import com.machiav3lli.backup.MODE_APK
import com.machiav3lli.backup.MODE_DATA
import com.machiav3lli.backup.MODE_DATA_DE
import com.machiav3lli.backup.MODE_DATA_EXT
import com.machiav3lli.backup.MODE_DATA_MEDIA
import com.machiav3lli.backup.MODE_DATA_OBB
import com.machiav3lli.backup.OABX
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
    SubcomposeAsyncImage(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(LocalShapes.current.medium)),
        model = imageData,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        error = {
            SubcomposeAsyncImageContent(
                painter = rememberAsyncImagePainter(
                    ContextCompat.getDrawable(
                        OABX.context,
                        when {
                            item.isSpecial -> R.drawable.ic_placeholder_special
                            item.isSystem -> R.drawable.ic_placeholder_system
                            else -> R.drawable.ic_placeholder_user
                        }
                    )
                )
            )
        }
    )
}

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
                modifier = Modifier.size(18.dp),
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
            contentColor = if (positive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
        ),
        onClick = onClick
    ) {
        if (icon != null) {
            Icon(
                modifier = Modifier.size(18.dp),
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
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ActionChip(
    modifier: Modifier = Modifier,
    icon: Painter,
    text: String,
    withText: Boolean = true,
    positive: Boolean = true,
    fullWidth: Boolean = false,
    onClick: () -> Unit
) {
    Chip(
        modifier = modifier,
        colors = ChipDefaults.chipColors(
            backgroundColor = if (positive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
            contentColor = if (positive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
        ),
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

@Composable
fun HorizontalExpandingVisibility(
    expanded: Boolean = false,
    expandedView: @Composable (AnimatedVisibilityScope.() -> Unit),
    collapsedView: @Composable (AnimatedVisibilityScope.() -> Unit)
) {
    AnimatedVisibility(
        visible = expanded,
        enter = expandHorizontally(expandFrom = Alignment.Start),
        exit = shrinkHorizontally(shrinkTowards = Alignment.Start),
        content = expandedView
    )
    AnimatedVisibility(
        visible = !expanded,
        enter = expandHorizontally(expandFrom = Alignment.End),
        exit = shrinkHorizontally(shrinkTowards = Alignment.End),
        content = collapsedView
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