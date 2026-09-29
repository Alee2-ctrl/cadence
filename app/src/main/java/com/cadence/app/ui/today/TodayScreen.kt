package com.cadence.app.ui.today

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cadence.app.modes.ModesManager
import com.cadence.app.ui.EditorState
import com.cadence.app.ui.TodayItem
import com.cadence.app.ui.TodayUiState
import com.cadence.app.ui.TodayViewModel
import com.cadence.app.ui.components.BlinkingDot
import com.cadence.app.ui.components.CadenceMenu
import com.cadence.app.ui.components.CadenceSheet
import com.cadence.app.ui.components.CircleCheckbox
import com.cadence.app.ui.components.HabitColor
import com.cadence.app.ui.components.ItemActionsSheet
import com.cadence.app.ui.components.MenuEntry
import com.cadence.app.ui.components.RoutineColor
import com.cadence.app.ui.components.TaskColor
import com.cadence.app.ui.components.TypePill
import com.cadence.app.ui.kadie.Kadie
import com.cadence.app.ui.kadie.KadieCommandSheet
import com.cadence.app.ui.kadie.KadieMood
import com.cadence.app.ui.theme.Background
import com.cadence.app.ui.theme.Destructive
import com.cadence.app.ui.theme.Faint
import com.cadence.app.ui.theme.Forest
import com.cadence.app.ui.theme.Honey
import com.cadence.app.ui.theme.Ink
import com.cadence.app.ui.theme.Leaf
import com.cadence.app.ui.theme.Matcha
import com.cadence.app.ui.theme.Outline
import com.cadence.app.ui.theme.Paper
import com.cadence.app.ui.theme.Pistachio
import com.cadence.app.ui.theme.Radius
import com.cadence.app.ui.theme.Red
import com.cadence.app.ui.theme.SkyBlue
import com.cadence.app.ui.theme.Space
import com.cadence.app.ui.theme.Success
import com.cadence.app.ui.theme.Surface
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val SECTION_ORDER = listOf("MORNING", "AFTERNOON", "EVENING", "ANYTIME")

@Composable
fun TodayScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val vm: TodayViewModel = viewModel(factory = TodayViewModel.factory(context))
    val state by vm.state.collectAsState()
    val overdue by vm.overdueCount.collectAsState()
    val backupStatus by vm.backupStatus.collectAsState()
    var editing by remember { mutableStateOf<EditorState?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var showKadie by remember { mutableStateOf(false) }
    var actionsFor by remember { mutableStateOf<TodayItem?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> uri?.let { vm.exportBackup(it) } }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let { vm.importBackup(it) } }

    val forceSleep = ModesManager.activeModeName(context) == "Bedtime"
    val kadieMood = when {
        forceSleep -> KadieMood.SLEEP
        state.totalCount == 0 && overdue == 0 -> KadieMood.IDLE
        overdue > 0 -> KadieMood.FROWN
        state.doneCount >= state.totalCount -> KadieMood.HAPPY
        else -> KadieMood.EXCITED
    }

    // Real state only: counts derived from today's items, never fabricated.
    val remaining = state.totalCount - state.doneCount
    val habits = state.items.filterIsInstance<TodayItem.Habit>()
    val habitsDone = habits.count { it.done }

    val greeting = when (java.time.LocalTime.now().hour) {
        in 5..11 -> "Good morning!"
        in 12..17 -> "Good afternoon!"
        else -> "Good evening!"
    }
    val (heroTitle, heroStatus) = when {
        forceSleep ->
            "Sleep tight." to "Bedtime mode is on. Kadie is resting."
        state.totalCount == 0 && overdue == 0 ->
            "We can begin small." to "Your day is open."
        state.totalCount > 0 && state.doneCount >= state.totalCount && overdue == 0 ->
            "You did it!" to "Everything is checked off."
        state.totalCount == 0 ->
            greeting to "$overdue ${if (overdue == 1) "task" else "tasks"} overdue from earlier days."
        else ->
            greeting to buildString {
                append("$remaining ${if (remaining == 1) "thing is" else "things are"} waiting today")
                if (overdue > 0) append(" · $overdue overdue")
                append(".")
            }
    }

    Scaffold(
        modifier = modifier,
        containerColor = Background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editing = null; showEditor = true },
                shape = CircleShape,
                containerColor = Forest,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add to today")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = Space.ScreenMargin,
                end = Space.ScreenMargin,
                top = Space.Small,
                bottom = 120.dp,
            ),
        ) {
            item {
                TodayHeader(
                    state = state,
                    onExport = { exportLauncher.launch("cadence-backup.json") },
                    onImport = { importLauncher.launch(arrayOf("application/json")) },
                    onAbout = { showAbout = true },
                )
            }
            item {
                KadieHero(
                    mood = kadieMood,
                    title = heroTitle,
                    status = heroStatus,
                    onTap = { showKadie = true },
                    modifier = Modifier.padding(top = Space.Standard),
                )
            }
            item {
                MetricsRow(
                    done = state.doneCount,
                    total = state.totalCount,
                    habitsDone = habitsDone,
                    habitsTotal = habits.size,
                    overdue = overdue,
                    modifier = Modifier.padding(top = Space.Compact),
                )
            }

            if (state.items.isEmpty()) {
                item { AgendaEmpty(modifier = Modifier.padding(top = Space.ScreenMargin)) }
            }

            SECTION_ORDER.forEach { section ->
                val sectionItems = state.items.filter { it.timeOfDay == section }
                if (sectionItems.isEmpty()) return@forEach
                item {
                    Text(
                        text = if (section == "ANYTIME") "Up next"
                        else section.lowercase().replaceFirstChar { it.uppercase(Locale.getDefault()) },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Ink,
                        modifier = Modifier.padding(top = Space.ScreenMargin, bottom = Space.Small),
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
                        is TodayItem.Habit -> HabitRow(
                            item = item,
                            onToggle = { vm.toggleHabit(item.habit.id, !item.done) },
                            onEdit = { editing = EditorState.from(item); showEditor = true },
                            onLongPress = { actionsFor = item },
                        )
                        is TodayItem.Routine -> RoutineRow(
                            item = item,
                            onToggleStep = { idx, done ->
                                vm.toggleRoutineStep(item.routine.id, idx, done, item.doneSteps)
                            },
                            onEdit = { editing = EditorState.from(item); showEditor = true },
                            onLongPress = { actionsFor = item },
                        )
                        is TodayItem.Task -> TaskRow(
                            item = item,
                            onToggle = { vm.toggleTask(item.task) },
                            onEdit = { editing = EditorState.from(item); showEditor = true },
                            onLongPress = { actionsFor = item },
                        )
                    }
                    Spacer(Modifier.height(Space.Small))
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

    if (showKadie) {
        KadieCommandSheet(onDismiss = { showKadie = false })
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
                showEditor = true
            },
            onDelete = {
                vm.delete(EditorState.from(item))
                actionsFor = null
            },
            onDismiss = { actionsFor = null },
        )
    }

    if (showAbout) {
        CadenceSheet(
            onDismiss = { showAbout = false },
            label = "T9.1",
            title = "Cadence",
            subtitle = "Fully offline. Yours alone.",
        ) {
            Text(
                "Habits, routines, tasks, notes, stats, modes and an app lockout - with a little robot cheering you on. Tap Kadie and give orders.",
                style = MaterialTheme.typography.bodyMedium,
                color = Ink,
            )
            backupStatus?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = Faint)
            }
            Spacer(Modifier.height(14.dp))
            TextButton(
                onClick = { showAbout = false },
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) { Text("Close", color = Faint, fontWeight = FontWeight.SemiBold) }
        }
    }
}

