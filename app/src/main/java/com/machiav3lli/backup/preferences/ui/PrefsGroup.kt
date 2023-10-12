package com.machiav3lli.backup.preferences.ui

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.machiav3lli.backup.BUTTON_SIZE_MEDIUM
import com.machiav3lli.backup.traceDebug
import com.machiav3lli.backup.ui.compose.item.PrefIcon
import com.machiav3lli.backup.ui.item.Pref

@Composable
fun PrefsGroupCollapsed(prefs: List<Pref>, heading: String) {
    val (expanded, expand) = remember { mutableStateOf(false) }

    if (prefs.size > 0)
        Card(
            modifier = Modifier
                .clip(CardDefaults.shape)
                .clickable { expand(!expanded) },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
            )
        ) {
            PrefsGroupHeading(heading = heading)
            AnimatedVisibility(
                visible = expanded,
                modifier = Modifier.padding(
                    start = 12.dp,
                    end = 12.dp,
                    bottom = 12.dp,
                ),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                PrefsGroup(prefs = prefs, heading = null)
            }
        }
}

@Composable
fun PrefsGroup(
    modifier: Modifier = Modifier,
    heading: String? = null,
    content: @Composable () -> Unit,
) {
    PrefsGroupHeading(heading)
    CompositionLocalProvider(
        LocalContentColor provides MaterialTheme.colorScheme.primary
    ) {
        Surface(color = Color.Transparent) {
            Column(modifier = modifier) {
                content()
            }
        }
    }
}

@Composable
fun PrefsGroup(
    modifier: Modifier = Modifier,
    heading: String? = null,
    prefs: List<Pref>,
    onPrefDialog: (Pref) -> Unit = {},
) {
    val size = prefs.size

    PrefsGroup(
        modifier = modifier,
        heading = heading
    ) {
        if (prefs.size > 0) {
            prefs.forEachIndexed { index, pref ->
                val value = remember(pref.toString()) { mutableStateOf(pref.toString()) }
                traceDebug { "${pref.key} = ${value.value}" }
                PrefsBuilder(
                    pref,
                    onPrefDialog,
                    index,
                    size,
                )
                if (index < size - 1)
                    Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun PrefsGroupHeading(
    heading: String? = null,
    modifier: Modifier = Modifier,
) =
    if (heading != null) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = modifier
                .height(BUTTON_SIZE_MEDIUM)
                .padding(horizontal = 32.dp)
                .fillMaxWidth(),
        ) {
            Text(
                text = heading,
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    } else {
        Spacer(modifier = Modifier.requiredHeight(8.dp))
    }


@Composable
fun PrefsExpandableGroupHeader(
    modifier: Modifier = Modifier,
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    icon: ImageVector,
    onClick: (() -> Unit),
) {
    Divider(thickness = 2.dp)
    Spacer(modifier = Modifier.height(8.dp))
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable { onClick() },
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PrefIcon(icon = icon, text = stringResource(id = titleId))
            Spacer(modifier = Modifier.requiredWidth(8.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(id = titleId),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
                if (summaryId != -1) {
                    Text(
                        text = stringResource(id = summaryId),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}