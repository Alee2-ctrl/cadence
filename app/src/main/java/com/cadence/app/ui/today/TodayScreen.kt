package com.cadence.app.ui.today

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cadence.app.modes.ModesManager
import com.cadence.app.ui.EditorState
import com.cadence.app.ui.TodayItem
import com.cadence.app.ui.TodayUiState
import com.cadence.app.ui.TodayViewModel
import com.cadence.app.ui.components.CadenceMenu
import com.cadence.app.ui.components.CadenceSheet
import com.cadence.app.ui.components.HabitCard
import com.cadence.app.ui.components.ItemActionsSheet
import com.cadence.app.ui.components.MenuEntry
import com.cadence.app.ui.components.RoutineCard
import com.cadence.app.ui.components.TaskCard
import com.cadence.app.ui.kadie.Kadie
import com.cadence.app.ui.kadie.KadieCommandSheet
import com.cadence.app.ui.kadie.KadieMood
import com.cadence.app.ui.theme.CardWhite
import com.cadence.app.ui.theme.Faint
import com.cadence.app.ui.theme.Forest
import com.cadence.app.ui.theme.Ink
import com.cadence.app.ui.theme.Leaf
import com.cadence.app.ui.theme.Matcha
import com.cadence.app.ui.theme.Paper
import com.cadence.app.ui.theme.Pistachio
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val SECTION_ORDER = listOf("MORNING", "AFTERNOON", "EVENING", "ANYTIME")

private val NOTHING_PHRASES = listOf(
    "Nothing's planned for today.",
    "A blank page. Nice.",
    "Your day is wide open.",
    "Room to breathe today.",
    "No plans. Pure space.",
    "Today is unwritten.",
)

private val ALLDONE_PHRASES = listOf(
    "All done. Kadie is thrilled.",
    "Everything checked off.",
    "A perfect sweep.",
    "You did the thing. All of it.",
    "Day complete. Savor it.",
)

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
            item {
                HeroRow(
                    mood = kadieMood,
                    done = state.doneCount,
                    total = state.totalCount,
                    overdue = overdue,
                    onKadieTap = { showKadie = true },
                )
            }

            if (state.items.isEmpty()) {
                item {
                    Text(
                        "Tap + to plan your first moment - or tap Kadie and say the word.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Faint,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 28.dp),
                    )
                }
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
                            onLongPress = { actionsFor = item },
                        )
                        is TodayItem.Routine -> RoutineCard(
                            item = item,
                            onToggleStep = { idx, done ->
                                vm.toggleRoutineStep(item.routine.id, idx, done, item.doneSteps)
                            },
                            onEdit = { editing = EditorState.from(item); showEditor = true },
                            onLongPress = { actionsFor = item },
                        )
                        is TodayItem.Task -> TaskCard(
                            item = item,
                            onToggle = { vm.toggleTask(item.task) },
                            onEdit = { editing = EditorState.from(item); showEditor = true },
                            onLongPress = { actionsFor = item },
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
            // Date: prominent but compact, leaf caps in contrast to the big ink header.
            Text(
                state.date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault()))
                    .uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Leaf,
                letterSpacing = 1.6.sp,
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
                    MenuEntry(Icons.Outlined.KeyboardArrowDown, "Import backup", onImport),
                    MenuEntry(Icons.Outlined.Info, "About Cadence", onAbout),
                ),
            )
        }
    }
}

@Composable
private fun HeroRow(
    mood: KadieMood,
    done: Int,
    total: Int,
    overdue: Int,
    onKadieTap: () -> Unit,
) {
    val caption = when (mood) {
        KadieMood.SLEEP -> "Kadie is sleeping. Shhh."
        KadieMood.IDLE -> "Kadie is around. Tap to talk."
        KadieMood.EXCITED -> "Kadie is pumped. Tap to talk."
        KadieMood.FROWN -> "$overdue overdue. Kadie noticed."
        KadieMood.HAPPY -> "Kadie is thrilled."
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Kadie's own card, forest like the nav bar.
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Forest),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .weight(1f)
                .height(184.dp)
                .clip(RoundedCornerShape(28.dp))
                .clickable(onClick = onKadieTap),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Kadie(
                    mood = mood,
                    modifier = Modifier.size(112.dp),
                    lineColor = Paper,
                    accent = Matcha,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    caption,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Paper.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                )
            }
        }

        // Companion card: phrases when empty / done, progress ring otherwise.
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .weight(1f)
                .height(184.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    total == 0 -> RotatingPhrases(phrases = NOTHING_PHRASES, color = Ink)
                    done >= total -> RotatingPhrases(phrases = ALLDONE_PHRASES, color = Leaf)
                    else -> ProgressRing(done = done, total = total)
                }
            }
        }
    }
}

@Composable
private fun RotatingPhrases(phrases: List<String>, color: Color) {
    var index by remember { mutableIntStateOf(0) }
    LaunchedEffect(phrases) {
        while (true) {
            delay(3400)
            index = (index + 1) % phrases.size
        }
    }
    Crossfade(targetState = index, animationSpec = tween(500), label = "phrase") { i ->
        Text(
            phrases[i],
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ProgressRing(done: Int, total: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(104.dp)) {
                val stroke = Stroke(width = 9.dp.toPx())
                val inset = 5.dp.toPx()
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
                    sweepAngle = 360f * (done.toFloat() / total.coerceAtLeast(1)),
                    useCenter = false,
                    style = stroke,
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - inset * 2, size.height - inset * 2),
                )
            }
            Text(
                "$done/$total",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Ink,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "done today",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Faint,
        )
    }
}
