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

import androidx.fragment.app.Fragment
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.dbs.AppExtras
import com.machiav3lli.backup.items.AppInfo

abstract class NavigationFragment : Fragment() {
    protected var sheetSortFilter: SortFilterSheet? = null
    var appInfoList: MutableList<AppInfo>
        get() = requireMainActivity().viewModel.appInfoList.value
            ?: mutableListOf()
        set(value) {
            requireMainActivity().viewModel.appInfoList.value = value
        }
    var appExtrasList: MutableList<AppExtras>
        get() = requireMainActivity().viewModel.appExtrasList
        set(value) {
            requireMainActivity().viewModel.appExtrasList = value
        }

    abstract fun setupViews()
    abstract fun setupOnClicks()

    fun requireMainActivity(): MainActivityX = super.requireActivity() as MainActivityX
}

interface RefreshViewController {
    fun refreshView()
}