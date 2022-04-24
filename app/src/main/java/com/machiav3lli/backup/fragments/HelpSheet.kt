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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import com.machiav3lli.backup.R
import com.machiav3lli.backup.legendList
import com.machiav3lli.backup.linksList
import com.machiav3lli.backup.ui.compose.item.LegendItem
import com.machiav3lli.backup.ui.compose.item.LinkItem
import com.machiav3lli.backup.ui.compose.item.RoundButton
import com.machiav3lli.backup.ui.compose.item.TitleText
import com.machiav3lli.backup.ui.compose.theme.AppTheme
import com.machiav3lli.backup.utils.gridItems
import java.io.IOException
import java.io.InputStream
import java.util.*

class HelpSheet : BaseSheet() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        return ComposeView(requireContext()).apply {
            setContent { HelpPage() }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun HelpPage() {
        AppTheme(
            darkTheme = isSystemInDarkTheme()
        ) {
            Scaffold(
                topBar = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        RoundButton(icon = painterResource(id = R.drawable.ic_arrow_down)) {
                            dismissAllowingStateLoss()
                        }
                    }
                }
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = MaterialTheme.colorScheme.background,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface)
                        ) {
                            linksList.forEach {
                                LinkItem(
                                    item = it,
                                    onClick = { uriString ->
                                        requireContext().startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse(uriString)
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                    item { TitleText(R.string.help_legend) }
                    gridItems(
                        items = legendList,
                        columns = 2,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LegendItem(item = it)
                    }
                    item {
                        Text(
                            text = stringResource(id = R.string.help_appTypeHint),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    item {
                        val (showNotes, extendNotes) = remember { mutableStateOf(false) }

                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = MaterialTheme.colorScheme.background,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { extendNotes(!showNotes) }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TitleText(
                                    textId = R.string.usage_notes_title,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    painter = painterResource(
                                        id = if (showNotes) R.drawable.ic_arrow_up
                                        else R.drawable.ic_arrow_down
                                    ),
                                    contentDescription = null
                                )
                            }
                            AnimatedVisibility(
                                visible = showNotes,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = getUsageNotes(), modifier = Modifier.padding(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getUsageNotes(): String = try {
        // binding.helpVersionName.text = requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0).versionName
        val stream = resources.openRawResource(R.raw.help)
        val htmlString = convertStreamToString(stream)
        stream.close()
        HtmlCompat.fromHtml(htmlString, HtmlCompat.FROM_HTML_MODE_LEGACY).dropLast(2).toString()
    } catch (e: IOException) {
        e.toString()
    } catch (ignored: PackageManager.NameNotFoundException) {
        ""
    }

    companion object {
        fun convertStreamToString(stream: InputStream?): String {
            val s = Scanner(stream, "utf-8").useDelimiter("\\A")
            return if (s.hasNext()) s.next() else ""
        }
    }
}