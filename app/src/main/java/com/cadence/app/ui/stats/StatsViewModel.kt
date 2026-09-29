package com.cadence.app.ui.stats

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cadence.app.data.CadenceDatabase
import com.cadence.app.data.CadenceRepository
import com.cadence.app.data.HabitEntity
import com.cadence.app.data.HabitLogEntity
import com.cadence.app.data.ReviewEntity
import com.cadence.app.data.RoutineEntity
import com.cadence.app.data.RoutineLogEntity
import com.cadence.app.data.TaskEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

data class HabitStreak(val name: String, val days: Int)

data class StatsUiState(
    val todayDone: Int = 0,
    val todayTotal: Int = 0,
    val weekRate: Int = 0,
    val totalCompletions: Int = 0,
    val weeklyRates: List<Int> = List(8) { 0 },
    val heatmap: Map<Long, Int> = emptyMap(),
    val heatmapStart: LocalDate = LocalDate.now(),
    val heatmapWeeks: Int = 18,
    val streaks: List<HabitStreak> = emptyList(),
    val review: ReviewEntity? = null,
)

class StatsViewModel(private val repo: CadenceRepository) : ViewModel() {

    private val today: LocalDate = LocalDate.now()
    private val heatmapStart: LocalDate = today.minusWeeks(17).with(DayOfWeek.MONDAY)
    private val rangeStart: Long = heatmapStart.toEpochDay()
    private val rangeEnd: Long = today.toEpochDay()

    private val core = combine(
        repo.habits,
        repo.routines,
        repo.habitLogsBetween(rangeStart, rangeEnd),
        repo.routineLogsBetween(rangeStart, rangeEnd),
        repo.tasksBetween(rangeStart, rangeEnd),
    ) { habits, routines, habitLogs, routineLogs, tasks ->
        compute(habits, routines, habitLogs, routineLogs, tasks)
    }

    val state: StateFlow<StatsUiState> =
        combine(core, repo.reviewFor(rangeEnd)) { stats, review ->
            stats.copy(review = review)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsUiState())

    private fun isScheduled(mask: Int, date: LocalDate): Boolean =
        mask and (1 shl (date.dayOfWeek.value - 1)) != 0

    private fun routineDone(routine: RoutineEntity, log: RoutineLogEntity?): Boolean {
        if (log == null) return false
        val steps = routine.steps.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        val done = log.doneSteps.split(",").mapNotNull { it.toIntOrNull() }.toSet()
        return if (steps.isEmpty()) done.isNotEmpty() else done.size >= steps.size
    }

    private fun compute(
        habits: List<HabitEntity>,
        routines: List<RoutineEntity>,
        habitLogs: List<HabitLogEntity>,
        routineLogs: List<RoutineLogEntity>,
        tasks: List<TaskEntity>,
    ): StatsUiState {
        val habitDoneByDate = habitLogs.filter { it.done }.groupBy { it.date }
        val routineLogByKey = routineLogs.associateBy { it.routineId to it.date }
        val tasksByDate = tasks.groupBy { it.date }

        // Today
        var todayTotal = 0
        var todayDone = 0
        habits.filter { isScheduled(it.daysMask, today) }.forEach { h ->
            todayTotal++
            if (habitDoneByDate[rangeEnd]?.any { it.habitId == h.id } == true) todayDone++
        }
        routines.filter { isScheduled(it.daysMask, today) }.forEach { r ->
            todayTotal++
            if (routineDone(r, routineLogByKey[r.id to rangeEnd])) todayDone++
        }
        tasksByDate[rangeEnd]?.forEach { t ->
            todayTotal++
            if (t.done) todayDone++
        }

        // Weekly rates (last 8 weeks, oldest first)
        val thisMonday = today.with(DayOfWeek.MONDAY)
        val weeklyRates = (7 downTo 0).map { w ->
            val monday = thisMonday.minusWeeks(w.toLong())
            var scheduled = 0
            var done = 0
            (0..6).forEach { offset ->
                val day = monday.plusDays(offset.toLong())
                if (day.isAfter(today)) return@forEach
                val epoch = day.toEpochDay()
                habits.filter { isScheduled(it.daysMask, day) }.forEach { h ->
                    scheduled++
                    if (habitDoneByDate[epoch]?.any { it.habitId == h.id } == true) done++
                }
                routines.filter { isScheduled(it.daysMask, day) }.forEach { r ->
                    scheduled++
                    if (routineDone(r, routineLogByKey[r.id to epoch])) done++
                }
                tasksByDate[epoch]?.forEach { t ->
                    scheduled++
                    if (t.done) done++
                }
            }
            if (scheduled == 0) 0 else (done * 100) / scheduled
        }

        // Heatmap counts
        val heatmap = mutableMapOf<Long, Int>()
        habitLogs.filter { it.done }.forEach { l ->
            heatmap[l.date] = (heatmap[l.date] ?: 0) + 1
        }
        routines.forEach { r ->
            routineLogs.filter { it.routineId == r.id }.forEach { l ->
                if (routineDone(r, l)) heatmap[l.date] = (heatmap[l.date] ?: 0) + 1
            }
        }
        tasks.filter { it.done }.forEach { t ->
            heatmap[t.date] = (heatmap[t.date] ?: 0) + 1
        }

        val totalCompletions = heatmap.values.sum()

        // Streaks per habit
        val streaks = habits.map { h ->
            var streak = 0
            var d = today
            var guard = 0
            while (guard < 370) {
                guard++
                if (!isScheduled(h.daysMask, d)) {
                    d = d.minusDays(1)
                    continue
                }
                val done = habitDoneByDate[d.toEpochDay()]?.any { it.habitId == h.id } == true
                if (done) {
                    streak++
                    d = d.minusDays(1)
                } else {
                    if (d == today) {
                        d = d.minusDays(1)
                        continue
                    }
                    break
                }
            }
            HabitStreak(h.name, streak)
        }.sortedByDescending { it.days }

        return StatsUiState(
            todayDone = todayDone,
            todayTotal = todayTotal,
            weekRate = weeklyRates.last(),
            totalCompletions = totalCompletions,
            weeklyRates = weeklyRates,
            heatmap = heatmap,
            heatmapStart = heatmapStart,
            streaks = streaks,
        )
    }

    fun saveReview(mood: String, note: String) = viewModelScope.launch {
        repo.upsertReview(ReviewEntity(date = rangeEnd, mood = mood, note = note.trim()))
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = CadenceDatabase.get(context)
                    return StatsViewModel(CadenceRepository(db)) as T
                }
            }
    }
}
