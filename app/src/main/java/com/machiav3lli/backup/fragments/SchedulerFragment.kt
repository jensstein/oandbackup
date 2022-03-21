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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Scaffold
import androidx.lifecycle.ViewModelProvider
import com.machiav3lli.backup.MAIN_FILTER_DEFAULT
import com.machiav3lli.backup.databinding.FragmentSchedulerBinding
import com.machiav3lli.backup.dbs.ODatabase
import com.machiav3lli.backup.dbs.entity.Schedule
import com.machiav3lli.backup.dialogs.PackagesListDialogFragment
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()

        viewModel.schedules.observe(requireActivity()) { list ->
            binding.recyclerView.setContent {
                AppTheme(
                    darkTheme = isSystemInDarkTheme()
                ) {
                    Scaffold {
                        ScheduleRecycler(productsList = list,
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
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupOnClicks()
    }

    override fun setupViews() {
    }

    override fun setupOnClicks() {
        binding.buttonBlocklist.setOnClickListener {
            Thread {
                val blocklistedPackages = requireMainActivity().viewModel.blocklist.value
                    ?.mapNotNull { it.packageName }
                    ?: listOf()

                PackagesListDialogFragment(
                    blocklistedPackages,
                    MAIN_FILTER_DEFAULT,
                    true
                ) { newList: Set<String> ->
                    requireMainActivity().viewModel.updateBlocklist(newList)
                }.show(requireActivity().supportFragmentManager, "BLOCKLIST_DIALOG")
            }.start()
        }
        binding.addSchedule.setOnClickListener {
            viewModel.addSchedule(requireContext().specialBackupsEnabled)
        }
    }

    override fun updateProgress(progress: Int, max: Int) {
        binding.progressBar.visibility = View.VISIBLE
        binding.progressBar.max = max
        binding.progressBar.progress = progress
    }

    override fun hideProgress() {
        binding.progressBar.visibility = View.GONE
    }
}