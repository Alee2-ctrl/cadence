package com.cadence.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.cadence.app.ui.TodayItem
import com.cadence.app.ui.theme.Bamboo
import com.cadence.app.ui.theme.CardWhite
import com.cadence.app.ui.theme.Faint
import com.cadence.app.ui.theme.Forest
import com.cadence.app.ui.theme.Honey
import com.cadence.app.ui.theme.Ink
import com.cadence.app.ui.theme.Leaf
import com.cadence.app.ui.theme.Mist
import com.cadence.app.ui.theme.Red
import com.cadence.app.ui.theme.SageDone
import com.cadence.app.ui.theme.SkyBlue
import com.cadence.app.ui.theme.TeaMist

val cardShape = RoundedCornerShape(24.dp)

@Composable
fun CircleCheckbox(checked: Boolean, accent: Color = Leaf, onClick: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (checked) accent else Color.Transparent)
            .clickable {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(28.dp)) {
            if (!checked) {
                drawCircle(
                    color = TeaMist,
                    radius = size.minDimension / 2 - 1.dp.toPx(),
                    style = Stroke(width = 2.dp.toPx()),
                )
            }
        }
        if (checked) {
            Canvas(modifier = Modifier.size(12.dp)) {
                val w = size.width
                val h = size.height
                drawLine(
                    Color.White,
                    Offset(w * 0.15f, h * 0.55f),
                    Offset(w * 0.42f, h * 0.8f),
                    strokeWidth = 2.dp.toPx(),
                )
                drawLine(
                    Color.White,
                    Offset(w * 0.42f, h * 0.8f),
                    Offset(w * 0.88f, h * 0.18f),
                    strokeWidth = 2.dp.toPx(),
                )
            }
        }
    }
}

@Composable
fun HabitCard(item: TodayItem.Habit, onToggle: () -> Unit, onEdit: () -> Unit) {
    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = if (item.done) SageDone else CardWhite,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    item.habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (item.done) Faint else Ink,
                    textDecoration = if (item.done) TextDecoration.LineThrough else null,
                )
                Text("Habit", style = MaterialTheme.typography.labelMedium, color = Leaf, fontWeight = FontWeight.SemiBold)
            }
            CircleCheckbox(checked = item.done, accent = Leaf, onClick = onToggle)
        }
    }
}

@Composable
fun RoutineCard(
    item: TodayItem.Routine,
    onToggleStep: (Int, Boolean) -> Unit,
    onEdit: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val allDone = item.steps.isNotEmpty() && item.doneSteps.containsAll(item.steps.indices.toList())
    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = if (allDone) Bamboo else CardWhite,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        item.routine.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Ink,
                    )
                    Text(
                        "Routine - ${item.doneSteps.count { it in item.steps.indices }}/${item.steps.size} steps",
                        style = MaterialTheme.typography.labelMedium,
                        color = Honey,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Icon(
                    if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Faint,
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "Edit",
                    color = Honey,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .clickable(onClick = onEdit)
                        .padding(4.dp),
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    item.steps.forEachIndexed { index, step ->
                        val stepDone = item.doneSteps.contains(index)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                        ) {
                            CircleCheckbox(
                                checked = stepDone,
                                accent = Honey,
                                onClick = { onToggleStep(index, !stepDone) },
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                step,
                                color = if (stepDone) Faint else Ink,
                                textDecoration = if (stepDone) TextDecoration.LineThrough else null,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BlinkingDot(color: Color, blinking: Boolean, size: Int) {
    val transition = rememberInfiniteTransition(label = "dot")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(650),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "alpha",
    )
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = if (blinking) alpha else 1f)),
    )
}

@Composable
fun TaskCard(item: TodayItem.Task, onToggle: () -> Unit, onEdit: () -> Unit) {
    val (dotColor, dotSize) = when (item.task.priority) {
        "HIGH" -> Red to 13
        "NORMAL" -> Honey to 10
        else -> TeaMist to 10
    }
    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = if (item.task.done) Mist else CardWhite,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BlinkingDot(color = dotColor, blinking = !item.task.done, size = dotSize)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    item.task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (item.task.done) Faint else Ink,
                    textDecoration = if (item.task.done) TextDecoration.LineThrough else null,
                )
                Text("Task", style = MaterialTheme.typography.labelMedium, color = SkyBlue, fontWeight = FontWeight.SemiBold)
            }
            CircleCheckbox(checked = item.task.done, accent = SkyBlue, onClick = onToggle)
        }
    }
}
