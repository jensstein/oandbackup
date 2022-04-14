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
package com.machiav3lli.backup.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.machiav3lli.backup.MAIN_FILTER_DEFAULT
import com.machiav3lli.backup.R
import com.machiav3lli.backup.databinding.FragmentSchedulerBinding
import com.machiav3lli.backup.dbs.ODatabase
import com.machiav3lli.backup.dbs.entity.Schedule
import com.machiav3lli.backup.dialogs.PackagesListDialogFragment
import com.machiav3lli.backup.ui.compose.item.ElevatedActionButton
import com.machiav3lli.backup.ui.compose.recycler.ScheduleRecycler
import com.machiav3lli.backup.ui.compose.theme.AppTheme
import com.machiav3lli.backup.utils.specialBackupsEnabled
import com.machiav3lli.backup.viewmodels.SchedulerViewModel

class SchedulerFragment : NavigationFragment() {
    private lateinit var binding: FragmentSchedulerBinding
    private var sheetSchedule: ScheduleSheet? = null
    private lateinit var viewModel: SchedulerViewModel
    private lateinit var database: ODatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        binding = FragmentSchedulerBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        database = ODatabase.getInstance(requireContext())
        val dataSource = database.scheduleDao
        val viewModelFactory = SchedulerViewModel.Factory(dataSource, requireActivity().application)
        viewModel = ViewModelProvider(this, viewModelFactory)[SchedulerViewModel::class.java]
        return binding.root
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.schedules.observe(requireActivity()) { list ->
            binding.recyclerView.setContent {
                AppTheme(
                    darkTheme = isSystemInDarkTheme()
                ) {
                    Scaffold {
                        Column {
                            ScheduleRecycler(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                productsList = list,
                                onClick = { item ->
                                    if (sheetSchedule != null) sheetSchedule?.dismissAllowingStateLoss()
                                    sheetSchedule = ScheduleSheet(item.id)
                                    sheetSchedule?.showNow(
                                        requireActivity().supportFragmentManager,
                                        "Schedule ${item.id}"
                                    )
                                },
                                onCheckChanged = { item: Schedule, b: Boolean ->
                                    item.enabled = b
                                    Thread(
                                        ScheduleSheet.UpdateRunnable(
                                            item,
                                            requireContext(),
                                            database.scheduleDao,
                                            true
                                        )
                                    ).start()
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
                                    icon = painterResource(id = R.drawable.ic_add_sched)
                                ) {
                                    viewModel.addSchedule(requireContext().specialBackupsEnabled)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun updateProgress(progress: Int, max: Int) {
        //binding.progressBar.visibility = View.VISIBLE
        //binding.progressBar.max = max
        //binding.progressBar.progress = progress
    }

    override fun hideProgress() {
        //binding.progressBar.visibility = View.GONE
    }
}