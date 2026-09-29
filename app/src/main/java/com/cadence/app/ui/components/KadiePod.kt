package com.cadence.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.cadence.app.ui.kadie.Kadie
import com.cadence.app.ui.kadie.KadieMood
import com.cadence.app.ui.theme.Leaf
import com.cadence.app.ui.theme.Mist
import com.cadence.app.ui.theme.TeaMist

@Composable
fun KadiePod(
    mood: KadieMood,
    done: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    val animated by animateFloatAsState(
        targetValue = if (total > 0) done.toFloat() / total else 0f,
        label = "podring",
    )
    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Mist),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = Stroke(width = 6.dp.toPx())
                val inset = 3.dp.toPx()
                drawArc(
                    color = TeaMist,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = stroke,
                    topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                    size = androidx.compose.ui.geometry.Size(
                        size.width - inset * 2,
                        size.height - inset * 2,
                    ),
                )
                drawArc(
                    color = Leaf,
                    startAngle = -90f,
                    sweepAngle = 360f * animated,
                    useCenter = false,
                    style = stroke,
                    topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                    size = androidx.compose.ui.geometry.Size(
                        size.width - inset * 2,
                        size.height - inset * 2,
                    ),
                )
            }
            Kadie(mood = mood, modifier = Modifier.size(62.dp))
        }
    }
}
