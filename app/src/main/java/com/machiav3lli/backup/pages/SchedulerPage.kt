/*
 * OAndBackupX: open-source apps backup and restore app.
 * Copyright (C) 2020  Antonios Hazim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.machiav3lli.backup.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.R
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.dbs.entity.Schedule
import com.machiav3lli.backup.fragments.ScheduleSheet
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.CalendarPlus
import com.machiav3lli.backup.ui.compose.item.ElevatedActionButton
import com.machiav3lli.backup.ui.compose.recycler.ScheduleRecycler
import com.machiav3lli.backup.utils.specialBackupsEnabled
import com.machiav3lli.backup.viewmodels.SchedulerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulerPage(viewModel: SchedulerViewModel) {
    val context = LocalContext.current
    var sheetSchedule: ScheduleSheet? = null
    val schedules by viewModel.schedules.observeAsState(null)
    val progress by viewModel.progress.observeAsState(Pair(false, 0f))

    Scaffold(containerColor = Color.Transparent) { paddingValues ->
        Column {
            AnimatedVisibility(visible = progress?.first == true) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    progress = progress.second
                )
            }
            ScheduleRecycler(
                modifier = Modifier
                    .padding(paddingValues)
                    .weight(1f)
                    .fillMaxWidth(),
                productsList = schedules,
                onClick = { item ->
                    if (sheetSchedule != null) sheetSchedule?.dismissAllowingStateLoss()
                    sheetSchedule = ScheduleSheet(item.id)
                    sheetSchedule?.showNow(
                        (context as MainActivityX).supportFragmentManager,
                        "Schedule ${item.id}"
                    )
                },
                onCheckChanged = { item: Schedule, b: Boolean ->
                    item.enabled = b
                    viewModel.updateSchedule(item, true)
                }
            )
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 8.dp)
            ) {
                ElevatedActionButton(
                    text = stringResource(id = R.string.sched_add),
                    modifier = Modifier.fillMaxWidth(),
                    fullWidth = true,
                    icon = Phosphor.CalendarPlus
                ) {
                    viewModel.addSchedule(context.specialBackupsEnabled)
                }
            }
        }
    }
}
