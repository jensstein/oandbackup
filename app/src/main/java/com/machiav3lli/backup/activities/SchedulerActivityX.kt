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

import android.content.ContentValues
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.machiav3lli.backup.BlacklistListener
import com.machiav3lli.backup.Constants
import com.machiav3lli.backup.Constants.classTag
import com.machiav3lli.backup.R
import com.machiav3lli.backup.databinding.ActivitySchedulerXBinding
import com.machiav3lli.backup.dbs.ScheduleDatabase
import com.machiav3lli.backup.dialogs.BlacklistDialogFragment
import com.machiav3lli.backup.fragments.HelpSheet
import com.machiav3lli.backup.fragments.ScheduleSheet
import com.machiav3lli.backup.items.SchedulerItemX
import com.machiav3lli.backup.schedules.BlacklistContract
import com.machiav3lli.backup.schedules.BlacklistsDBHelper
import com.machiav3lli.backup.schedules.HandleAlarms
import com.machiav3lli.backup.viewmodels.SchedulerViewModel
import com.machiav3lli.backup.viewmodels.SchedulerViewModelFactory
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil.calculateDiff
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil.set
import com.mikepenz.fastadapter.listeners.ClickEventHook

class SchedulerActivityX : BaseActivity(), BlacklistListener {
    lateinit var handleAlarms: HandleAlarms
    private var sheetSchedule: ScheduleSheet? = null
    private val schedulerItemAdapter = ItemAdapter<SchedulerItemX>()
    private var schedulerFastAdapter: FastAdapter<SchedulerItemX> = FastAdapter.with(schedulerItemAdapter)
    private lateinit var viewModel: SchedulerViewModel
    private lateinit var binding: ActivitySchedulerXBinding
    private lateinit var blacklistsDBHelper: BlacklistsDBHelper
    private var sheetHelp: HelpSheet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySchedulerXBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        handleAlarms = HandleAlarms(this)
        blacklistsDBHelper = BlacklistsDBHelper(this)
        val dataSource = ScheduleDatabase.getInstance(this, DATABASE_NAME).scheduleDao
        val viewModelFactory = SchedulerViewModelFactory(dataSource, application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(SchedulerViewModel::class.java)

        viewModel.schedules.observe(this, {
            it?.let {
                val diffResult = calculateDiff(schedulerItemAdapter, viewModel.schedulesItems, SchedulerItemX.Companion.SchedulerDiffCallback())
                set(schedulerItemAdapter, diffResult)
            }
        })

        viewModel.activeSchedule.observe(this, {
            val diffResult = calculateDiff(schedulerItemAdapter, viewModel.schedulesItems)
            set(schedulerItemAdapter, diffResult)
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
                viewModel.setActiveSchedule(it.schedule.id)
                sheetSchedule?.showNow(supportFragmentManager, "SCHEDULESHEET")
            }
            false
        }
        binding.blacklistButton.setOnClickListener {
            Thread {
                val args = Bundle()
                args.putInt(Constants.BLACKLIST_ARGS_ID, GLOBALBLACKLISTID)
                val db = blacklistsDBHelper.readableDatabase
                val blacklistedPackages = blacklistsDBHelper
                        .getBlacklistedPackages(db, GLOBALBLACKLISTID) as ArrayList<String>
                args.putStringArrayList(Constants.BLACKLIST_ARGS_PACKAGES,
                        blacklistedPackages)
                val blacklistDialogFragment = BlacklistDialogFragment()
                blacklistDialogFragment.arguments = args
                blacklistDialogFragment.addBlacklistListener(this)
                blacklistDialogFragment.show(supportFragmentManager, "blacklistDialog")
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

    override fun onPause() {
        super.onPause()
        if (sheetSchedule != null) sheetSchedule?.dismissAllowingStateLoss()
    }

    public override fun onDestroy() {
        blacklistsDBHelper.close()
        super.onDestroy()
    }

    override fun onBlacklistChanged(blacklist: Array<CharSequence>, id: Int) {
        Thread {
            val db = blacklistsDBHelper.writableDatabase
            blacklistsDBHelper.deleteBlacklistFromId(db, id)
            for (packageName in blacklist) {
                val values = ContentValues()
                values.put(BlacklistContract.BlacklistEntry.COLUMN_PACKAGENAME, packageName as String)
                values.put(BlacklistContract.BlacklistEntry.COLUMN_BLACKLISTID, id.toString())
                db.insert(BlacklistContract.BlacklistEntry.TABLE_NAME, null, values)
            }
        }.start()
    }

    inner class OnDeleteClickHook : ClickEventHook<SchedulerItemX>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            return viewHolder.itemView.findViewById(R.id.delete)
        }

        override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<SchedulerItemX>, item: SchedulerItemX) {
            viewModel.removeSchedule(item.schedule.id)
            handleAlarms.cancelAlarm(item.schedule.id.toInt())
        }
    }

    inner class OnEnableClickHook : ClickEventHook<SchedulerItemX>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            return viewHolder.itemView.findViewById(R.id.enableCheckbox)
        }

        override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<SchedulerItemX>, item: SchedulerItemX) {
            viewModel.getActiveSchedule(item.schedule.id)?.enabled = (v as AppCompatCheckBox).isChecked
            if (!item.schedule.enabled) {
                handleAlarms.cancelAlarm(item.schedule.id.toInt())
            }
            refresh()
        }
    }

    private fun refresh() {
        Thread(ScheduleSheet.UpdateRunnable(viewModel.activeSchedule.value, BlacklistsDBHelper.DATABASE_NAME, this@SchedulerActivityX)).start()
    }

    companion object {
        val TAG = classTag(".SchedulerActivityX")
        const val GLOBALBLACKLISTID = -1
        const val DATABASE_NAME = "schedules.db"
    }
}