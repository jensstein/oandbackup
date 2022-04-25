package com.machiav3lli.backup.ui.compose.recycler

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.machiav3lli.backup.R
import com.machiav3lli.backup.ui.item.ChipItem

@Composable
fun <T> VerticalItemList(
    modifier: Modifier = Modifier.fillMaxSize(),
    list: List<T>?,
    itemKey: ((T) -> Any)? = null,
    itemContent: @Composable LazyItemScope.(T) -> Unit
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = if (list.isNullOrEmpty()) Alignment.Center else Alignment.TopStart
    ) {
        when {
            list == null -> Text(
                text = stringResource(id = R.string.loading_list),
                color = MaterialTheme.colorScheme.onBackground
            )
            list.isEmpty() -> Text(
                text = stringResource(id = R.string.empty_filtered_list),
                color = MaterialTheme.colorScheme.onBackground
            )
            else -> {
                // TODO add scrollbars
                LazyColumn(
                    verticalArrangement = Arrangement.Absolute.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp, horizontal = 8.dp),
                ) {
                    items(items = list, key = itemKey, itemContent = itemContent)
                }
            }
        }
    }
}

@Composable
fun <T> SizedItemList(
    modifier: Modifier = Modifier,
    itemHeight: Int,
    list: List<T>?,
    itemKey: ((T) -> Any)? = null,
    itemContent: @Composable LazyItemScope.(T) -> Unit
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .requiredHeight(if (list != null) (list.size * (8 + itemHeight) + 8).dp else 20.dp),
        contentAlignment = if (list.isNullOrEmpty()) Alignment.Center else Alignment.TopStart
    ) {
        when {
            list == null -> Text(
                text = stringResource(id = R.string.loading_list),
                color = MaterialTheme.colorScheme.onBackground
            )
            list.isEmpty() -> Text(
                text = stringResource(id = R.string.empty_filtered_list),
                color = MaterialTheme.colorScheme.onBackground
            )
            else -> {
                // TODO add scrollbars
                LazyColumn(
                    verticalArrangement = Arrangement.Absolute.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp, horizontal = 8.dp),
                ) {
                    items(items = list, key = itemKey, itemContent = itemContent)
                }
            }
        }
    }
}

@Composable
fun <T> HorizontalItemList(
    modifier: Modifier = Modifier,
    list: List<T>?,
    itemKey: ((T) -> Any)? = null,
    itemContent: @Composable LazyItemScope.(T) -> Unit
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .fillMaxWidth(),
        contentAlignment = if (list.isNullOrEmpty()) Alignment.Center else Alignment.CenterStart
    ) {
        when {
            list == null -> Text(
                text = stringResource(id = R.string.loading_list),
                color = MaterialTheme.colorScheme.onBackground
            )
            list.isEmpty() -> Text(
                text = stringResource(id = R.string.empty_filtered_list),
                color = MaterialTheme.colorScheme.onBackground
            )
            else -> {
                LazyRow(
                    horizontalArrangement = Arrangement.Absolute.spacedBy(4.dp),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp),
                ) {
                    items(items = list, key = itemKey, itemContent = itemContent)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SelectableChipGroup(
    modifier: Modifier = Modifier,
    list: List<ChipItem>,
    selectedFlag: Int,
    onClick: (Int) -> Unit
) {
    val (selectedFlag, setFlag) = remember { mutableStateOf(selectedFlag) }

    FlowRow(
        modifier = modifier
            .fillMaxWidth(),
        mainAxisSpacing = 8.dp
    ) {
        list.forEach {
            val colors = ChipDefaults.outlinedFilterChipColors(
                backgroundColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface,
                selectedBackgroundColor = Color.Transparent,
                selectedContentColor = MaterialTheme.colorScheme.primary,
                leadingIconColor = MaterialTheme.colorScheme.onSurface,
                selectedLeadingIconColor = colorResource(id = it.colorId)
            )

            FilterChip(
                colors = colors,
                border = BorderStroke(
                    1.dp,
                    if (it.flag == selectedFlag) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                ),
                selected = it.flag == selectedFlag,
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = it.iconId),
                        contentDescription = stringResource(id = it.textId),
                        tint = colors.leadingIconColor(
                            enabled = true,
                            selected = it.flag == selectedFlag
                        ).value,
                        modifier = Modifier.size(24.dp)
                    )
                },
                onClick = {
                    setFlag(it.flag)
                    onClick(it.flag)
                }
            ) {
                Text(
                    text = stringResource(id = it.textId),
                    color = colors.contentColor(
                        enabled = true,
                        selected = it.flag == selectedFlag
                    ).value,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MultiSelectableChipGroup(
    modifier: Modifier = Modifier,
    list: List<ChipItem>,
    selectedFlags: Int,
    onClick: (Int) -> Unit
) {
    val (selectedFlags, setFlag) = remember { mutableStateOf(selectedFlags) }

    FlowRow(
        modifier = modifier
            .fillMaxWidth(),
        mainAxisSpacing = 8.dp
    ) {
        list.forEach {
            val colors = ChipDefaults.outlinedFilterChipColors(
                backgroundColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface,
                selectedBackgroundColor = Color.Transparent,
                selectedContentColor = MaterialTheme.colorScheme.primary,
                leadingIconColor = MaterialTheme.colorScheme.onSurface,
                selectedLeadingIconColor = colorResource(id = it.colorId)
            )

            FilterChip(
                colors = colors,
                border = BorderStroke(
                    1.dp,
                    if (it.flag and selectedFlags != 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                ),
                selected = it.flag and selectedFlags != 0,
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = it.iconId),
                        contentDescription = stringResource(id = it.textId),
                        tint = colors.leadingIconColor(
                            enabled = true,
                            selected = it.flag and selectedFlags != 0
                        ).value,
                        modifier = Modifier.size(24.dp)
                    )
                },
                onClick = {
                    setFlag(selectedFlags xor it.flag)
                    onClick(it.flag)
                }
            ) {
                Text(
                    text = stringResource(id = it.textId),
                    color = colors.contentColor(
                        enabled = true,
                        selected = it.flag and selectedFlags != 0
                    ).value,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}