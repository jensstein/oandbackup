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
package com.machiav3lli.backup.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.machiav3lli.backup.BLACKLIST_ARGS_ID
import com.machiav3lli.backup.BLACKLIST_ARGS_PACKAGES
import com.machiav3lli.backup.R
import com.machiav3lli.backup.databinding.ActivitySchedulerXBinding
import com.machiav3lli.backup.dbs.*
import com.machiav3lli.backup.dialogs.BlacklistDialogFragment
import com.machiav3lli.backup.fragments.HelpSheet
import com.machiav3lli.backup.fragments.ScheduleSheet
import com.machiav3lli.backup.handler.ScheduleJobsHandler
import com.machiav3lli.backup.items.SchedulerItemX
import com.machiav3lli.backup.viewmodels.SchedulerViewModel
import com.machiav3lli.backup.viewmodels.SchedulerViewModelFactory
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil.calculateDiff
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil.set
import com.mikepenz.fastadapter.listeners.ClickEventHook

class SchedulerActivityX : BaseActivity(), BlacklistDialogFragment.BlacklistListener {
    private var sheetSchedule: ScheduleSheet? = null
    private val schedulerItemAdapter = ItemAdapter<SchedulerItemX>()
    private var schedulerFastAdapter: FastAdapter<SchedulerItemX> = FastAdapter.with(schedulerItemAdapter)
    private lateinit var viewModel: SchedulerViewModel
    private lateinit var binding: ActivitySchedulerXBinding
    private lateinit var blacklistDao: BlacklistDao
    private var sheetHelp: HelpSheet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySchedulerXBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        blacklistDao = BlacklistDatabase.getInstance(this).blacklistDao
        val dataSource = ScheduleDatabase.getInstance(this).scheduleDao
        val viewModelFactory = SchedulerViewModelFactory(dataSource, application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(SchedulerViewModel::class.java)

        viewModel.schedules.observe(this, {
            it?.let {
                val diffResult = calculateDiff(schedulerItemAdapter, viewModel.schedulesItems, SchedulerItemX.Companion.SchedulerDiffCallback())
                set(schedulerItemAdapter, diffResult)
            }
        })

        schedulerFastAdapter.setHasStableIds(false)
        binding.recyclerView.adapter = schedulerFastAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        FastAdapterDiffUtil[schedulerItemAdapter] = viewModel.schedulesItems
        setupOnClicks()

        setContentView(binding.root)
    }

    private fun setupOnClicks() {
        binding.backButton.setOnClickListener { finish() }
        schedulerFastAdapter.onClickListener = { _: View?, _: IAdapter<SchedulerItemX>, item: SchedulerItemX?, _: Int? ->
            sheetSchedule?.dismissAllowingStateLoss()
            item?.let {
                sheetSchedule = ScheduleSheet(it.schedule.id)
                sheetSchedule?.showNow(supportFragmentManager, "SCHEDULESHEET")
            }
            false
        }
        binding.blacklistButton.setOnClickListener {
            Thread {
                val args = Bundle()
                args.putLong(BLACKLIST_ARGS_ID, GLOBAL_ID)

                val blacklistedPackages = blacklistDao
                        .getBlacklistedPackages(GLOBAL_ID) as ArrayList<String>
                args.putStringArrayList(BLACKLIST_ARGS_PACKAGES,
                        blacklistedPackages)
                val blacklistDialogFragment = BlacklistDialogFragment(this)
                blacklistDialogFragment.arguments = args
                blacklistDialogFragment.show(supportFragmentManager, "BLACKLIST_DIALOG")
            }.start()
        }
        binding.addSchedule.setOnClickListener {
            viewModel.addSchedule()
        }
        binding.helpButton.setOnClickListener {
            if (sheetHelp == null) sheetHelp = HelpSheet()
            sheetHelp?.showNow(this@SchedulerActivityX.supportFragmentManager, "SCHEDULESHEET")
        }
        schedulerFastAdapter.addEventHook(OnDeleteClickHook())
        schedulerFastAdapter.addEventHook(OnEnableClickHook())
    }

    override fun onBlacklistChanged(newList: Set<String>, blacklistId: Long) {
        Thread {
            blacklistDao.updateList(blacklistId, newList)
        }.start()
    }

    inner class OnDeleteClickHook : ClickEventHook<SchedulerItemX>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            return viewHolder.itemView.findViewById(R.id.delete)
        }

        override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<SchedulerItemX>, item: SchedulerItemX) {
            viewModel.removeSchedule(item.schedule.id)
            ScheduleJobsHandler.cancelJob(this@SchedulerActivityX, item.schedule.id.toInt())
        }
    }

    inner class OnEnableClickHook : ClickEventHook<SchedulerItemX>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            return viewHolder.itemView.findViewById(R.id.enableCheckbox)
        }

        override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<SchedulerItemX>, item: SchedulerItemX) {
            item.schedule.enabled = (v as AppCompatCheckBox).isChecked
            Thread(ScheduleSheet.UpdateRunnable(item.schedule, this@SchedulerActivityX, true)).start()
        }
    }

    companion object {
        const val GLOBAL_ID = -1L
    }
}