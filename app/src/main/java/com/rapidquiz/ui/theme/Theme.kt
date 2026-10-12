package com.rapidquiz.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Brand,
    secondary = Secondary,
    tertiary = Accent,
    error = Incorrect,
    onSurface = Ink,
    onBackground = Ink,
    background = BackgroundTop,
)

private val AppShapes = Shapes(
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp), // kontrol
    large = androidx.compose.foundation.shape.RoundedCornerShape(24.dp), // kart
)

@Composable
fun RapidQuizTheme(content: @Composable () -> Unit) {
    // Koyu tema kapsam dışı (web'de de yok).
    MaterialTheme(colorScheme = LightColors, typography = AppTypography, shapes = AppShapes, content = content)
}

/** Ekran zemini: #FFF7ED → #FDF2F8 dikey gradyan. */
@Composable
fun AppBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundTop, BackgroundBottom))),
    ) { content() }
}
