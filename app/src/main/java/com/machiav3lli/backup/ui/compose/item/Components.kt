package com.machiav3lli.backup.ui.compose.item

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.machiav3lli.backup.*
import com.machiav3lli.backup.R
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.dbs.entity.Schedule
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.ui.compose.theme.*

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
    }
}

@Composable
fun ActionChip(
    icon: Painter,
    text: String,
    withText: Boolean = true,
    positive: Boolean = true,
    onClick: () -> Unit
) {
    FilledTonalButton(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (positive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
            contentColor = if (positive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
        ),
        onClick = onClick,
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            painter = icon,
            contentDescription = text
        )
        if (withText) Text(
            modifier = Modifier.padding(start = 4.dp),
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall
        )
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