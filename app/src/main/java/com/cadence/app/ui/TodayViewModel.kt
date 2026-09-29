package com.cadence.app.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cadence.app.data.CadenceDatabase
import com.cadence.app.data.CadenceRepository
import com.cadence.app.data.HabitEntity
import com.cadence.app.data.HabitLogEntity
import com.cadence.app.data.RoutineEntity
import com.cadence.app.data.RoutineLogEntity
import com.cadence.app.data.TaskEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed interface TodayItem {
    val timeOfDay: String
    val sortKey: Long

    data class Habit(val habit: HabitEntity, val done: Boolean) : TodayItem {
        override val timeOfDay get() = habit.timeOfDay
        override val sortKey get() = habit.createdAt
    }

    data class Routine(val routine: RoutineEntity, val steps: List<String>, val doneSteps: Set<Int>) : TodayItem {
        override val timeOfDay get() = routine.timeOfDay
        override val sortKey get() = routine.createdAt
    }

    data class Task(val task: TaskEntity) : TodayItem {
        override val timeOfDay get() = "ANYTIME"
        override val sortKey get() = task.id
    }
}

data class TodayUiState(
    val date: LocalDate = LocalDate.now(),
    val items: List<TodayItem> = emptyList(),
    val doneCount: Int = 0,
    val totalCount: Int = 0
)

data class EditorState(
    val id: Long = 0,
    val type: String = "TASK", // TASK / HABIT / ROUTINE
    val name: String = "",
    val daysMask: Int = 127,
    val timeOfDay: String = "ANYTIME",
    val priority: String = "NORMAL",
    val steps: String = "",
    val date: Long = 0,
    val done: Boolean = false
) {
    companion object {
        fun from(item: TodayItem): EditorState = when (item) {
            is TodayItem.Habit -> EditorState(
                id = item.habit.id,
                type = "HABIT",
                name = item.habit.name,
                daysMask = item.habit.daysMask,
                timeOfDay = item.habit.timeOfDay
            )
            is TodayItem.Routine -> EditorState(
                id = item.routine.id,
                type = "ROUTINE",
                name = item.routine.name,
                daysMask = item.routine.daysMask,
                timeOfDay = item.routine.timeOfDay,
                steps = item.routine.steps
            )
            is TodayItem.Task -> EditorState(
                id = item.task.id,
                type = "TASK",
                name = item.task.title,
                priority = item.task.priority,
                date = item.task.date,
                done = item.task.done
            )
        }
    }
}

class TodayViewModel(private val repo: CadenceRepository) : ViewModel() {

    private val today: LocalDate = LocalDate.now()
    private val todayEpoch: Long = today.toEpochDay()
    private val todayMask: Int = 1 shl (today.dayOfWeek.value - 1)

    val state: StateFlow<TodayUiState> = combine(
        repo.habits,
        repo.habitLogs(todayEpoch),
        repo.routines,
        repo.routineLogs(todayEpoch),
        repo.tasksFor(todayEpoch)
    ) { habits, habitLogs, routines, routineLogs, tasks ->
        build(habits, habitLogs, routines, routineLogs, tasks)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        TodayUiState(date = today)
    )

    private fun build(
        habits: List<HabitEntity>,
        habitLogs: List<HabitLogEntity>,
        routines: List<RoutineEntity>,
        routineLogs: List<RoutineLogEntity>,
        tasks: List<TaskEntity>
    ): TodayUiState {
        val items = mutableListOf<TodayItem>()
        var done = 0
        var total = 0

        habits.filter { it.daysMask and todayMask != 0 }.forEach { h ->
            val isDone = habitLogs.any { it.habitId == h.id && it.done }
            items.add(TodayItem.Habit(h, isDone))
            total += 1
            if (isDone) done += 1
        }

        routines.filter { it.daysMask and todayMask != 0 }.forEach { r ->
            val steps = r.steps.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
            val doneSteps = routineLogs.firstOrNull { it.routineId == r.id }
                ?.doneSteps
                ?.split(",")
                ?.mapNotNull { it.toIntOrNull() }
                ?.toSet() ?: emptySet()
            items.add(TodayItem.Routine(r, steps, doneSteps))
            if (steps.isEmpty()) {
                total += 1
                if (doneSteps.contains(-1)) done += 1
            } else {
                total += steps.size
                done += doneSteps.count { it in steps.indices }
            }
        }

        tasks.forEach { t ->
            items.add(TodayItem.Task(t))
            total += 1
            if (t.done) done += 1
        }

        return TodayUiState(
            date = today,
            items = items.sortedBy { it.sortKey },
            doneCount = done,
            totalCount = total
        )
    }

    fun toggleHabit(habitId: Long, done: Boolean) = viewModelScope.launch {
        repo.toggleHabit(habitId, todayEpoch, done)
    }

    fun toggleRoutineStep(routineId: Long, stepIndex: Int, done: Boolean, current: Set<Int>) = viewModelScope.launch {
        repo.toggleRoutineStep(routineId, todayEpoch, stepIndex, done, current)
    }

    fun toggleTask(task: TaskEntity) = viewModelScope.launch {
        repo.toggleTask(task)
    }

    fun save(editor: EditorState) = viewModelScope.launch {
        when (editor.type) {
            "TASK" -> repo.upsertTask(
                TaskEntity(
                    id = editor.id,
                    title = editor.name.trim(),
                    date = if (editor.date > 0) editor.date else todayEpoch,
                    priority = editor.priority,
                    done = editor.done
                )
            )
            "HABIT" -> repo.upsertHabit(
                HabitEntity(
                    id = editor.id,
                    name = editor.name.trim(),
                    daysMask = editor.daysMask,
                    timeOfDay = editor.timeOfDay
                )
            )
            "ROUTINE" -> repo.upsertRoutine(
                RoutineEntity(
                    id = editor.id,
                    name = editor.name.trim(),
                    steps = editor.steps,
                    daysMask = editor.daysMask,
                    timeOfDay = editor.timeOfDay
                )
            )
        }
    }

    fun delete(editor: EditorState) = viewModelScope.launch {
        when (editor.type) {
            "TASK" -> repo.deleteTask(editor.id)
            "HABIT" -> repo.deleteHabit(editor.id)
            "ROUTINE" -> repo.deleteRoutine(editor.id)
        }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = CadenceDatabase.get(context)
                    return TodayViewModel(CadenceRepository(db)) as T
                }
            }
    }
}
