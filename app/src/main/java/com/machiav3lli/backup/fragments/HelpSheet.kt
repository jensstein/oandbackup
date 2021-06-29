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
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.text.HtmlCompat
import com.machiav3lli.backup.*
import com.machiav3lli.backup.databinding.SheetHelpBinding
import java.io.IOException
import java.io.InputStream
import java.util.*

class HelpSheet : BaseSheet() {
    private lateinit var binding: SheetHelpBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SheetHelpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupOnClicks()
        setupViews()
    }

    private fun setupOnClicks() {
        binding.dismiss.setOnClickListener { dismissAllowingStateLoss() }
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
        binding.usageNotesTitle.setOnClickListener {
            usageNotesExtendShrink(binding.usageNotesHtml.visibility == View.VISIBLE)
        }
        binding.extendShrink.setOnClickListener {
            usageNotesExtendShrink(binding.usageNotesHtml.visibility == View.VISIBLE)
        }
    }

    private fun setupViews() {
        try {
            binding.helpVersionName.text = requireActivity().packageManager.getPackageInfo(
                requireActivity().packageName,
                0
            ).versionName
            val stream = resources.openRawResource(R.raw.help)
            val htmlString = convertStreamToString(stream)
            stream.close()
            binding.usageNotesHtml.text =
                HtmlCompat.fromHtml(htmlString, HtmlCompat.FROM_HTML_MODE_LEGACY)
            binding.usageNotesHtml.movementMethod = LinkMovementMethod.getInstance()
        } catch (e: IOException) {
            binding.usageNotesHtml.text = e.toString()
        } catch (ignored: PackageManager.NameNotFoundException) {
        }
    }

    private fun usageNotesExtendShrink(extended: Boolean) {
        val rotate = AnimationUtils.loadAnimation(context, R.anim.anim_rotate)
        if (extended) {
            binding.usageNotesHtml.visibility = View.GONE
            binding.extendShrink.startAnimation(rotate)
            binding.extendShrink.rotation = 0.0F
        } else {
            binding.usageNotesHtml.visibility = View.VISIBLE
            binding.extendShrink.startAnimation(rotate)
            binding.extendShrink.rotation = 180.0F
        }
    }

    companion object {
        fun convertStreamToString(stream: InputStream?): String {
            val s = Scanner(stream, "utf-8").useDelimiter("\\A")
            return if (s.hasNext()) s.next() else ""
        }
    }
}