package com.machiav3lli.backup.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.machiav3lli.backup.R
import com.machiav3lli.backup.dbs.entity.PackageInfo
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.ArchiveTray
import com.machiav3lli.backup.ui.compose.icons.phosphor.ClockCounterClockwise
import com.machiav3lli.backup.ui.compose.item.ActionButton
import com.machiav3lli.backup.ui.compose.item.ElevatedActionButton

@Composable
fun BaseDialog(
    openDialogCustom: MutableState<Boolean>,
    dialogUI: @Composable (() -> Unit),
) {
    Dialog(
        onDismissRequest = { openDialogCustom.value = false },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        dialogUI()
    }
}

@Composable
fun ActionsDialogUI(
    titleText: String,
    messageText: String,
    openDialogCustom: MutableState<Boolean>,
    primaryText: String,
    primaryIcon: ImageVector? = null,
    primaryAction: (() -> Unit) = {},
    secondaryText: String = "",
    secondaryIcon: ImageVector? = null,
    secondaryAction: (() -> Unit)? = null,
) {
    val scrollState = rememberScrollState()

    Card(
        shape = RoundedCornerShape(8.dp),
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
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .fillMaxWidth()
                    .weight(1f, false)
            ) {
                Text(text = messageText, style = MaterialTheme.typography.bodyMedium)
            }

            Row(
                Modifier.fillMaxWidth()
            ) {
                ActionButton(text = stringResource(id = R.string.dialogCancel)) {
                    openDialogCustom.value = false
                }
                Spacer(Modifier.weight(1f))
                if (secondaryAction != null && secondaryText.isNotEmpty()) {
                    ElevatedActionButton(
                        text = secondaryText,
                        icon = secondaryIcon,
                        positive = false
                    ) {
                        secondaryAction()
                        openDialogCustom.value = false
                    }
                    Spacer(Modifier.requiredWidth(8.dp))
                }
                ElevatedActionButton(
                    text = primaryText,
                    icon = primaryIcon,
                ) {
                    primaryAction()
                    openDialogCustom.value = false
                }
            }
        }
    }
}

@Composable
fun BatchActionDialogUI(
    backupBoolean: Boolean,
    selectedPackageInfos: List<PackageInfo>,
    selectedApk: Map<String, Int>,
    selectedData: Map<String, Int>,
    openDialogCustom: MutableState<Boolean>,
    primaryAction: (() -> Unit) = {},
) {
    val message = StringBuilder()
    selectedPackageInfos.forEach { pi ->
        message.append(pi.packageLabel)
        message.append(
            ": ${
                stringResource(
                    id = when {
                        selectedApk[pi.packageName] != null && selectedData[pi.packageName] != null -> R.string.handleBoth
                        selectedApk[pi.packageName] != null                                         -> R.string.handleApk
                        selectedData[pi.packageName] != null                                        -> R.string.handleData
                        else                                                                        -> R.string.errorDialogTitle
                    }
                )
            }\n"
        )
    }

    ActionsDialogUI(
        titleText = stringResource(
            id = if (backupBoolean) R.string.backupConfirmation
            else R.string.restoreConfirmation
        ),
        messageText = message.toString().trim { it <= ' ' },
        openDialogCustom = openDialogCustom,
        primaryText = stringResource(
            id = if (backupBoolean) R.string.backup
            else R.string.restore
        ),
        primaryIcon = if (backupBoolean) Phosphor.ArchiveTray
        else Phosphor.ClockCounterClockwise,
        primaryAction = primaryAction,
    )
}
