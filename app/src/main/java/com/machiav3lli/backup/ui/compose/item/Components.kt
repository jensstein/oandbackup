package com.machiav3lli.backup.ui.compose.item

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.compose.rememberAsyncImagePainter
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.items.AppInfo
import com.machiav3lli.backup.ui.compose.theme.*

@Composable
fun PackageIcon(
    item: AppInfo,
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
fun PackageLabels(
    item: AppInfo
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