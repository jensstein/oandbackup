package com.machiav3lli.backup.ui.compose.item

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import com.machiav3lli.backup.items.AppInfo
import com.machiav3lli.backup.ui.compose.theme.LocalShapes

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoilApi::class)
@Composable
fun UpdatedPackageItem(
    item: AppInfo,
    onClick: (AppInfo) -> Unit = {}
) {
    val imageData by remember(item) {
        mutableStateOf(
            if (item.isSpecial) item.appMetaInfo.icon
            else "android.resource://${item.packageName}/${item.appMetaInfo.icon}"
        )
    }

    Card(
        modifier = Modifier,
        border = BorderStroke(0.dp, MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(LocalShapes.current.medium),
        containerColor = MaterialTheme.colorScheme.surface,
        onClick = { onClick(item) },
    ) {
        Column(
            modifier = Modifier
                .padding(4.dp)
                .requiredWidth(64.dp)
                .clip(shape = RoundedCornerShape(8.dp))
                .background(color = MaterialTheme.colorScheme.surface),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            PackageIcon(item = item, imageData = imageData)

            Text(
                text = item.packageLabel,
                modifier = Modifier.fillMaxWidth(),
                softWrap = true,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}