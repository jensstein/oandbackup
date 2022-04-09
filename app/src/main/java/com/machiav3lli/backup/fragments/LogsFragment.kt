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
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.machiav3lli.backup.R
import com.machiav3lli.backup.databinding.FragmentRecyclerBinding
import com.machiav3lli.backup.ui.compose.recycler.LogRecycler
import com.machiav3lli.backup.ui.compose.theme.AppTheme
import com.machiav3lli.backup.viewmodels.LogViewModel

class LogsFragment : Fragment() {
    private lateinit var binding: FragmentRecyclerBinding
    private lateinit var viewModel: LogViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        binding = FragmentRecyclerBinding.inflate(inflater, container, false)

        //TODO apparently the actions below take a lot of time before setContent is even called
        //TODO preset it here with a "loading..." message or may be a spinner or animation,
        //TODO because a lot of things can happen before the content exists
        //TODO do it like this for other recyclers, too, e.g. search, schedules, ...
        //TODO they all do something to retrieve data and setting up databases, LiveData etc. needs trime
        //TODO loading_list content should probably be a compose function for consistency
        //TODO note: loading_list string changed to not include "application"
        binding.recyclerView.setContent {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Text(
                    text = stringResource(id = R.string.loading_list),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        val viewModelFactory = LogViewModel.Factory(requireActivity().application)
        viewModel = ViewModelProvider(this, viewModelFactory)[LogViewModel::class.java]

        viewModel.refreshActive.observe(viewLifecycleOwner) {
            //binding.refreshLayout.isRefreshing = it
        }
        viewModel.refreshNow.observe(viewLifecycleOwner) {
            if (it) refresh()
        }
        viewModel.logsList.observe(viewLifecycleOwner) { list ->
            binding.recyclerView.setContent {
                AppTheme(
                    darkTheme = isSystemInDarkTheme()
                ) {
                    Scaffold {
                        LogRecycler(productsList = list,
                            onShare = { viewModel.shareLog(it) },
                            onDelete = { viewModel.deleteLog(it) }
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
        // binding.refreshLayout.setOnRefreshListener { viewModel.refreshList() }
    }

    fun refresh() {
        viewModel.finishRefresh()
    }
}