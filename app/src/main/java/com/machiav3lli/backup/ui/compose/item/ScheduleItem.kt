package com.machiav3lli.backup.ui.compose.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.machiav3lli.backup.R
import com.machiav3lli.backup.dbs.entity.Schedule
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.Play
import com.machiav3lli.backup.utils.startSchedule
import com.machiav3lli.backup.utils.timeLeft
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@Composable
fun ScheduleItem(
    schedule: Schedule,
    onClick: (Schedule) -> Unit = {},
    onCheckChanged: (Schedule, Boolean) -> Unit = { _: Schedule, _: Boolean -> },
) {
    val (checked, check) = mutableStateOf(schedule.enabled)
    val (absTime, relTime) = timeLeft(schedule, CoroutineScope(Dispatchers.Default))
        .collectAsState().value

    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable { onClick(schedule) },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
        ),
        leadingContent = {
            Checkbox(checked = checked,
                onCheckedChange = {
                    check(it)
                    onCheckChanged(schedule, it)
                }
            )
        },
        headlineContent = {
            Row {
                Text(
                    text = schedule.name,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(1f),
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium
                )
                ScheduleFilters(item = schedule)
            }
        },
        supportingContent = {
            Row {
                Text(
                    text = if (schedule.enabled)
                        "🕒 $absTime\n⏳ $relTime"    // TODO replace by resource icons
                    else
                        "🕒 $absTime",
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(1f),
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    style = MaterialTheme.typography.bodySmall
                )
                ScheduleTypes(item = schedule)
            }
        },
        trailingContent = {
            IconButton(onClick = {
                startSchedule(schedule)
            }) {
                Icon(
                    imageVector = Phosphor.Play,
                    contentDescription = stringResource(id = R.string.sched_startingbackup)
                )
            }
        }
    )
}
