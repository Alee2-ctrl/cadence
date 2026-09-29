package com.cadence.app.ui.today

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cadence.app.ui.EditorState
import com.cadence.app.ui.TodayItem
import com.cadence.app.ui.TodayUiState
import com.cadence.app.ui.TodayViewModel
import com.cadence.app.ui.theme.Butter
import com.cadence.app.ui.theme.CardWhite
import com.cadence.app.ui.theme.Clay
import com.cadence.app.ui.theme.Faint
import com.cadence.app.ui.theme.Ink
import com.cadence.app.ui.theme.Lilac
import com.cadence.app.ui.theme.Paper
import com.cadence.app.ui.theme.Sage
import com.cadence.app.ui.theme.Soft
import com.cadence.app.ui.theme.Stone
import java.time.format.DateTimeFormatter
import java.util.Locale

private val SECTION_ORDER = listOf("MORNING", "AFTERNOON", "EVENING", "ANYTIME")
private val cardShape = RoundedCornerShape(24.dp)

@Composable
fun TodayScreen(modifier: Modifier = Modifier) {
    val vm: TodayViewModel = viewModel(factory = TodayViewModel.factory(LocalContext.current))
    val state by vm.state.collectAsState()
    var editing by remember { mutableStateOf<EditorState?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = Paper,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editing = null; showEditor = true },
                containerColor = Clay,
                contentColor = Color.White,
                text = { Text("Add to today", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 96.dp),
        ) {
            item { Header(state) }

            if (state.items.isEmpty()) {
                item { EmptyState() }
            }

            SECTION_ORDER.forEach { section ->
                val sectionItems = state.items.filter { it.timeOfDay == section }
                if (sectionItems.isEmpty()) return@forEach
                item {
                    Text(
                        text = section.lowercase().replaceFirstChar { it.uppercase(Locale.getDefault()) },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Faint,
                        modifier = Modifier.padding(top = 24.dp, bottom = 10.dp),
                    )
                }
                items(
                    sectionItems,
                    key = {
                        when (it) {
                            is TodayItem.Habit -> "h" + it.habit.id
                            is TodayItem.Routine -> "r" + it.routine.id
                            is TodayItem.Task -> "t" + it.task.id
                        }
                    },
                ) { item ->
                    when (item) {
                        is TodayItem.Habit -> HabitCard(
                            item = item,
                            onToggle = { vm.toggleHabit(item.habit.id, !item.done) },
                            onEdit = { editing = EditorState.from(item); showEditor = true },
                        )
                        is TodayItem.Routine -> RoutineCard(
                            item = item,
                            onToggleStep = { idx, done ->
                                vm.toggleRoutineStep(item.routine.id, idx, done, item.doneSteps)
                            },
                            onEdit = { editing = EditorState.from(item); showEditor = true },
                        )
                        is TodayItem.Task -> TaskCard(
                            item = item,
                            onToggle = { vm.toggleTask(item.task) },
                            onEdit = { editing = EditorState.from(item); showEditor = true },
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }

    if (showEditor) {
        ItemEditorDialog(
            initial = editing,
            onDismiss = { showEditor = false },
            onSave = { vm.save(it); showEditor = false },
            onDelete = { vm.delete(it); showEditor = false },
        )
    }
}

@Composable
private fun Header(state: TodayUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                state.date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())),
                style = MaterialTheme.typography.bodyMedium,
                color = Faint,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Today",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Ink,
            )
        }
        ProgressRing(done = state.doneCount, total = state.totalCount)
    }
}

@Composable
private fun ProgressRing(done: Int, total: Int) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(72.dp)) {
        Canvas(modifier = Modifier.size(72.dp)) {
            val stroke = Stroke(width = 8.dp.toPx())
            drawArc(color = Stone, startAngle = 0f, sweepAngle = 360f, useCenter = false, style = stroke)
            if (total > 0) {
                drawArc(
                    color = Clay,
                    startAngle = -90f,
                    sweepAngle = 360f * done / total,
                    useCenter = false,
                    style = stroke,
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "$done/$total",
                fontWeight = FontWeight.Bold,
                color = Ink,
                style = MaterialTheme.typography.titleSmall,
            )
            Text("done", color = Faint, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(Soft),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(44.dp)) {
                val r = size.minDimension / 2
                drawRoundRect(
                    color = Ink,
                    topLeft = Offset(r * 0.3f, r * 0.45f),
                    size = androidx.compose.ui.geometry.Size(r * 1.4f, r * 1.1f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(r * 0.3f, r * 0.3f),
                    style = Stroke(width = r * 0.12f),
                )
                drawCircle(color = Clay, radius = r * 0.1f, center = Offset(r * 0.8f, r * 0.95f))
                drawCircle(color = Clay, radius = r * 0.1f, center = Offset(r * 1.2f, r * 0.95f))
                drawLine(
                    color = Ink,
                    start = Offset(r, r * 0.45f),
                    end = Offset(r, r * 0.12f),
                    strokeWidth = r * 0.12f,
                )
                drawCircle(color = Clay, radius = r * 0.14f, center = Offset(r, r * 0.08f))
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "A quiet day",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Ink,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Tap + to add your first habit, routine or task.",
            color = Faint,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun CircleCheckbox(checked: Boolean, accent: Color = Clay, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (checked) accent else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(28.dp)) {
            if (!checked) {
                drawCircle(
                    color = Stone,
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
private fun HabitCard(item: TodayItem.Habit, onToggle: () -> Unit, onEdit: () -> Unit) {
    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = if (item.done) Sage.copy(alpha = 0.35f) else CardWhite,
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
                    color = Ink,
                )
                Text("Habit", style = MaterialTheme.typography.labelMedium, color = Faint)
            }
            CircleCheckbox(checked = item.done, onClick = onToggle)
        }
    }
}

@Composable
private fun RoutineCard(
    item: TodayItem.Routine,
    onToggleStep: (Int, Boolean) -> Unit,
    onEdit: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val allDone = item.steps.isNotEmpty() && item.doneSteps.containsAll(item.steps.indices.toList())
    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = if (allDone) Lilac.copy(alpha = 0.30f) else CardWhite,
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
                        color = Faint,
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
                    color = Clay,
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
                                accent = Lilac,
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
private fun TaskCard(item: TodayItem.Task, onToggle: () -> Unit, onEdit: () -> Unit) {
    val priorityColor = when (item.task.priority) {
        "HIGH" -> Clay
        "NORMAL" -> Butter
        else -> Sage
    }
    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(priorityColor),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    item.task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (item.task.done) Faint else Ink,
                    textDecoration = if (item.task.done) TextDecoration.LineThrough else null,
                )
                Text("Task", style = MaterialTheme.typography.labelMedium, color = Faint)
            }
            CircleCheckbox(checked = item.task.done, onClick = onToggle)
        }
    }
}
