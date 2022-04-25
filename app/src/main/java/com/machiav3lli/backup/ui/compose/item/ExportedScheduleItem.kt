package com.machiav3lli.backup.ui.compose.item

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.R
import com.machiav3lli.backup.dbs.entity.Schedule
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.ui.compose.theme.LocalShapes
import java.time.LocalTime


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportedScheduleItem(
    item: Schedule,
    file: StorageFile,
    onRestore: (Schedule) -> Unit = { },
    onDelete: (StorageFile) -> Unit = { },
) {
    OutlinedCard(
        modifier = Modifier,
        shape = RoundedCornerShape(LocalShapes.current.medium),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface),
        containerColor = MaterialTheme.colorScheme.background,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .wrapContentHeight()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
            ) {
                Text(
                    text = item.name,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(1f),
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium
                )
                ScheduleFilters(item = item)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
            ) {
                var setTime = LocalTime.of(item.timeHour, item.timeMinute).toString()
                setTime += " ${
                    LocalContext.current.resources
                        .getQuantityString(
                            R.plurals.sched_interval_formal,
                            item.interval,
                            item.interval
                        )
                }"
                Text(
                    text = setTime,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(1f),
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.weight(1f))
                ScheduleTypes(item = item)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionChip(
                    icon = painterResource(id = R.drawable.ic_restore),
                    text = stringResource(id = R.string.dialog_import),
                    positive = true,
                    onClick = { onRestore(item) }
                )

                Spacer(modifier = Modifier.weight(1f))

                ActionChip(
                    icon = painterResource(id = R.drawable.ic_delete),
                    text = stringResource(id = R.string.delete),
                    withText = false,
                    positive = false,
                    onClick = { onDelete(file) }
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewExportedScheduleItem() {
    //ExportedScheduleItem(Schedule())
}