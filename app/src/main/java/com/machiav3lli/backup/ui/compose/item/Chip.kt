package com.machiav3lli.backup.ui.compose.item

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.ui.item.ChipItem
import com.machiav3lli.backup.ui.item.InfoChipItem

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionChip(
    item: ChipItem,
    isSelected: Boolean,
    colors: SelectableChipColors = FilterChipDefaults.filterChipColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ),
    onClick: () -> Unit,
) {
    val selectableChipTransitionState = selectableChipTransition(selected = isSelected)

    FilterChip(
        colors = colors,
        shape = RoundedCornerShape(selectableChipTransitionState.cornerRadius),
        border = null,
        selected = isSelected,
        leadingIcon = {
            ButtonIcon(item.icon, item.textId)
        },
        onClick = onClick,
        label = {
            Text(text = stringResource(id = item.textId))
        }
    )
}

@Composable
fun InfoChip(
    item: InfoChipItem,
) {
    SuggestionChip(
        icon = {
            if (item.icon != null) Icon(
                imageVector = item.icon,
                contentDescription = item.text,
            )
        },
        border = null,
        label = {
            Text(text = item.text)
        },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = item.color ?: MaterialTheme.colorScheme.surfaceVariant,
            labelColor = if (item.color != null) MaterialTheme.colorScheme.background
            else MaterialTheme.colorScheme.onSurfaceVariant,
            iconContentColor = if (item.color != null) MaterialTheme.colorScheme.background
            else MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        onClick = {}
    )
}
