package com.cadence.app.ui.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.cadence.app.ui.EditorState
import com.cadence.app.ui.TodayItem
import com.cadence.app.ui.components.HabitCard
import com.cadence.app.ui.components.RoutineCard
import com.cadence.app.ui.components.TaskCard
import com.cadence.app.ui.theme.CardWhite
import com.cadence.app.ui.theme.Faint
import com.cadence.app.ui.theme.Forest
import com.cadence.app.ui.theme.Ink
import com.cadence.app.ui.theme.Leaf
import com.cadence.app.ui.theme.Matcha
import com.cadence.app.ui.theme.Mist
import com.cadence.app.ui.theme.Paper
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
                .padding(horizontal = 20.dp),
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
                    onSelect = { vm.select(it) },
                )
            } else {
                WeekRow(
                    weekStart = weekStart,
                    selected = selectedDate,
                    taskDays = weekTaskDays,
                    onSelect = { vm.select(it) },
                )
            }

            Text(
                selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Faint,
                modifier = Modifier.padding(top = 18.dp, bottom = 10.dp),
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 110.dp),
            ) {
                if (items.isEmpty()) {
                    item {
                        Text(
                            "Nothing on this day.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Faint,
                            modifier = Modifier.padding(top = 24.dp),
                        )
                    }
                }
                items(
                    items,
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
            defaultDate = selectedDate.toEpochDay(),
            onDismiss = { showEditor = false },
            onSave = { vm.save(it); showEditor = false },
            onDelete = { vm.delete(it); showEditor = false },
        )
    }
}

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
