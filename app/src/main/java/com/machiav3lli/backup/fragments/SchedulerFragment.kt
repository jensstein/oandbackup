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
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.machiav3lli.backup.MAIN_FILTER_DEFAULT
import com.machiav3lli.backup.R
import com.machiav3lli.backup.databinding.FragmentSchedulerBinding
import com.machiav3lli.backup.dbs.ScheduleDatabase
import com.machiav3lli.backup.dialogs.PackagesListDialogFragment
import com.machiav3lli.backup.items.SchedulerItemX
import com.machiav3lli.backup.viewmodels.SchedulerViewModel
import com.machiav3lli.backup.viewmodels.SchedulerViewModelFactory
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil.calculateDiff
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil.set
import com.mikepenz.fastadapter.listeners.ClickEventHook

class SchedulerFragment : NavigationFragment() {
    private lateinit var binding: FragmentSchedulerBinding
    private var sheetSchedule: ScheduleSheet? = null
    private lateinit var viewModel: SchedulerViewModel

    private val schedulerItemAdapter = ItemAdapter<SchedulerItemX>()
    private var schedulerFastAdapter: FastAdapter<SchedulerItemX> =
        FastAdapter.with(schedulerItemAdapter)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        binding = FragmentSchedulerBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        val dataSource = ScheduleDatabase.getInstance(requireContext()).scheduleDao
        val viewModelFactory = SchedulerViewModelFactory(dataSource, requireActivity().application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(SchedulerViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()

        viewModel.schedules.observe(requireActivity(), {
            it?.let {
                val diffResult = calculateDiff(
                    schedulerItemAdapter,
                    viewModel.schedulesItems,
                    SchedulerItemX.Companion.SchedulerDiffCallback()
                )
                set(schedulerItemAdapter, diffResult)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        setupOnClicks()
    }

    override fun setupViews() {
        schedulerFastAdapter.setHasStableIds(false)
        binding.recyclerView.adapter = schedulerFastAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

    }

    override fun setupOnClicks() {
        schedulerFastAdapter.onClickListener =
            { _: View?, _: IAdapter<SchedulerItemX>, item: SchedulerItemX?, _: Int? ->
                sheetSchedule?.dismissAllowingStateLoss()
                item?.let {
                    sheetSchedule = ScheduleSheet(it.schedule.id)
                    sheetSchedule?.showNow(
                        requireActivity().supportFragmentManager,
                        "SCHEDULESHEET"
                    )
                }
                false
            }
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
        binding.helpButton.setOnClickListener {
            if (requireMainActivity().sheetHelp == null) requireMainActivity().sheetHelp =
                HelpSheet()
            requireMainActivity().sheetHelp!!.showNow(
                requireActivity().supportFragmentManager,
                "HELPSHEET"
            )
        }
        binding.addSchedule.setOnClickListener {
            viewModel.addSchedule()
        }
        schedulerFastAdapter.addEventHook(OnEnableClickHook())
    }

    inner class OnEnableClickHook : ClickEventHook<SchedulerItemX>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            return viewHolder.itemView.findViewById(R.id.enableCheckbox)
        }

        override fun onClick(
            v: View,
            position: Int,
            fastAdapter: FastAdapter<SchedulerItemX>,
            item: SchedulerItemX
        ) {
            item.schedule.enabled = (v as AppCompatCheckBox).isChecked
            Thread(ScheduleSheet.UpdateRunnable(item.schedule, requireContext(), true)).start()
        }
    }
}