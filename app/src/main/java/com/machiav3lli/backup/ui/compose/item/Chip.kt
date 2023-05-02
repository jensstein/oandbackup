package com.machiav3lli.backup.ui.compose.item

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private enum class SelectionState { Unselected, Selected }

class SelectableChipTransition constructor(
    cornerRadius: State<Dp>,
) {
    val cornerRadius by cornerRadius
}

@Composable
fun selectableChipTransition(selected: Boolean): SelectableChipTransition {
    val transition = updateTransition(
        targetState = if (selected) SelectionState.Selected else SelectionState.Unselected,
        label = "chip_transition"
    )
    val corerRadius = transition.animateDp(label = "chip_corner") { state ->
        when (state) {
            SelectionState.Unselected -> 8.dp
            SelectionState.Selected   -> 16.dp
        }
    }
    return remember(transition) {
        SelectableChipTransition(corerRadius)
    }
}