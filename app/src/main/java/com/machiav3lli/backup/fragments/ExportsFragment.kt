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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.machiav3lli.backup.databinding.FragmentRecyclerBinding
import com.machiav3lli.backup.dbs.ODatabase
import com.machiav3lli.backup.ui.compose.recycler.ExportedScheduleRecycler
import com.machiav3lli.backup.ui.compose.theme.AppTheme
import com.machiav3lli.backup.viewmodels.ExportsViewModel

class ExportsFragment : Fragment() {
    private lateinit var binding: FragmentRecyclerBinding
    private lateinit var viewModel: ExportsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        binding = FragmentRecyclerBinding.inflate(inflater, container, false)

        val dataSource = ODatabase.getInstance(requireContext()).scheduleDao
        val viewModelFactory = ExportsViewModel.Factory(dataSource, requireActivity().application)
        viewModel = ViewModelProvider(this, viewModelFactory)[ExportsViewModel::class.java]

        viewModel.refreshActive.observe(viewLifecycleOwner) {
            //binding.refreshLayout.isRefreshing = it
        }
        viewModel.refreshNow.observe(viewLifecycleOwner) {
            if (it) refresh()
        }
        viewModel.exportsList.observe(viewLifecycleOwner) { list ->
            binding.recyclerView.setContent {
                AppTheme(
                    darkTheme = isSystemInDarkTheme()
                ) {
                    Scaffold {
                        ExportedScheduleRecycler(productsList = list,
                            onImport = { viewModel.importSchedule(it) },
                            onDelete = { viewModel.deleteExport(it) }
                        )
                    }
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.refreshNow.value == true) viewModel.refreshList()
    }

    private fun setupViews() {
    }

    fun refresh() {
        viewModel.finishRefresh()
    }
}