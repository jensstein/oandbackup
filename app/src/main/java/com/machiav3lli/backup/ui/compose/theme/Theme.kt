package com.machiav3lli.backup.ui.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import com.machiav3lli.backup.utils.accentStyle
import com.machiav3lli.backup.utils.brighter
import com.machiav3lli.backup.utils.darker
import com.machiav3lli.backup.utils.getPrimaryColor
import com.machiav3lli.backup.utils.getSecondaryColor
import com.machiav3lli.backup.utils.secondaryStyle

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    CompositionLocalProvider(LocalShapes provides ShapeSize()) {
        MaterialTheme(
            colorScheme = when {
                darkTheme -> DarkColors.copy(
                    primary = getPrimaryColor(LocalContext.current.accentStyle),
                    primaryContainer = getPrimaryColor(LocalContext.current.accentStyle)
                        .darker(0.2f),
                    inverseOnSurface = getPrimaryColor(LocalContext.current.accentStyle),
                    secondary = getSecondaryColor(LocalContext.current.secondaryStyle),
                    secondaryContainer = getSecondaryColor(LocalContext.current.secondaryStyle)
                        .darker(0.2f),
                )
                else -> LightColors.copy(
                    primary = getPrimaryColor(LocalContext.current.accentStyle),
                    primaryContainer = getPrimaryColor(LocalContext.current.accentStyle)
                        .brighter(0.2f),
                    inverseOnSurface = getPrimaryColor(LocalContext.current.accentStyle),
                    secondary = getSecondaryColor(LocalContext.current.secondaryStyle),
                    secondaryContainer = getSecondaryColor(LocalContext.current.secondaryStyle)
                        .brighter(0.2f),
                )
            },
            content = content
        )
    }
}

private val LightColors = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimary.darker(0.1f),
    onPrimaryContainer = LightOnPrimary,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondary.darker(0.1f),
    onSecondaryContainer = LightOnSecondary,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    background = LightBackground,
    onBackground = LightOnBackground,
    inversePrimary = LightInversePrimary,
    inverseSurface = LightInverseSurface,
    inverseOnSurface = LightInverseOnSurface,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightError.brighter(0.3f),
    onErrorContainer = LightOnError.darker(0.3f)
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimary.brighter(0.1f),
    onPrimaryContainer = DarkOnPrimary,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondary.brighter(0.1f),
    onSecondaryContainer = DarkOnSecondary,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    inversePrimary = DarkInversePrimary,
    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkError.darker(0.3f),
    onErrorContainer = DarkOnError.brighter(0.3f)
)
