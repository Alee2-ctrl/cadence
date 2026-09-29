package com.cadence.app.ui.plan

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.cadence.app.ui.EditorState
import com.cadence.app.ui.TodayItem
import com.cadence.app.ui.components.CadenceSheet
import com.cadence.app.ui.components.ItemActionsSheet
import com.cadence.app.ui.components.SheetPillButton
import com.cadence.app.ui.theme.CardWhite
import com.cadence.app.ui.theme.Faint
import com.cadence.app.ui.theme.Forest
import com.cadence.app.ui.theme.Honey
import com.cadence.app.ui.theme.Ink
import com.cadence.app.ui.theme.Leaf
import com.cadence.app.ui.theme.Matcha
import com.cadence.app.ui.theme.Mist
import com.cadence.app.ui.theme.Paper
import com.cadence.app.ui.theme.SkyBlue
import com.cadence.app.ui.today.ItemEditorDialog
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PlanScreen(modifier: Modifier = Modifier) {
    val vm: PlanViewModel = viewModel(factory = PlanViewModel.factory(LocalContext.current))
    val selectedDate by vm.selectedDate.collectAsState()
    val weekStart by vm.weekStart.collectAsState()
    val monthMode by vm.monthMode.collectAsState()
    val items by vm.items.collectAsState()
    val weekTaskDays by vm.weekTaskDays.collectAsState()
    val monthTaskDays by vm.monthTaskDays.collectAsState()
    var editing by remember { mutableStateOf<EditorState?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var showDaySheet by remember { mutableStateOf(false) }
    var actionsFor by remember { mutableStateOf<TodayItem?>(null) }

    Scaffold(
        modifier = modifier,
        containerColor = Paper,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editing = null; showEditor = true },
                containerColor = Forest,
                contentColor = Color.White,
                shape = CircleShape,
                text = { Text("Add", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Plan",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                    modifier = Modifier.weight(1f),
                )
                FilterChip(
                    selected = !monthMode,
                    onClick = { vm.monthMode.value = false },
                    label = { Text("Week") },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = CardWhite,
                        labelColor = Faint,
                        selectedContainerColor = Mist,
                        selectedLabelColor = Ink,
                    ),
                )
                Spacer(Modifier.size(8.dp))
                FilterChip(
                    selected = monthMode,
                    onClick = { vm.monthMode.value = true },
                    label = { Text("Month") },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = CardWhite,
                        labelColor = Faint,
                        selectedContainerColor = Mist,
                        selectedLabelColor = Ink,
                    ),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = { if (monthMode) vm.prevMonth() else vm.prevWeek() }) {
                    Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Previous", tint = Ink)
                }
                Text(
                    if (monthMode)
                        selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))
                    else
                        weekStart.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())) +
                            " - " + weekStart.plusDays(6).format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink,
                )
                IconButton(onClick = { if (monthMode) vm.nextMonth() else vm.nextWeek() }) {
                    Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Next", tint = Ink)
                }
            }

            if (monthMode) {
                MonthGrid(
                    month = selectedDate,
                    selected = selectedDate,
                    taskDays = monthTaskDays,
                    onSelect = { vm.select(it); showDaySheet = true },
                )
            } else {
                WeekRow(
                    weekStart = weekStart,
                    selected = selectedDate,
                    taskDays = weekTaskDays,
                    onSelect = { vm.select(it); showDaySheet = true },
                )
            }

            Spacer(Modifier.height(28.dp))
            Text(
                "Tap a day to see what's planned.",
                style = MaterialTheme.typography.bodyMedium,
                color = Faint,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                textAlign = TextAlign.Center,
            )
        }
    }

    if (showDaySheet) {
        DaySheet(
            date = selectedDate,
            items = items,
            onDismiss = { showDaySheet = false },
            onAdd = {
                showDaySheet = false
                editing = null
                showEditor = true
            },
            onEditItem = { item ->
                showDaySheet = false
                editing = EditorState.from(item)
                showEditor = true
            },
            onLongPressItem = { item -> actionsFor = item },
        )
    }

    actionsFor?.let { item ->
        ItemActionsSheet(
            name = when (item) {
                is TodayItem.Habit -> item.habit.name
                is TodayItem.Routine -> item.routine.name
                is TodayItem.Task -> item.task.title
            },
            kind = when (item) {
                is TodayItem.Habit -> "Habit"
                is TodayItem.Routine -> "Routine"
                is TodayItem.Task -> "Task"
            },
            onEdit = {
                editing = EditorState.from(item)
                actionsFor = null
                showDaySheet = false
                showEditor = true
            },
            onDelete = {
                vm.delete(EditorState.from(item))
                actionsFor = null
            },
            onDismiss = { actionsFor = null },
        )
    }

    if (showEditor) {
        ItemEditorDialog(
            initial = editing,
            defaultDate = selectedDate.toEpochDay(),
            onDismiss = { showEditor = false },
            onSave = { vm.save(it); showEditor = false },
            onDelete = { vm.delete(it); showEditor = false },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DaySheet(
    date: LocalDate,
    items: List<TodayItem>,
    onDismiss: () -> Unit,
    onAdd: () -> Unit,
    onEditItem: (TodayItem) -> Unit,
    onLongPressItem: (TodayItem) -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val isToday = date == LocalDate.now()
    CadenceSheet(
        onDismiss = onDismiss,
        label = date.format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault())),
        title = if (items.isEmpty()) {
            if (isToday) "Planning something for today?" else "Planning something for this day?"
        } else {
            if (isToday) "Today's plan" else "Planned for this day"
        },
        subtitle = if (items.isEmpty()) "Nothing here yet. Start with one small thing."
        else "${items.size} item" + (if (items.size == 1) "" else "s"),
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            items.forEach { item ->
                val (name, kind, accent, done) = when (item) {
                    is TodayItem.Habit -> Quad(item.habit.name, "Habit", Leaf, item.done)
                    is TodayItem.Routine -> Quad(
                        item.routine.name,
                        "Routine",
                        Honey,
                        item.steps.isNotEmpty() &&
                            item.doneSteps.containsAll(item.steps.indices.toList()),
                    )
                    is TodayItem.Task -> Quad(item.task.title, "Task", SkyBlue, item.task.done)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(CardWhite)
                        .combinedClickable(
                            onClick = { onEditItem(item) },
                            onLongClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onLongPressItem(item)
                            },
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(accent),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (done) Faint else Ink,
                            textDecoration = if (done) TextDecoration.LineThrough else null,
                        )
                        Text(
                            kind + if (done) " - done" else "",
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            SheetPillButton(
                text = if (items.isEmpty()) "Plan something" else "Add another",
                onClick = onAdd,
            )
        }
    }
}

private data class Quad(val name: String, val kind: String, val accent: Color, val done: Boolean)

@Composable
private fun WeekRow(
    weekStart: LocalDate,
    selected: LocalDate,
    taskDays: Set<Long>,
    onSelect: (LocalDate) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        (0..6).forEach { offset ->
            val day = weekStart.plusDays(offset.toLong())
            DayCell(
                day = day,
                selected = day == selected,
                hasTasks = taskDays.contains(day.toEpochDay()),
                onSelect = onSelect,
            )
        }
    }
}

