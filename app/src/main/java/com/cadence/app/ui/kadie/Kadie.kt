package com.cadence.app.ui.kadie

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.cadence.app.ui.theme.Forest
import com.cadence.app.ui.theme.Matcha

enum class KadieMood { SLEEP, IDLE, HAPPY }

@Composable
fun Kadie(mood: KadieMood, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "kadie")
    val bob by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bob",
    )
    val blink by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3400
                1f at 0
                1f at 3000
                0.05f at 3100
                1f at 3220
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "blink",
    )

    Canvas(modifier) {
        val u = size.minDimension / 48f
        val lift = bob * u * 1.2f
        val strokeW = u * 1.6f
        val stroke = Stroke(width = strokeW, cap = StrokeCap.Round)

        // antenna
        drawLine(
            Forest,
            Offset(24f * u, 14f * u - lift),
            Offset(24f * u, 6.5f * u - lift),
            strokeWidth = strokeW,
            cap = StrokeCap.Round,
        )
        drawCircle(Matcha, radius = 2.2f * u, center = Offset(24f * u, 4.8f * u - lift))

        // head
        drawRoundRect(
            Forest,
            topLeft = Offset(8f * u, 14f * u - lift),
            size = Size(32f * u, 26f * u - 2f * u),
            cornerRadius = CornerRadius(9f * u, 9f * u),
            style = stroke,
        )

        // eyes
        if (mood == KadieMood.SLEEP) {
            drawArc(
                Forest, 200f, 140f, false,
                topLeft = Offset(13.5f * u, 21f * u - lift),
                size = Size(6f * u, 4.5f * u),
                style = stroke,
            )
            drawArc(
                Forest, 200f, 140f, false,
                topLeft = Offset(28.5f * u, 21f * u - lift),
                size = Size(6f * u, 4.5f * u),
                style = stroke,
            )
        } else {
            val eyeH = 3.2f * u * blink.coerceAtLeast(0.05f)
            drawOval(
                Matcha,
                topLeft = Offset(14.6f * u, 22.5f * u - eyeH / 2 - lift),
                size = Size(3.6f * u, eyeH),
            )
            drawOval(
                Matcha,
                topLeft = Offset(29.8f * u, 22.5f * u - eyeH / 2 - lift),
                size = Size(3.6f * u, eyeH),
            )
        }

        // mouth
        when (mood) {
            KadieMood.HAPPY -> drawArc(
                Forest, 15f, 150f, false,
                topLeft = Offset(18f * u, 26.5f * u - lift),
                size = Size(12f * u, 7f * u),
                style = stroke,
            )
            KadieMood.IDLE -> drawLine(
                Forest,
                Offset(20f * u, 31f * u - lift),
                Offset(28f * u, 31f * u - lift),
                strokeWidth = strokeW,
                cap = StrokeCap.Round,
            )
            KadieMood.SLEEP -> drawCircle(
                Forest,
                radius = 1.7f * u,
                center = Offset(24f * u, 31f * u - lift),
                style = stroke,
            )
        }
    }
}
