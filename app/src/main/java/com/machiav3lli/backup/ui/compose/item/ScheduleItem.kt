package com.machiav3lli.backup.ui.compose.item

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.R
import com.machiav3lli.backup.dbs.entity.Schedule
import com.machiav3lli.backup.ui.compose.theme.LocalShapes
import com.machiav3lli.backup.utils.calculateTimeToRun
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleItem(
    item: Schedule,
    onClick: (Schedule) -> Unit = {},
    onCheckChanged: (Schedule, Boolean) -> Unit = { _: Schedule, _: Boolean -> }
) {
    val schedule by remember(item) { mutableStateOf(item) }
    val (checked, check) = mutableStateOf(item.enabled)

    OutlinedCard(
        modifier = Modifier,
        shape = RoundedCornerShape(LocalShapes.current.medium),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        elevation = CardDefaults.cardElevation(4.dp),
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
                    // schedule.enabled = it
                    check(it)
                    onCheckChanged(schedule, it)
                }
            )

            Column(
                modifier = Modifier.wrapContentHeight()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.4f),
                ) {
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.4f),
                ) {
                    AnimatedVisibility(visible = schedule.enabled) {
                        val now = System.currentTimeMillis()
                        val timeDiff = abs(calculateTimeToRun(schedule, now) - now)
                        val days = TimeUnit.MILLISECONDS.toDays(timeDiff).toInt()
                        var timeLeft = stringResource(id = R.string.sched_timeLeft)
                        if (days != 0) {
                            timeLeft += " ${
                                LocalContext.current.resources
                                    .getQuantityString(R.plurals.days_left, days, days)
                            }"
                        }
                        val hours = TimeUnit.MILLISECONDS.toHours(timeDiff).toInt() % 24
                        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff).toInt() % 60
                        timeLeft += " ${LocalTime.of(hours, minutes)}"
                        Text(
                            text = timeLeft,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .weight(1f),
                            softWrap = true,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    ScheduleTypes(item = schedule)
                }
            }
        }
    }
}