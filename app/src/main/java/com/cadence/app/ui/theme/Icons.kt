package com.cadence.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

// Outlined bar-chart icon for the Insights destination, drawn in the same
// 2dp round-cap stroke style as the Material outlined family.
val CadenceIcons.BarChart: ImageVector
    get() {
        if (_barChart != null) return _barChart!!
        _barChart = ImageVector.Builder(
            name = "BarChart",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(4f, 20f)
            lineTo(20f, 20f)
            moveTo(7.5f, 20f)
            lineTo(7.5f, 12f)
            moveTo(12f, 20f)
            lineTo(12f, 5f)
            moveTo(16.5f, 20f)
            lineTo(16.5f, 9f)
        }.build()
        return _barChart!!
    }

private var _barChart: ImageVector? = null

// Outlined sliders icon for the Modes destination.
val CadenceIcons.Sliders: ImageVector
    get() {
        if (_sliders != null) return _sliders!!
        _sliders = ImageVector.Builder(
            name = "Sliders",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            // tracks
            moveTo(4f, 7f)
            lineTo(11f, 7f)
            moveTo(15f, 7f)
            lineTo(20f, 7f)
            moveTo(4f, 12f)
            lineTo(15f, 12f)
            moveTo(19f, 12f)
            lineTo(20f, 12f)
            moveTo(4f, 17f)
            lineTo(9f, 17f)
            moveTo(13f, 17f)
            lineTo(20f, 17f)
        }.path(
            fill = SolidColor(Color.Black),
        ) {
            // knobs (small filled circles on the tracks)
            moveTo(13f, 5.4f)
            arcTo(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 13f, 8.6f)
            arcTo(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 13f, 5.4f)
            moveTo(17f, 10.4f)
            arcTo(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 17f, 13.6f)
            arcTo(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 17f, 10.4f)
            moveTo(11f, 15.4f)
            arcTo(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 11f, 18.6f)
            arcTo(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 11f, 15.4f)
        }.build()
        return _sliders!!
    }

private var _sliders: ImageVector? = null

object CadenceIcons
