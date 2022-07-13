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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.machiav3lli.backup.dbs.ODatabase
import com.machiav3lli.backup.ui.compose.recycler.ExportedScheduleRecycler
import com.machiav3lli.backup.ui.compose.theme.AppTheme
import com.machiav3lli.backup.viewmodels.ExportsViewModel

class ExportsFragment : Fragment() {
    private lateinit var viewModel: ExportsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        val dataSource = ODatabase.getInstance(requireContext()).scheduleDao
        val viewModelFactory = ExportsViewModel.Factory(dataSource, requireActivity().application)
        viewModel = ViewModelProvider(this, viewModelFactory)[ExportsViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setContent { ExportsPage() }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshList()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ExportsPage() {
        val exports by viewModel.exportsList.observeAsState()

        AppTheme(
            darkTheme = isSystemInDarkTheme()
        ) {
            Scaffold { paddingValues ->
                ExportedScheduleRecycler(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    productsList = exports,
                    onImport = { viewModel.importSchedule(it) },
                    onDelete = { viewModel.deleteExport(it) }
                )
            }
        }
    }
}