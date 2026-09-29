package com.cadence.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Paper = Color(0xFFFAF6EE)
val Ink = Color(0xFF1C1917)
val Clay = Color(0xFFE8632C)
val Soft = Color(0xFFF3EDE0)
val Stone = Color(0xFFE5DCCA)
val Sage = Color(0xFF9DBF8E)
val Butter = Color(0xFFF5C64F)
val Lilac = Color(0xFFC7B9E8)
val Faint = Color(0xFF8A8378)
val CardWhite = Color(0xFFFFFFFF)

private val WarmPaper = lightColorScheme(
    primary = Clay,
    onPrimary = Color.White,
    secondary = Ink,
    onSecondary = Paper,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Soft,
    onSurfaceVariant = Ink,
    outline = Stone
)

@Composable
fun CadenceTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WarmPaper,
        content = content
    )
}
