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
import androidx.core.view.children
import com.google.android.material.chip.ChipGroup
import com.machiav3lli.backup.PREFS_ENABLESPECIALBACKUPS
import com.machiav3lli.backup.databinding.SheetSortFilterBinding
import com.machiav3lli.backup.items.SortFilterModel
import com.machiav3lli.backup.utils.*

class SortFilterSheet(
    private var mSortFilterModel: SortFilterModel = SortFilterModel(),
    private val stats: Triple<Int, Int, Int>
) : BaseSheet() {
    private lateinit var binding: SheetSortFilterBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SheetSortFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mSortFilterModel = requireContext().sortFilterModel
        setupOnClicks()
        setupChips()
    }

    private fun setupOnClicks() {
        binding.dismiss.setOnClickListener { dismissAllowingStateLoss() }
        binding.reset.setOnClickListener {
            requireContext().sortFilterModel = SortFilterModel()
            requireContext().sortOrder = false
            requireMainActivity().refreshView()
            dismissAllowingStateLoss()
        }
        binding.apply.setOnClickListener {
            requireContext().sortFilterModel = mSortFilterModel
            requireContext().sortOrder = binding.sortAscDesc.isChecked
            requireMainActivity().refreshView()
            dismissAllowingStateLoss()
        }
        binding.appsNum.text = stats.first.toString()
        binding.backupsNum.text = stats.second.toString()
        binding.updatedNum.text = stats.third.toString()
    }

    private fun setupChips() {
        binding.sortBy.check(mSortFilterModel.sortById)
        binding.sortBy.setOnCheckedChangeListener { _: ChipGroup?, checkedId: Int ->
            mSortFilterModel.putSortBy(checkedId)
        }
        binding.sortAscDesc.isChecked = requireContext().sortOrder
        mSortFilterModel.filterIds.forEach { binding.filters.check(it) }
        binding.filters.children.forEach {
            it.setOnClickListener { view ->
                mSortFilterModel.mainFilter = mSortFilterModel.mainFilter xor idToFilter(view.id)
            }
        }
        mSortFilterModel.backupFilterIds.forEach { binding.backupFilters.check(it) }
        binding.backupFilters.children.forEach {
            it.setOnClickListener { view ->
                mSortFilterModel.backupFilter = mSortFilterModel.backupFilter xor idToMode(view.id)
            }
        }
        binding.specialFilters.check(specialFilterToId(mSortFilterModel.specialFilter))
        binding.specialFilters.setOnCheckedChangeListener { _: ChipGroup?, checkedId: Int ->
            mSortFilterModel.specialFilter = idToSpecialFilter(checkedId)
        }
        if (requireContext().getDefaultSharedPreferences()
                .getBoolean(PREFS_ENABLESPECIALBACKUPS, false)
        ) {
            binding.filterSpecial.visibility = View.VISIBLE
        } else {
            binding.filterSpecial.visibility = View.GONE
        }
    }
}