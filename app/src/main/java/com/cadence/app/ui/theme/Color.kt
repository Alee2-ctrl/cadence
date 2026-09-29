package com.cadence.app.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// Cadence semantic color tokens (derived from the approved mockup).
// Screens should prefer these over raw hex. Legacy names below are aliases so
// existing screens adopt the new palette without a rewrite.
// ---------------------------------------------------------------------------

val Background = Color(0xFFF5F6F0)        // warm off-white / very pale sage
val Surface = Color(0xFFFFFFFF)           // white elevated surfaces
val SurfaceSecondary = Color(0xFFEEF3E6)  // pale sage fills
val Primary = Color(0xFF1C4B38)           // deep forest green (brand/action)
val PrimaryContainer = Color(0xFFE1EFDD)  // soft green selected state
val TextPrimary = Color(0xFF14261E)       // near-black green
val TextSecondary = Color(0xFF75857A)     // muted gray-green
val Outline = Color(0xFFE2E7DD)           // subtle borders instead of shadows
val Success = Color(0xFF4E9A67)
val Warning = Color(0xFFD19A3D)
val Destructive = Color(0xFFC4573E)

// ---- Legacy aliases (retuned to the new palette) ----
val Paper = Background
val CardWhite = Surface
val Ink = TextPrimary
val Forest = Primary
val Leaf = Success
val Matcha = Color(0xFFA9C632)
val TeaMist = Color(0xFFC9D3BD)
val Pistachio = Color(0xFFDDE9C3)
val Mist = Color(0xFFEAF3E4)
val Bamboo = Color(0xFFE6D4A6)
val SageDone = Color(0xFFCDE8C4)
val Faint = TextSecondary
val Red = Destructive
val Honey = Warning
val SkyBlue = Color(0xFF7FA8C9)
