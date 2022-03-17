package com.machiav3lli.backup.ui.compose.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val LocalShapes = staticCompositionLocalOf { ShapeSize() }

@Immutable
data class ShapeSize(
    val small: Dp = 4.dp,
    val medium: Dp = 8.dp,
    val large: Dp = 16.dp,
)
