package com.machiav3lli.backup.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.R
import com.machiav3lli.backup.ui.compose.item.ActionButton
import com.machiav3lli.backup.ui.compose.item.ElevatedActionButton
import com.machiav3lli.backup.ui.compose.item.MultiSelectionListItem

@Composable
fun <T> MultiSelectionDialogUI(
    titleText: String,
    entryMap: Map<T, String>,
    selectedItems: List<T>,
    openDialogCustom: MutableState<Boolean>,
    onSave: (List<T>) -> Unit,
) {
    val context = LocalContext.current
    var selected by remember { mutableStateOf(selectedItems) }
    val entryPairs = entryMap.toList()

    Card(
        modifier = Modifier.padding(8.dp),
        elevation = CardDefaults.elevatedCardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = titleText, style = MaterialTheme.typography.titleLarge)
            LazyColumn(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 8.dp)
                    .weight(1f, false)
            ) {
                items(items = entryPairs) { (key, label) ->
                    val isSelected = rememberSaveable(selected) {
                        mutableStateOf(selected.contains(key))
                    }

                    MultiSelectionListItem(
                        text = label,
                        isChecked = isSelected.value,
                    ) {
                        selected = if (it) selected.plus(key)
                        else selected.minus(key)
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth()
            ) {
                ActionButton(text = stringResource(id = R.string.dialogCancel)) {
                    openDialogCustom.value = false
                }
                Spacer(Modifier.weight(1f))
                ElevatedActionButton(text = stringResource(id = R.string.dialogOK)) {
                    onSave(selected)
                    openDialogCustom.value = false
                }
            }
        }
    }
}