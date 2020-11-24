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
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.machiav3lli.backup.Constants.classTag
import com.machiav3lli.backup.SearchViewController
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.databinding.FragmentMainBinding
import com.machiav3lli.backup.items.MainItemX

class MainFragment : Fragment(), SearchViewController {
    private lateinit var binding: FragmentMainBinding
    private val TAG = classTag(".MainFragment")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setup() {
        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                requireMainActivity().mainItemAdapter.filter(newText)
                requireMainActivity().mainItemAdapter.itemFilter.filterPredicate = { mainItemX: MainItemX, charSequence: CharSequence? ->
                    (mainItemX.app.packageLabel.toLowerCase().contains(charSequence.toString().toLowerCase())
                            || mainItemX.app.packageName.toLowerCase().contains(charSequence.toString().toLowerCase()))
                }
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                requireMainActivity().mainItemAdapter.filter(query)
                requireMainActivity().mainItemAdapter.itemFilter.filterPredicate = { mainItemX: MainItemX, charSequence: CharSequence? ->
                    (mainItemX.app.packageLabel.toLowerCase().contains(charSequence.toString().toLowerCase())
                            || mainItemX.app.packageName.toLowerCase().contains(charSequence.toString().toLowerCase()))
                }
                return true
            }
        })
        binding.helpButton.setOnClickListener {
            if (requireMainActivity().sheetHelp == null) requireMainActivity().sheetHelp = HelpSheet()
            requireMainActivity().sheetHelp!!.showNow(requireActivity().supportFragmentManager, "HELPSHEET")
        }
    }

    override fun clean() {
        binding.searchBar.setQuery("", false)
    }

    override fun onResume() {
        super.onResume()
        requireMainActivity().setSearchViewController(this)
        requireMainActivity().onResumeFragment()
    }

    fun requireMainActivity(): MainActivityX = super.requireActivity() as MainActivityX
}