package com.machiav3lli.backup.ui.compose.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.machiav3lli.backup.preferences.pref_appTheme
import com.machiav3lli.backup.utils.brighter
import com.machiav3lli.backup.utils.darker
import com.machiav3lli.backup.utils.isBlackTheme
import com.machiav3lli.backup.utils.isDynamicTheme
import com.machiav3lli.backup.utils.primaryColor
import com.machiav3lli.backup.utils.secondaryColor

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val blackTheme by remember(pref_appTheme) { mutableStateOf(isBlackTheme) }

    MaterialTheme(
        colorScheme = when {
            isDynamicTheme && isSystemInDarkTheme() && blackTheme
            -> dynamicBlackColorScheme(context)

            isDynamicTheme && isSystemInDarkTheme()
            -> dynamicDarkColorScheme(context)

            isDynamicTheme
            -> dynamicLightColorScheme(context)

            darkTheme && blackTheme
            -> DarkColors.copy(
                background = Color.Black,
                primary = primaryColor,
                primaryContainer = primaryColor
                    .darker(0.2f),
                inverseOnSurface = primaryColor,
                tertiary = secondaryColor,
                tertiaryContainer = secondaryColor
                    .darker(0.2f),
                surfaceTint = primaryColor
            )

            darkTheme
            -> DarkColors.copy(
                primary = primaryColor,
                primaryContainer = primaryColor
                    .darker(0.2f),
                inverseOnSurface = primaryColor,
                tertiary = secondaryColor,
                tertiaryContainer = secondaryColor
                    .darker(0.2f),
                surfaceTint = primaryColor
            )

            else
            -> LightColors.copy(
                primary = primaryColor,
                primaryContainer = primaryColor
                    .brighter(0.2f),
                inverseOnSurface = primaryColor,
                tertiary = secondaryColor,
                tertiaryContainer = secondaryColor
                    .brighter(0.2f),
                surfaceTint = primaryColor
            )
        },
        content = content
    )
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
    //background = LightBackground,
    onBackground = LightOnBackground,
    inversePrimary = LightInversePrimary,
    inverseSurface = LightInverseSurface,
    inverseOnSurface = LightInverseOnSurface,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightError.brighter(0.3f),
    onErrorContainer = LightOnError.darker(0.3f),
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
    //background = DarkBackground,
    onBackground = DarkOnBackground,
    inversePrimary = DarkInversePrimary,
    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkError.darker(0.3f),
    onErrorContainer = DarkOnError.brighter(0.3f)
)

fun dynamicBlackColorScheme(context: Context) = dynamicDarkColorScheme(context).copy(
    background = Color.Black,
)
