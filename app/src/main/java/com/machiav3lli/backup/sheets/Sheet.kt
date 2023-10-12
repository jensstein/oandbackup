package com.machiav3lli.backup.sheets

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Sheet(
    onDismissRequest: () -> Unit,
    //modifier: Modifier,
    sheetState: SheetState,
    //shape: Shape,
    //containerColor: Color,
    //contentColor: Color,
    //tonalElevation: Dp,
    //scrimColor: Color,
    //dragHandle: @Composable() (() -> Unit)?,
    //windowInsets: WindowInsets,
    content: @Composable() (ColumnScope.() -> Unit),
) {
    ModalBottomSheet(
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        scrimColor = Color.Transparent,
        dragHandle = null,
        onDismissRequest = onDismissRequest
    ) {
        content()
    }
}
