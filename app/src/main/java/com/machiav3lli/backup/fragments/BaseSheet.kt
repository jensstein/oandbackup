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
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.utils.accentStyle
import com.machiav3lli.backup.utils.getAccentStyle
import com.machiav3lli.backup.utils.getSecondaryStyle
import com.machiav3lli.backup.utils.secondaryStyle

open class BaseSheet : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val sheet = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        sheet.setOnShowListener { d: DialogInterface ->
            val bottomSheetDialog = d as BottomSheetDialog
            val bottomSheet =
                bottomSheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            if (bottomSheet != null) BottomSheetBehavior.from(bottomSheet).state =
                BottomSheetBehavior.STATE_EXPANDED
        }
        sheet.context.theme.applyStyle(getAccentStyle(requireContext().accentStyle), true)
        sheet.context.theme.applyStyle(getSecondaryStyle(requireContext().secondaryStyle), true)
        return sheet
    }

    protected fun requireMainActivity(): MainActivityX = super.requireActivity() as MainActivityX
}