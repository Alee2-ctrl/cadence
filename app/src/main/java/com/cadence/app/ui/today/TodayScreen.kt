package com.cadence.app.ui.today

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cadence.app.ui.EditorState
import com.cadence.app.ui.TodayItem
import com.cadence.app.ui.TodayUiState
import com.cadence.app.ui.TodayViewModel
import com.cadence.app.ui.components.HabitCard
import com.cadence.app.ui.components.KadiePod
import com.cadence.app.ui.components.RoutineCard
import com.cadence.app.ui.components.TaskCard
import com.cadence.app.ui.components.cardShape
import com.cadence.app.ui.kadie.Kadie
import com.cadence.app.ui.kadie.KadieMood
import com.cadence.app.ui.theme.CardWhite
import com.cadence.app.ui.theme.Faint
import com.cadence.app.ui.theme.Forest
import com.cadence.app.ui.theme.Ink
import com.cadence.app.ui.theme.Leaf
import com.cadence.app.ui.theme.Mist
import com.cadence.app.ui.theme.Paper
import com.cadence.app.ui.theme.Pistachio
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val SECTION_ORDER = listOf("MORNING", "AFTERNOON", "EVENING", "ANYTIME")

@Composable
fun TodayScreen(modifier: Modifier = Modifier) {
    val vm: TodayViewModel = viewModel(factory = TodayViewModel.factory(LocalContext.current))
    val state by vm.state.collectAsState()
    val backupStatus by vm.backupStatus.collectAsState()
    var editing by remember { mutableStateOf<EditorState?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> uri?.let { vm.exportBackup(it) } }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let { vm.importBackup(it) } }

    Scaffold(
        modifier = modifier,
        containerColor = Paper,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editing = null; showEditor = true },
                containerColor = Forest,
                contentColor = Color.White,
                shape = CircleShape,
                text = { Text("Add to today", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 110.dp),
        ) {
            item {
                TopBar(
                    state = state,
                    onExport = { exportLauncher.launch("cadence-backup.json") },
                    onImport = { importLauncher.launch(arrayOf("application/json")) },
                    onAbout = { showAbout = true },
                )
            }
            item { WeekStrip(today = state.date) }
            item { QuoteBanner(quote = Quotes.forToday()) }
            item {
                KadieSection(done = state.doneCount, total = state.totalCount)
            }

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
            defaultDate = state.date.toEpochDay(),
            onDismiss = { showEditor = false },
            onSave = { vm.save(it); showEditor = false },
            onDelete = { vm.delete(it); showEditor = false },
        )
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            containerColor = Paper,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Cadence - T4", fontWeight = FontWeight.Bold, color = Ink) },
            text = {
                Column {
                    Text(
                        "Habits, routines and tasks with reminders, backup, a planner and a little robot cheering you on. Fully offline.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink,
                    )
                    backupStatus?.let {
                        Spacer(Modifier.height(10.dp))
                        Text(it, style = MaterialTheme.typography.bodySmall, color = Faint)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) { Text("Close", color = Faint) }
            },
        )
    }
}

@Composable
private fun TopBar(
    state: TodayUiState,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onAbout: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                state.date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())),
                style = MaterialTheme.typography.bodyMedium,
                color = Faint,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "Today",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Ink,
            )
        }
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "Menu", tint = Faint)
            }
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
                containerColor = CardWhite,
            ) {
                DropdownMenuItem(
                    text = { Text("Export backup") },
                    onClick = { menuOpen = false; onExport() },
                )
                DropdownMenuItem(
                    text = { Text("Import backup") },
                    onClick = { menuOpen = false; onImport() },
                )
                DropdownMenuItem(
                    text = { Text("About Cadence") },
                    onClick = { menuOpen = false; onAbout() },
                )
            }
        }
    }
}

@Composable
private fun WeekStrip(today: LocalDate) {
    val monday = today.with(DayOfWeek.MONDAY)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        (0..6).forEach { offset ->
            val day = monday.plusDays(offset.toLong())
            val isToday = day == today
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    day.format(DateTimeFormatter.ofPattern("EEE", Locale.getDefault())),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isToday) Ink else Faint,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isToday) Forest else CardWhite),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        day.dayOfMonth.toString(),
                        color = if (isToday) Color.White else Faint,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun QuoteBanner(quote: String) {
    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = Pistachio),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
            Text(
                "Daily spark",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Leaf,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                quote,
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = Ink,
            )
        }
    }
}

@Composable
private fun KadieSection(done: Int, total: Int) {
    val mood = when {
        total == 0 -> KadieMood.SLEEP
        done >= total -> KadieMood.HAPPY
        else -> KadieMood.IDLE
    }
    val (headline, subline) = when (mood) {
        KadieMood.HAPPY -> "All clear!" to "Kadie is proud of you."
        KadieMood.IDLE -> "Keep going" to "Kadie is watching your progress."
        KadieMood.SLEEP -> "Quiet day" to "Kadie is recharging."
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KadiePod(
            mood = mood,
            done = done,
            total = total,
            modifier = Modifier.size(124.dp),
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                headline,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Ink,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "$done of $total done today",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Leaf,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                subline,
                style = MaterialTheme.typography.bodySmall,
                color = Faint,
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(Mist),
            contentAlignment = Alignment.Center,
        ) {
            Kadie(mood = KadieMood.SLEEP, modifier = Modifier.size(52.dp))
        }
        Spacer(Modifier.height(18.dp))
        Text(
            "Nothing scheduled",
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
