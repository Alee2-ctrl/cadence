package com.cadence.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.cadence.app.R

// Cadence T3 palette: matcha greens, forest ink, pistachio paper
val Paper = Color(0xFFF5F8EC)
val CardWhite = Color(0xFFFFFFFF)
val Ink = Color(0xFF013237)
val Forest = Color(0xFF1D2E1B)
val Leaf = Color(0xFF4CA771)
val Matcha = Color(0xFFA9C632)
val TeaMist = Color(0xFFC8D2A6)
val Pistachio = Color(0xFFDDE9C3)
val Mist = Color(0xFFEAF9E7)
val Bamboo = Color(0xFFE6D4A6)
val SageDone = Color(0xFFC0E6BA)
val Faint = Color(0xFF6E8078)

val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
)

private fun Typography.withFont(f: FontFamily) = Typography(
    displayLarge = displayLarge.copy(fontFamily = f),
    displayMedium = displayMedium.copy(fontFamily = f),
    displaySmall = displaySmall.copy(fontFamily = f),
    headlineLarge = headlineLarge.copy(fontFamily = f),
    headlineMedium = headlineMedium.copy(fontFamily = f),
    headlineSmall = headlineSmall.copy(fontFamily = f),
    titleLarge = titleLarge.copy(fontFamily = f),
    titleMedium = titleMedium.copy(fontFamily = f),
    titleSmall = titleSmall.copy(fontFamily = f),
    bodyLarge = bodyLarge.copy(fontFamily = f),
    bodyMedium = bodyMedium.copy(fontFamily = f),
    bodySmall = bodySmall.copy(fontFamily = f),
    labelLarge = labelLarge.copy(fontFamily = f),
    labelMedium = labelMedium.copy(fontFamily = f),
    labelSmall = labelSmall.copy(fontFamily = f),
)

private val MatchaPaper = lightColorScheme(
    primary = Leaf,
    onPrimary = Color.White,
    secondary = Forest,
    onSecondary = Paper,
    tertiary = Matcha,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Mist,
    onSurfaceVariant = Ink,
    outline = TeaMist,
)

@Composable
fun CadenceTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MatchaPaper,
        typography = Typography().withFont(Poppins),
        content = content,
    )
}