@Composable
private fun DayCell(
    day: LocalDate,
    selected: Boolean,
    hasTasks: Boolean,
    onSelect: (LocalDate) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(CircleShape)
            .clickable { onSelect(day) },
    ) {
        Text(
            day.format(DateTimeFormatter.ofPattern("EEE", Locale.getDefault())),
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) Ink else Faint,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (selected) Forest else CardWhite),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                day.dayOfMonth.toString(),
                color = if (selected) Color.White else Ink,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(if (hasTasks) Matcha else Color.Transparent),
        )
    }
}

@Composable
private fun MonthGrid(
    month: LocalDate,
    selected: LocalDate,
    taskDays: Set<Long>,
    onSelect: (LocalDate) -> Unit,
) {
    val firstOfMonth = month.withDayOfMonth(1)
    val leadingBlanks = firstOfMonth.dayOfWeek.value - 1
    val daysInMonth = month.lengthOfMonth()
    val rows = (leadingBlanks + daysInMonth + 6) / 7

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { label ->
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = Faint,
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        (0 until rows).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                (0..6).forEach { col ->
                    val index = row * 7 + col - leadingBlanks
                    if (index < 0 || index >= daysInMonth) {
                        Spacer(Modifier.weight(1f))
                    } else {
                        val day = firstOfMonth.plusDays(index.toLong())
                        val isSelected = day == selected
                        val hasTasks = taskDays.contains(day.toEpochDay())
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Forest else Color.Transparent)
                                .clickable { onSelect(day) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    day.dayOfMonth.toString(),
                                    color = if (isSelected) Color.White else Ink,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                )
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (hasTasks) (if (isSelected) Color.White else Matcha)
                                            else Color.Transparent,
                                        ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
