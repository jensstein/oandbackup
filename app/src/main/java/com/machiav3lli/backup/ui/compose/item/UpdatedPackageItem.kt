package com.machiav3lli.backup.ui.compose.item

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.items.Package

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatedPackageItem(
    item: Package,
    onClick: (Package) -> Unit = {},
) {
    val imageData by remember(item) {
        mutableStateOf(
            if (item.isSpecial) item.packageInfo.icon
            else "android.resource://${item.packageName}/${item.packageInfo.icon}"
        )
    }

    Card(
        modifier = Modifier,
        border = BorderStroke(0.dp, MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color.Transparent
        ),
        onClick = { onClick(item) },
    ) {
        Column(
            modifier = Modifier
                .padding(4.dp)
                .requiredWidth(64.dp)
                .clip(shape = RoundedCornerShape(8.dp)),
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