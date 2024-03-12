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
package com.machiav3lli.backup.preferences

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.machiav3lli.backup.items.Log
import com.machiav3lli.backup.ui.compose.blockBorder
import com.machiav3lli.backup.ui.compose.item.TopBar
import com.machiav3lli.backup.ui.compose.recycler.LogRecycler
import com.machiav3lli.backup.ui.navigation.NavItem
import com.machiav3lli.backup.viewmodels.LogViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LogsPage(viewModel: LogViewModel) {

    val logs = remember(viewModel) { viewModel.logsList }

    LaunchedEffect(viewModel) {
        viewModel.refreshList()
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopBar(title = stringResource(id = NavItem.Logs.title))
        }
    ) { paddingValues ->
        LogRecycler(
            modifier = Modifier
                .padding(paddingValues)
                .blockBorder()
                .fillMaxSize(),
            productsList = logs.sortedByDescending(Log::logDate),
            onShare = { viewModel.shareLog(it, pref_shareAsFile.value) },
            onDelete = { viewModel.deleteLog(it) }
        )
    }
}
