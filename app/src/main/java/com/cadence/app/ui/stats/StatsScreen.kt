package com.cadence.app.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cadence.app.ui.components.cardShape
import com.cadence.app.ui.theme.CardWhite
import com.cadence.app.ui.theme.Faint
import com.cadence.app.ui.theme.Forest
import com.cadence.app.ui.theme.Honey
import com.cadence.app.ui.theme.Ink
import com.cadence.app.ui.theme.Leaf
import com.cadence.app.ui.theme.Matcha
import com.cadence.app.ui.theme.Mist
import com.cadence.app.ui.theme.Paper
import com.cadence.app.ui.theme.Pistachio
import com.cadence.app.ui.theme.Red
import com.cadence.app.ui.theme.TeaMist
import java.time.LocalDate

private val MOODS = listOf(
    Triple("GREAT", "Great", Leaf),
    Triple("OKAY", "Okay", Honey),
    Triple("TOUGH", "Tough", Red),
)

@Composable
fun StatsScreen(modifier: Modifier = Modifier) {
    val vm: StatsViewModel = viewModel(factory = StatsViewModel.factory(LocalContext.current))
    val state by vm.state.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Paper),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 110.dp),
    ) {
        item {
            Text(
                "Insights",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Ink,
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatCard(
                    value = "${state.todayDone}/${state.todayTotal}",
                    label = "Today",
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    value = "${state.weekRate}%",
                    label = "This week",
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    value = state.totalCompletions.toString(),
                    label = "Check-offs",
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item { SectionTitle("Last 8 weeks") }
        item { WeeklyChart(rates = state.weeklyRates) }

        item { SectionTitle("Activity") }
        item { Heatmap(state = state) }

        item { SectionTitle("Habit streaks") }
        if (state.streaks.isEmpty()) {
            item {
                Text(
                    "Add a habit to start a streak.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Faint,
                )
            }
        } else {
            state.streaks.forEach { streak ->
                item { StreakRow(streak) }
            }
        }

        item { SectionTitle("Evening review") }
        item {
            EveningReviewCard(
                savedMood = state.review?.mood ?: "",
                savedNote = state.review?.note ?: "",
                onSave = { mood, note -> vm.saveReview(mood, note) },
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = Faint,
        modifier = Modifier.padding(top = 26.dp, bottom = 10.dp),
    )
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Ink,
            )
            Spacer(Modifier.height(2.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = Faint)
        }
    }
}

@Composable
private fun WeeklyChart(rates: List<Int>) {
    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            rates.forEachIndexed { index, rate ->
                val isLast = index == rates.lastIndex
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (rate > 0) {
                        Text(
                            "$rate",
                            style = MaterialTheme.typography.labelSmall,
                            color = Faint,
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((4 + rate).dp.coerceAtMost(96.dp))
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when {
                                    rate == 0 -> TeaMist
                                    isLast -> Leaf
                                    else -> Matcha
                                },
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun Heatmap(state: StatsUiState) {
    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            (0 until state.heatmapWeeks).forEach { week ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    (0..6).forEach { dow ->
                        val day = state.heatmapStart.plusDays((week * 7 + dow).toLong())
                        val count = state.heatmap[day.toEpochDay()] ?: 0
                        val future = day.isAfter(LocalDate.now())
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when {
                                        future -> Mist
                                        count == 0 -> TeaMist.copy(alpha = 0.45f)
                                        count == 1 -> Pistachio
                                        count == 2 -> Matcha
                                        count <= 4 -> Leaf
                                        else -> Forest
                                    },
                                ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StreakRow(streak: HabitStreak) {
    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (streak.days > 0) Leaf else TeaMist),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                streak.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Ink,
                modifier = Modifier.weight(1f),
            )
            Text(
                if (streak.days == 1) "1 day" else "${streak.days} days",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (streak.days > 0) Leaf else Faint,
            )
        }
    }
}

@Composable
private fun EveningReviewCard(
    savedMood: String,
    savedNote: String,
    onSave: (String, String) -> Unit,
) {
    var mood by remember(savedMood) { mutableStateOf(savedMood) }
    var note by remember(savedNote) { mutableStateOf(savedNote) }
    var justSaved by remember { mutableStateOf(false) }

    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = Pistachio),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "How was today?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Ink,
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MOODS.forEach { (key, label, color) ->
                    FilterChip(
                        selected = mood == key,
                        onClick = { mood = key; justSaved = false },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = CardWhite,
                            labelColor = Faint,
                            selectedContainerColor = color.copy(alpha = 0.25f),
                            selectedLabelColor = Ink,
                        ),
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it; justSaved = false },
                placeholder = { Text("One line about today...", color = Faint) },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = {
                        if (mood.isNotEmpty() || note.isNotBlank()) {
                            onSave(mood, note)
                            justSaved = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Forest,
                        contentColor = CardWhite,
                    ),
                ) {
                    Text("Save review", fontWeight = FontWeight.SemiBold)
                }
                if (justSaved) {
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Saved",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Leaf,
                    )
                }
            }
        }
    }
}
