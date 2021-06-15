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

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.machiav3lli.backup.*
import com.machiav3lli.backup.databinding.FragmentWelcomeBinding

class WelcomeFragment : Fragment() {
    private lateinit var binding: FragmentWelcomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupOnClicks()
    }

    private fun setupOnClicks() {
        binding.changelog.setOnClickListener {
            requireContext().startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(HELP_CHANGELOG)))
        }
        binding.telegram.setOnClickListener {
            requireContext().startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(HELP_TELEGRAM)))
        }
        binding.element.setOnClickListener {
            requireContext().startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(HELP_ELEMENT)))
        }
        binding.license.setOnClickListener {
            requireContext().startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(HELP_LICENSE)))
        }
        binding.issues.setOnClickListener {
            requireContext().startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(HELP_ISSUES)))
        }
        binding.faq.setOnClickListener {
            requireContext().startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(HELP_FAQ)))
        }
    }

    private fun setupViews() {
        try {
            binding.versionName.text = requireActivity().packageManager
                .getPackageInfo(requireActivity().packageName, 0)
                .versionName
        } catch (ignored: PackageManager.NameNotFoundException) {
        }
    }
}