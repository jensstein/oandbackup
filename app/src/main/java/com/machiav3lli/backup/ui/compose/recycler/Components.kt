package com.machiav3lli.backup.ui.compose.recycler

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.R

@Composable
fun <T> VerticalItemList(
    modifier: Modifier = Modifier,
    list: List<T>?,
    itemKey: ((T) -> Any)? = null,
    itemContent: @Composable LazyItemScope.(T) -> Unit
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize(),
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