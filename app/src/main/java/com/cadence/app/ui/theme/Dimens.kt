package com.cadence.app.ui.theme

import androidx.compose.ui.unit.dp

// Spacing tokens. Avoid arbitrary values; reach for these first.
object Space {
    val Micro = 4.dp
    val Small = 8.dp
    val Compact = 12.dp
    val Standard = 16.dp
    val CardPad = 20.dp
    val ScreenMargin = 24.dp
    val Section = 32.dp
}

// Corner radii. Fully rounded is reserved for intentional pills/FABs/toggles.
object Radius {
    val Tag = 8.dp          // tags / tiny controls
    val Chip = 12.dp        // chips / segmented controls
    val CompactCard = 16.dp // compact cards and inputs
    val Card = 20.dp        // standard cards
    val Hero = 24.dp        // prominent / hero cards
}

// Icon sizes.
object IconSize {
    val Compact = 20.dp
    val Nav = 24.dp
}
