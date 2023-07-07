package com.machiav3lli.backup.sheets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.preferences.pref_fixNavBarOverlap

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
): Unit {
    ModalBottomSheet(
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        scrimColor = Color.Transparent,
        onDismissRequest = onDismissRequest
    ) {
        if (pref_fixNavBarOverlap.value > 0) {
            Column(
                modifier = Modifier
                    .padding(bottom = pref_fixNavBarOverlap.value.dp)
            ) {
                content()
            }
            // another possibility
            //Column {
            //    Column(modifier = Modifier.weight(1f)) {
            //        content()
            //    }
            //    Spacer(modifier = Modifier
            //        .fillMaxWidth()
            //        .height(navBarHeight)
            //        .background(Color.Red),
            //    )
            //}
        } else {
            content()
        }
    }
}
