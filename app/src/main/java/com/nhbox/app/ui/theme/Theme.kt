package com.nhbox.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val NhDarkColorScheme = darkColorScheme(
    primary = NhRed,
    onPrimary = NhWhite,
    secondary = NhSecondaryBg,
    onSecondary = NhWhite,
    background = NhBackground,
    onBackground = NhWhite,
    surface = NhSecondaryBg,
    onSurface = NhWhite,
    surfaceVariant = NhCardBg,
    onSurfaceVariant = NhLightGray,
    outline = NhCardBorder,
    error = NhError,
    onError = NhWhite
)

private val NhLightColorScheme = lightColorScheme(
    primary = NhRed,
    onPrimary = NhWhite,
    secondary = NhSecondaryBg,
    onSecondary = NhWhite,
    background = NhBackground,
    onBackground = NhWhite,
    surface = NhSecondaryBg,
    onSurface = NhWhite,
    surfaceVariant = NhCardBg,
    onSurfaceVariant = NhLightGray,
    outline = NhCardBorder,
    error = NhError,
    onError = NhWhite
)

@Composable
fun NhBoxTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) NhDarkColorScheme else NhLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = NhTypography,
        content = content
    )
}
