package be.sigmadelta.becycle.common.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily


private val DarkColorPalette = darkColors(
    primary = primaryAccent,
    primaryVariant = variantAccent,
    secondary = secondaryAccent,
    error = errorColor,
)

private val LightColorPalette = lightColors(
    primary = primaryAccent,
    primaryVariant = variantAccent,
    secondary = secondaryAccent,
    error = errorColor
)

@Composable
fun BecycleTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        content = content,
        typography = Typography(defaultFontFamily = montserrat)
    )
}