// ---- header ---------------------------------------------------------------

@Composable
private fun TodayHeader(
    state: TodayUiState,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onAbout: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Space.Compact),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                state.date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault()))
                    .uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Success,
                letterSpacing = 1.5.sp,
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
            CadenceMenu(
                expanded = menuOpen,
                onDismiss = { menuOpen = false },
                entries = listOf(
                    MenuEntry(Icons.Outlined.Share, "Export backup", onExport),
                    MenuEntry(Icons.Filled.KeyboardArrowDown, "Import backup", onImport),
                    MenuEntry(Icons.Outlined.Info, "About Cadence", onAbout),
                ),
            )
        }
    }
}

// ---- Kadie hero -----------------------------------------------------------

@Composable
private fun KadieHero(
    mood: KadieMood,
    title: String,
    status: String,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(Radius.Hero),
        colors = CardDefaults.cardColors(containerColor = Forest),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.Hero))
            .clickable(onClick = onTap),
    ) {
        Row(
            modifier = Modifier.padding(Space.CardPad),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Kadie(
                mood = mood,
                modifier = Modifier.size(88.dp),
                lineColor = Paper,
                accent = Matcha,
            )
            Spacer(Modifier.width(Space.Standard))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Paper,
                )
                Spacer(Modifier.height(Space.Micro))
                Text(
                    status,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Paper.copy(alpha = 0.8f),
                )
            }
            Spacer(Modifier.width(Space.Small))
            Icon(
                Icons.Outlined.KeyboardArrowRight,
                contentDescription = "Talk to Kadie",
                tint = Paper.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

// ---- daily metrics --------------------------------------------------------

@Composable
private fun MetricsRow(
    done: Int,
    total: Int,
    habitsDone: Int,
    habitsTotal: Int,
    overdue: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Space.Compact),
    ) {
        MetricCard(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MiniRing(
                    progress = if (total > 0) done.toFloat() / total else 0f,
                    modifier = Modifier.size(40.dp),
                )
                Spacer(Modifier.width(Space.Small))
                Column {
                    Text(
                        "$done/$total",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                    )
                    Text("Done today", style = MaterialTheme.typography.labelSmall, color = Faint)
                }
            }
        }
        MetricCard(Modifier.weight(1f)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (habitsTotal > 0) "$habitsDone/$habitsTotal" else "-",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                )
                Spacer(Modifier.height(2.dp))
                Text("Habits today", style = MaterialTheme.typography.labelSmall, color = Faint)
            }
        }
        MetricCard(Modifier.weight(1f)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "$overdue",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (overdue > 0) Destructive else Ink,
                )
                Spacer(Modifier.height(2.dp))
                Text("Overdue", style = MaterialTheme.typography.labelSmall, color = Faint)
            }
        }
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(Radius.CompactCard),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.height(76.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(Space.Compact),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

@Composable
private fun MiniRing(progress: Float, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val stroke = Stroke(width = 5.dp.toPx())
        val inset = 3.dp.toPx()
        drawArc(
            color = Pistachio,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = stroke,
            topLeft = Offset(inset, inset),
            size = Size(size.width - inset * 2, size.height - inset * 2),
        )
        drawArc(
            color = Leaf,
            startAngle = -90f,
            sweepAngle = 360f * progress.coerceIn(0f, 1f),
            useCenter = false,
            style = stroke,
            topLeft = Offset(inset, inset),
            size = Size(size.width - inset * 2, size.height - inset * 2),
        )
    }
}

