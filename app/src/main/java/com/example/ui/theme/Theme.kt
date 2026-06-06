package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ElegantDarkColorScheme = darkColorScheme(
    primary = ElegantLavender,
    onPrimary = ElegantDeepPurple,
    secondary = ElegantActiveBg,
    onSecondary = ElegantTextGlacier,
    tertiary = ElegantCoral,
    background = ElegantDarkBg,
    surface = ElegantSurface,
    onBackground = ElegantTextGlacier,
    onSurface = ElegantTextGlacier,
    surfaceVariant = ElegantActiveBg,
    onSurfaceVariant = ElegantTextSilver
)

private val ElegantLightColorScheme = lightColorScheme(
    primary = ElegantLavender,
    onPrimary = ElegantDeepPurple,
    secondary = ElegantActiveBg,
    onSecondary = ElegantTextGlacier,
    tertiary = ElegantCoral,
    background = ElegantTextGlacier,
    surface = ElegantActiveBg,
    onBackground = ElegantDarkBg,
    onSurface = ElegantDarkBg
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // We strictly enforce Dark Theme for the Elegant Dark premium experience
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) ElegantDarkColorScheme else ElegantLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
