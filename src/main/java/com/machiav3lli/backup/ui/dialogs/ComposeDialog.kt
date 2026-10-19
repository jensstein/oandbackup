package com.machiav3lli.backup.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.machiav3lli.backup.R
import com.machiav3lli.backup.data.dbs.entity.PackageInfo
import com.machiav3lli.backup.ui.compose.component.DialogNegativeButton
import com.machiav3lli.backup.ui.compose.component.DialogPositiveButton
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.ArchiveTray
import com.machiav3lli.backup.ui.compose.icons.phosphor.ClockCounterClockwise

@Composable
fun BaseDialog(
    onDismiss: () -> Unit,
    dialogUI: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        dialogUI()
    }
}

@Composable
fun ActionsDialogUI(
    titleText: String,
    messageText: String,
    onDismiss: () -> Unit,
    primaryText: String,
    primaryIcon: ImageVector? = null,
    primaryAction: () -> Unit = {},
    secondaryText: String = "",
    secondaryAction: (() -> Unit)? = null,
) {
    val scrollState = rememberScrollState()

    Card(
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
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
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = messageText,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            ) {
                DialogNegativeButton(
                    text = stringResource(id = R.string.dialogCancel),
                    onClick = onDismiss
                )
                Spacer(Modifier.weight(1f))
                if (secondaryAction != null && secondaryText.isNotEmpty()) {
                    DialogNegativeButton(
                        text = secondaryText,
                    ) {
                        secondaryAction()
                        onDismiss()
                    }
                    Spacer(Modifier.requiredWidth(8.dp))
                }
                if (primaryIcon != null) DialogPositiveButton(
                    text = primaryText,
                    icon = primaryIcon,
                ) {
                    primaryAction()
                    onDismiss()
                }
                else DialogPositiveButton(text = primaryText) {
                    primaryAction()
                    onDismiss()
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
    onDismiss: () -> Unit,
    primaryAction: () -> Unit = {},
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
        onDismiss = onDismiss,
        primaryText = stringResource(
            id = if (backupBoolean) R.string.backup
            else R.string.restore
        ),
        primaryIcon = if (backupBoolean) Phosphor.ArchiveTray
        else Phosphor.ClockCounterClockwise,
        primaryAction = primaryAction,
    )
}