// ---- empty state ----------------------------------------------------------

@Composable
private fun AgendaEmpty(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        Text(
            "Your agenda",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Ink,
        )
        Card(
            shape = RoundedCornerShape(Radius.Card),
            colors = CardDefaults.cardColors(containerColor = Surface),
            border = BorderStroke(1.dp, Outline),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Space.Compact),
        ) {
            Column(modifier = Modifier.padding(Space.CardPad)) {
                Text(
                    "Nothing planned yet",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink,
                )
                Spacer(Modifier.height(Space.Micro))
                Text(
                    "Add a task, habit or routine when you're ready.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Faint,
                )
            }
        }
    }
}

// ---- compact item rows -----------------------------------------------------

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RowCard(
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    content: @Composable () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    Card(
        shape = RoundedCornerShape(Radius.CompactCard),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.CompactCard))
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongPress()
                },
            ),
    ) {
        content()
    }
}

// Visible circle stays small; the touch target is a full 48dp box.
@Composable
private fun CheckboxTarget(
    checked: Boolean,
    accent: Color,
    onToggle: () -> Unit,
) {
    Box(
        modifier = Modifier.size(48.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircleCheckbox(checked = checked, accent = accent, onClick = onToggle)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HabitRow(
    item: TodayItem.Habit,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onLongPress: () -> Unit,
) {
    RowCard(onClick = onEdit, onLongPress = onLongPress) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = Space.Standard),
        ) {
            CheckboxTarget(checked = item.done, accent = Leaf, onToggle = onToggle)
            Column(Modifier.weight(1f).padding(vertical = Space.Compact)) {
                Text(
                    item.habit.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (item.done) Faint else Ink,
                    textDecoration = if (item.done) TextDecoration.LineThrough else null,
                )
                TypePill("Habit", HabitColor)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TaskRow(
    item: TodayItem.Task,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onLongPress: () -> Unit,
) {
    val (dotColor, dotSize) = when (item.task.priority) {
        "HIGH" -> Red to 12
        "NORMAL" -> Honey to 9
        else -> Pistachio to 9
    }
    RowCard(onClick = onEdit, onLongPress = onLongPress) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = Space.Standard),
        ) {
            CheckboxTarget(checked = item.task.done, accent = SkyBlue, onToggle = onToggle)
            Column(Modifier.weight(1f).padding(vertical = Space.Compact)) {
                Text(
                    item.task.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (item.task.done) Faint else Ink,
                    textDecoration = if (item.task.done) TextDecoration.LineThrough else null,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TypePill("Task", TaskColor)
                    if (item.task.reminderMin >= 0) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Outlined.Notifications,
                            contentDescription = "Reminder set",
                            tint = Faint,
                            modifier = Modifier
                                .padding(top = 5.dp)
                                .size(14.dp),
                        )
                    }
                }
            }
            BlinkingDot(color = dotColor, blinking = !item.task.done, size = dotSize)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RoutineRow(
    item: TodayItem.Routine,
    onToggleStep: (Int, Boolean) -> Unit,
    onEdit: () -> Unit,
    onLongPress: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    RowCard(onClick = { expanded = !expanded }, onLongPress = onLongPress) {
        Column(modifier = Modifier.padding(end = Space.Standard)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Routines complete step-by-step; tapping the row expands.
                Spacer(Modifier.width(Space.Standard))
                Column(Modifier.weight(1f).padding(vertical = Space.Compact)) {
                    Text(
                        item.routine.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Ink,
                    )
                    TypePill(
                        "Routine - ${item.doneSteps.count { it in item.steps.indices }}/${item.steps.size} steps",
                        RoutineColor,
                    )
                }
                Icon(
                    if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Faint,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Edit",
                    color = Honey,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.Tag))
                        .clickable(onClick = onEdit)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(bottom = Space.Small)) {
                    item.steps.forEachIndexed { index, step ->
                        val stepDone = item.doneSteps.contains(index)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            CheckboxTarget(
                                checked = stepDone,
                                accent = Honey,
                                onToggle = { onToggleStep(index, !stepDone) },
                            )
                            Text(
                                step,
                                color = if (stepDone) Faint else Ink,
                                textDecoration = if (stepDone) TextDecoration.LineThrough else null,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
        }
    }
}
