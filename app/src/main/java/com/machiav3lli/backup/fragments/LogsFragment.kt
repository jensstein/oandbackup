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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.machiav3lli.backup.R
import com.machiav3lli.backup.databinding.FragmentLogsBinding
import com.machiav3lli.backup.items.LogItemX
import com.machiav3lli.backup.viewmodels.LogViewModel
import com.machiav3lli.backup.viewmodels.LogViewModelFactory
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import com.mikepenz.fastadapter.listeners.ClickEventHook

class LogsFragment : Fragment() {
    private lateinit var binding: FragmentLogsBinding
    private val logItemAdapter = ItemAdapter<LogItemX>()
    private var logFastAdapter: FastAdapter<LogItemX>? = null
    private lateinit var viewModel: LogViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        binding = FragmentLogsBinding.inflate(inflater, container, false)

        val viewModelFactory = LogViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(LogViewModel::class.java)

        viewModel.refreshActive.observe(viewLifecycleOwner, {
            binding.refreshLayout.isRefreshing = it
        })
        viewModel.refreshNow.observe(viewLifecycleOwner, {
            if (it) refresh()
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.refreshNow.value != true) refresh()
        else viewModel.refreshList()
    }

    private fun setupViews() {
        logFastAdapter = FastAdapter.with(logItemAdapter)
        logFastAdapter?.setHasStableIds(true)
        binding.recyclerView.adapter = logFastAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        logFastAdapter?.addEventHook(OnDeleteClickHook())
        binding.refreshLayout.setOnRefreshListener { viewModel.refreshList() }
    }

    inner class OnDeleteClickHook : ClickEventHook<LogItemX>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            return viewHolder.itemView.findViewById(R.id.delete)
        }

        override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<LogItemX>, item: LogItemX) {
            viewModel.deleteLog(item.log)
        }
    }

    fun refresh() {
        val logsList = mutableListOf<LogItemX>()
        viewModel.logsList.value?.forEach { logsList.add(LogItemX(it)) }
        FastAdapterDiffUtil[logItemAdapter] = logsList
        viewModel.finishRefresh()
    }
}