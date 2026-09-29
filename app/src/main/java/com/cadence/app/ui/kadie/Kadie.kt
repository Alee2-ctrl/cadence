package com.cadence.app.ui.kadie

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.cadence.app.ui.theme.Forest
import com.cadence.app.ui.theme.Honey
import com.cadence.app.ui.theme.Matcha

// Kadie moods:
// SLEEP   - bedtime / recharging, eyes closed, floating z's
// IDLE    - nothing on the agenda: looks around, breathes, settles
// EXCITED - tasks waiting: bounces, waves arms
// FROWN   - overdue tasks: drooped antenna, sways, frowns
// HAPPY   - everything done: jumps, arms up, sparkles
enum class KadieMood { SLEEP, IDLE, EXCITED, FROWN, HAPPY }

@Composable
fun Kadie(
    mood: KadieMood,
    modifier: Modifier = Modifier,
    lineColor: Color = Forest,
    accent: Color = Matcha,
    cheek: Color = Honey,
) {
    val t = rememberInfiniteTransition(label = "kadie")

    val bob by t.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bob",
    )
    val bounce by t.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(430, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bounce",
    )
    val jump by t.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            keyframes { durationMillis = 1400; 0f at 0; 1f at 340; 0f at 680; 0f at 1400 },
            RepeatMode.Restart,
        ),
        label = "jump",
    )
    val look by t.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = 5600
                0.5f at 0; 0.5f at 1300; 0.95f at 1900; 0.95f at 3300; 0.05f at 3900; 0.05f at 5000; 0.5f at 5600
            },
            RepeatMode.Restart,
        ),
        label = "look",
    )
    val blink by t.animateFloat(
        initialValue = 1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            keyframes { durationMillis = 3400; 1f at 0; 1f at 3000; 0.05f at 3100; 1f at 3220 },
            RepeatMode.Restart,
        ),
        label = "blink",
    )
    val sway by t.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2300, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "sway",
    )
    val wave by t.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(430, easing = LinearEasing), RepeatMode.Reverse),
        label = "wave",
    )
    val sparkle by t.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Reverse),
        label = "sparkle",
    )

    Canvas(modifier) {
        // Design space: 64 wide x 58 tall units.
        val u = minOf(size.width / 64f, size.height / 58f)
        val padX = (size.width - 64f * u) / 2f
        val padY = (size.height - 58f * u) / 2f
        val strokeW = u * 1.7f
        val stroke = Stroke(width = strokeW, cap = StrokeCap.Round)

        val lift = when (mood) {
            KadieMood.EXCITED -> bounce * 3.2f * u
            KadieMood.HAPPY -> jump * 4.5f * u
            KadieMood.SLEEP -> bob * 0.8f * u
            else -> bob * 1.1f * u
        }
        val swayX = if (mood == KadieMood.FROWN) (sway - 0.5f) * 2.4f * u else 0f

        fun px(x: Float) = padX + x * u + swayX
        fun py(y: Float) = padY + y * u - lift
        fun line(color: Color, x1: Float, y1: Float, x2: Float, y2: Float, w: Float = strokeW) =
            drawLine(color, Offset(px(x1), py(y1)), Offset(px(x2), py(y2)), strokeWidth = w, cap = StrokeCap.Round)

        // ---- antenna ----
        if (mood == KadieMood.FROWN) {
            // drooped antenna
            line(lineColor, 32f, 14f, 36.5f, 9f)
            drawCircle(accent, radius = 2.1f * u, center = Offset(px(37.5f), py(8f)))
        } else {
            line(lineColor, 32f, 14f, 32f, 7.5f)
            drawCircle(accent, radius = 2.2f * u, center = Offset(px(32f), py(5.6f)))
        }

        // ---- head ----
        drawRoundRect(
            lineColor,
            topLeft = Offset(px(20f), py(14f)),
            size = Size(24f * u, 19f * u),
            cornerRadius = CornerRadius(7f * u, 7f * u),
            style = stroke,
        )

        // ---- eyes ----
        val lookX = if (mood == KadieMood.IDLE) (look - 0.5f) * 2.6f else 0f
        when (mood) {
            KadieMood.SLEEP -> {
                // closed: small lower arcs
                listOf(26.5f, 37.5f).forEach { ex ->
                    drawArc(
                        lineColor, 10f, 160f, false,
                        topLeft = Offset(px(ex - 2.4f), py(22f)),
                        size = Size(4.8f * u, 3.6f * u),
                        style = stroke,
                    )
                }
            }
            KadieMood.HAPPY -> {
                // joyful closed arcs (upside-down U)
                listOf(26.5f, 37.5f).forEach { ex ->
                    drawArc(
                        lineColor, 190f, 160f, false,
                        topLeft = Offset(px(ex - 2.5f), py(21.2f)),
                        size = Size(5f * u, 4f * u),
                        style = stroke,
                    )
                }
            }
            else -> {
                val eyeH = (if (mood == KadieMood.EXCITED) 4.2f else 3.6f) * u * blink.coerceAtLeast(0.06f)
                val eyeY = if (mood == KadieMood.FROWN) 23.6f else 23f
                listOf(26.5f, 37.5f).forEach { ex ->
                    drawOval(
                        accent,
                        topLeft = Offset(px(ex - 1.7f + lookX), py(eyeY) - eyeH / 2),
                        size = Size(3.4f * u, eyeH),
                    )
                }
            }
        }

        // ---- cheeks ----
        if (mood == KadieMood.HAPPY || mood == KadieMood.EXCITED) {
            drawCircle(cheek.copy(alpha = 0.55f), radius = 1.6f * u, center = Offset(px(23.5f), py(26.5f)))
            drawCircle(cheek.copy(alpha = 0.55f), radius = 1.6f * u, center = Offset(px(40.5f), py(26.5f)))
        }

        // ---- mouth ----
        when (mood) {
            KadieMood.HAPPY -> drawArc(
                lineColor, 10f, 160f, false,
                topLeft = Offset(px(27f), py(25.5f)),
                size = Size(10f * u, 6.5f * u),
                style = stroke,
            )
            KadieMood.EXCITED -> {
                // open smile
                drawArc(
                    lineColor, 10f, 160f, false,
                    topLeft = Offset(px(27.5f), py(25.5f)),
                    size = Size(9f * u, 6f * u),
                    style = stroke,
                )
                drawOval(
                    lineColor,
                    topLeft = Offset(px(30f), py(29.2f)),
                    size = Size(4f * u, 2.6f * u),
                )
            }
            KadieMood.FROWN -> drawArc(
                lineColor, 190f, 160f, false,
                topLeft = Offset(px(28f), py(29.5f)),
                size = Size(8f * u, 5f * u),
                style = stroke,
            )
            KadieMood.SLEEP -> drawCircle(
                lineColor, radius = 1.6f * u,
                center = Offset(px(32f), py(28.5f)),
                style = stroke,
            )
            KadieMood.IDLE -> line(lineColor, 29f, 28.5f, 35f, 28.5f)
        }

        // ---- body ----
        drawRoundRect(
            lineColor,
            topLeft = Offset(px(23.5f), py(35f)),
            size = Size(17f * u, 15f * u),
            cornerRadius = CornerRadius(6f * u, 6f * u),
            style = stroke,
        )
        // belly button light
        drawCircle(accent, radius = 1.8f * u, center = Offset(px(32f), py(42.5f)))

        // ---- arms ----
        when (mood) {
            KadieMood.EXCITED -> {
                line(lineColor, 23.5f, 38f, 19f, 33f - wave * 2.4f)
                line(lineColor, 40.5f, 38f, 45f, 33f - (1f - wave) * 2.4f)
            }
            KadieMood.HAPPY -> {
                line(lineColor, 23.5f, 38f, 18.5f, 31.5f)
                line(lineColor, 40.5f, 38f, 45.5f, 31.5f)
            }
            KadieMood.FROWN -> {
                line(lineColor, 23.5f, 38f, 21.5f, 45.5f)
                line(lineColor, 40.5f, 38f, 42.5f, 45.5f)
            }
            else -> {
                line(lineColor, 23.5f, 38f, 20.5f, 44f)
                line(lineColor, 40.5f, 38f, 43.5f, 44f)
            }
        }

        // ---- feet ----
        drawRoundRect(
            lineColor,
            topLeft = Offset(px(24.5f), py(51.5f)),
            size = Size(6.5f * u, 3.2f * u),
            cornerRadius = CornerRadius(1.6f * u, 1.6f * u),
        )
        drawRoundRect(
            lineColor,
            topLeft = Offset(px(33f), py(51.5f)),
            size = Size(6.5f * u, 3.2f * u),
            cornerRadius = CornerRadius(1.6f * u, 1.6f * u),
        )

        // ---- extras ----
        if (mood == KadieMood.HAPPY) {
            // sparkles
            val a = 0.35f + 0.65f * sparkle
            fun plus(cx: Float, cy: Float, r: Float) {
                line(accent.copy(alpha = a), cx - r, cy, cx + r, cy, strokeW * 0.8f)
                line(accent.copy(alpha = a), cx, cy - r, cx, cy + r, strokeW * 0.8f)
            }
            plus(14f, 18f, 2.2f)
            plus(51f, 12f, 1.8f)
            plus(52f, 30f, 1.5f)
        }
        if (mood == KadieMood.SLEEP) {
            // floating z's
            val za = 0.4f + 0.6f * sparkle
            val zStroke = strokeW * 0.8f
            fun zee(cx: Float, cy: Float, s: Float) {
                line(lineColor.copy(alpha = za), cx, cy, cx + s, cy, zStroke)
                line(lineColor.copy(alpha = za), cx + s, cy, cx, cy + s, zStroke)
                line(lineColor.copy(alpha = za), cx, cy + s, cx + s, cy + s, zStroke)
            }
            zee(46f, 8f + bob * 1.5f, 3.4f)
            zee(51f, 3f + bob * 1.5f, 2.4f)
        }
    }
}
