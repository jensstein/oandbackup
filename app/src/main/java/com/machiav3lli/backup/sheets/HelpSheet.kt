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
package com.machiav3lli.backup.sheets

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import com.machiav3lli.backup.BuildConfig
import com.machiav3lli.backup.R
import com.machiav3lli.backup.legendList
import com.machiav3lli.backup.linksList
import com.machiav3lli.backup.ui.compose.blockBorder
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.CaretDown
import com.machiav3lli.backup.ui.compose.icons.phosphor.CaretUp
import com.machiav3lli.backup.ui.compose.item.LegendItem
import com.machiav3lli.backup.ui.compose.item.LinkItem
import com.machiav3lli.backup.ui.compose.item.RoundButton
import com.machiav3lli.backup.ui.compose.item.TitleText
import com.machiav3lli.backup.utils.SystemUtils.applicationIssuer
import com.machiav3lli.backup.utils.gridItems
import java.io.IOException
import java.io.InputStream
import java.util.Scanner

@Composable
fun HelpSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val nestedScrollConnection = rememberNestedScrollInteropConnection()

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            ListItem(
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent,
                ),
                headlineContent = {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.headlineMedium,
                            maxLines = 1,
                        )
                        Text(
                            text = BuildConfig.VERSION_NAME,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                supportingContent = {
                    applicationIssuer?.let {
                        Text(
                            text = "signed by $it",
                            modifier = Modifier.padding(horizontal = 8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                trailingContent = {
                    RoundButton(icon = Phosphor.CaretDown) {
                        onDismiss()
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .blockBorder()
                .nestedScroll(nestedScrollConnection)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                    ),
                    shape = MaterialTheme.shapes.large,
                ) {
                    linksList.forEach {
                        LinkItem(
                            item = it,
                            onClick = { uriString ->
                                context.startActivity(
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

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface),
                    shape = MaterialTheme.shapes.large,
                ) {
                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { extendNotes(!showNotes) },
                        headlineContent = {
                            TitleText(R.string.usage_notes_title)
                        },
                        trailingContent = {
                            Icon(
                                imageVector = if (showNotes) Phosphor.CaretUp
                                else Phosphor.CaretDown,
                                contentDescription = null
                            )
                        }
                    )
                    AnimatedVisibility(
                        visible = showNotes,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = context.getUsageNotes(), modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }
    }
}


private fun Context.getUsageNotes(): String = try {
    val stream = resources.openRawResource(R.raw.help)
    val htmlString = convertStreamToString(stream)
    stream.close()
    HtmlCompat.fromHtml(htmlString, HtmlCompat.FROM_HTML_MODE_LEGACY).dropLast(2).toString()
} catch (e: IOException) {
    e.toString()
} catch (ignored: PackageManager.NameNotFoundException) {
    ""
}

fun convertStreamToString(stream: InputStream?): String {
    val s = Scanner(stream, "utf-8").useDelimiter("\\A")
    return if (s.hasNext()) s.next() else ""
}
