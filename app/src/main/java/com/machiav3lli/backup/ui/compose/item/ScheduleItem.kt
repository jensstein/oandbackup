package com.machiav3lli.backup.ui.compose.item

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.R
import com.machiav3lli.backup.dbs.entity.Schedule
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.PlayCircle
import com.machiav3lli.backup.ui.compose.theme.LocalShapes
import com.machiav3lli.backup.utils.getTimeLeft
import com.machiav3lli.backup.utils.startSchedule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleItem(
    item: Schedule,
    onClick: (Schedule) -> Unit = {},
    onCheckChanged: (Schedule, Boolean) -> Unit = { _: Schedule, _: Boolean -> }
) {
    val schedule by remember(item) { mutableStateOf(item) }     //TODO hg42 remove remember ???
    val (checked, check) = mutableStateOf(item.enabled)
    val context = LocalContext.current

    Card(
        modifier = Modifier,
        shape = RoundedCornerShape(LocalShapes.current.medium),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        onClick = { onClick(schedule) },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = checked,
                onCheckedChange = {
                    check(it)
                    onCheckChanged(schedule, it)
                }
            )

            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .weight(1f)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
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
                Row(modifier = Modifier.fillMaxWidth()) {
                    AnimatedVisibility(visible = schedule.enabled) {
                        val (absTime, relTime) = getTimeLeft(context, schedule)
                        Text(
                            text = "    🕒 $absTime\n    ⏳ $relTime",
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .weight(1f),
                            softWrap = true,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 2,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    ScheduleTypes(item = schedule)
                }
            }

            IconButton(onClick = {
                startSchedule(schedule)
            }) {
                Icon(
                    imageVector = Phosphor.PlayCircle,
                    contentDescription = stringResource(id = R.string.sched_startingbackup)
                )
            }
        }
    }
}
