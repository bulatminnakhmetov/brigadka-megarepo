package com.brigadka.app.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

//
//tertiary: Color = ColorLightTokens.Tertiary,
//onTertiary: Color = ColorLightTokens.OnTertiary,
//tertiaryContainer: Color = ColorLightTokens.TertiaryContainer,
//onTertiaryContainer: Color = ColorLightTokens.OnTertiaryContainer,
//background: Color = ColorLightTokens.Background,
//onBackground: Color = ColorLightTokens.OnBackground,
//surface: Color = ColorLightTokens.Surface,
//onSurface: Color = ColorLightTokens.OnSurface,
//surfaceVariant: Color = ColorLightTokens.SurfaceVariant,
//onSurfaceVariant: Color = ColorLightTokens.OnSurfaceVariant,
//surfaceTint: Color = primary,
//inverseSurface: Color = ColorLightTokens.InverseSurface,
//inverseOnSurface: Color = ColorLightTokens.InverseOnSurface,
//error: Color = ColorLightTokens.Error,
//onError: Color = ColorLightTokens.OnError,
//errorContainer: Color = ColorLightTokens.ErrorContainer,
//onErrorContainer: Color = ColorLightTokens.OnErrorContainer,
//outline: Color = ColorLightTokens.Outline,
//outlineVariant: Color = ColorLightTokens.OutlineVariant,
//scrim: Color = ColorLightTokens.Scrim,
//surfaceBright: Color = ColorLightTokens.SurfaceBright,
//surfaceContainer: Color = ColorLightTokens.SurfaceContainer,
//surfaceContainerHigh: Color = ColorLightTokens.SurfaceContainerHigh,
//surfaceContainerHighest: Color = ColorLightTokens.SurfaceContainerHighest,
//surfaceContainerLow: Color = ColorLightTokens.SurfaceContainerLow,
//surfaceContainerLowest: Color = ColorLightTokens.SurfaceContainerLowest,
//surfaceDim: Color = ColorLightTokens.SurfaceDim,
//secondary: Color = ColorLightTokens.Secondary,
//onSecondary: Color = ColorLightTokens.OnSecondary,
//secondaryContainer: Color = ColorLightTokens.SecondaryContainer,
//onSecondaryContainer: Color = ColorLightTokens.OnSecondaryContainer,
//tertiary: Color = ColorLightTokens.Tertiary,
//onTertiary: Color = ColorLightTokens.OnTertiary,
//tertiaryContainer: Color = ColorLightTokens.TertiaryContainer,
//onTertiaryContainer: Color = ColorLightTokens.OnTertiaryContainer,

val AppColorScheme = lightColorScheme(
    primary = Color.Yellow,
    background = Color.White,
    surface = Color.White,
    surfaceVariant = Color(0xFFf0f2f5),
    onSurfaceVariant = Color.Black,
    surfaceContainer = Color.White,
    secondaryContainer = Color(0xFFf0f2f5)
)


@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
//        colorScheme = AppColorScheme
    ) {
        content()
    }
}