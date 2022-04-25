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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.machiav3lli.backup.databinding.FragmentComposeBinding
import com.machiav3lli.backup.items.Log
import com.machiav3lli.backup.ui.compose.recycler.LogRecycler
import com.machiav3lli.backup.ui.compose.theme.AppTheme
import com.machiav3lli.backup.viewmodels.LogViewModel

class LogsFragment : Fragment() {
    private lateinit var binding: FragmentComposeBinding
    private lateinit var viewModel: LogViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        binding = FragmentComposeBinding.inflate(inflater, container, false)

        val viewModelFactory = LogViewModel.Factory(requireActivity().application)
        viewModel = ViewModelProvider(this, viewModelFactory)[LogViewModel::class.java]

        viewModel.refreshActive.observe(viewLifecycleOwner) {
            //binding.refreshLayout.isRefreshing = it
        }
        viewModel.refreshNow.observe(viewLifecycleOwner) { if (it) refresh() }
        viewModel.logsList.observe(viewLifecycleOwner, ::redrawPage)

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
        // binding.refreshLayout.setOnRefreshListener { viewModel.refreshList() }
    }

    fun refresh() {
        viewModel.finishRefresh()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun redrawPage(list: MutableList<Log>) {
        binding.composeView.setContent {
            AppTheme(
                darkTheme = isSystemInDarkTheme()
            ) {
                Scaffold { paddingValues ->
                    LogRecycler(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize(),
                        productsList = list.sortedByDescending(Log::logDate),
                        onShare = { viewModel.shareLog(it) },
                        onDelete = { viewModel.deleteLog(it) }
                    )
                }
            }
        }
    }
}