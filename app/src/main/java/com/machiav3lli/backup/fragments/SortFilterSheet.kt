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

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.ChipGroup
import com.machiav3lli.backup.PREFS_ENABLESPECIALBACKUPS
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.databinding.SheetSortFilterBinding
import com.machiav3lli.backup.items.SortFilterModel
import com.machiav3lli.backup.utils.getDefaultSharedPreferences
import com.machiav3lli.backup.utils.sortFilterModel
import com.machiav3lli.backup.utils.sortOrder

class SortFilterSheet(private var mSortFilterModel: SortFilterModel = SortFilterModel(), private val stats: Triple<Int, Int, Int>) : BottomSheetDialogFragment() {
    private lateinit var binding: SheetSortFilterBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val sheet = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        sheet.setOnShowListener { d: DialogInterface ->
            val bottomSheetDialog = d as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            if (bottomSheet != null) BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
        }
        return sheet
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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
            requireContext().sortFilterModel = SortFilterModel("0000")
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
        binding.sortBy.setOnCheckedChangeListener { _: ChipGroup?, checkedId: Int -> mSortFilterModel.putSortBy(checkedId) }
        binding.sortAscDesc.isChecked = requireContext().sortOrder
        binding.filters.check(mSortFilterModel.filterId)
        binding.filters.setOnCheckedChangeListener { _: ChipGroup?, checkedId: Int -> mSortFilterModel.putFilter(checkedId) }
        binding.backupFilters.check(mSortFilterModel.backupFilterId)
        binding.backupFilters.setOnCheckedChangeListener { _: ChipGroup?, checkedId: Int -> mSortFilterModel.putBackupFilter(checkedId) }
        binding.specialFilters.check(mSortFilterModel.specialFilterId)
        binding.specialFilters.setOnCheckedChangeListener { _: ChipGroup?, checkedId: Int -> mSortFilterModel.putSpecialFilter(checkedId) }
        if (requireContext().getDefaultSharedPreferences().getBoolean(PREFS_ENABLESPECIALBACKUPS, false)) {
            binding.showOnlySpecial.visibility = View.VISIBLE
        } else {
            binding.showOnlySpecial.visibility = View.GONE
        }
    }

    private fun requireMainActivity(): MainActivityX = super.requireActivity() as MainActivityX
}