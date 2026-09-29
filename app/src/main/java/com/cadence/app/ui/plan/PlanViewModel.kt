package com.cadence.app.ui.plan

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
import com.cadence.app.notifications.ReminderScheduler
import com.cadence.app.ui.EditorState
import com.cadence.app.ui.TodayItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

class PlanViewModel(
    private val app: Context,
    private val repo: CadenceRepository,
) : ViewModel() {

    private val today: LocalDate = LocalDate.now()

    val selectedDate = MutableStateFlow(today)
    val weekStart = MutableStateFlow(today.with(DayOfWeek.MONDAY))
    val monthMode = MutableStateFlow(false)

    fun select(date: LocalDate) {
        selectedDate.value = date
    }

    fun prevWeek() {
        weekStart.value = weekStart.value.minusWeeks(1)
    }

    fun nextWeek() {
        weekStart.value = weekStart.value.plusWeeks(1)
    }

    fun prevMonth() {
        selectedDate.value = selectedDate.value.minusMonths(1)
    }

    fun nextMonth() {
        selectedDate.value = selectedDate.value.plusMonths(1)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val items: StateFlow<List<TodayItem>> = selectedDate.flatMapLatest { date ->
        val epoch = date.toEpochDay()
        combine(
            repo.habits,
            repo.habitLogs(epoch),
            repo.routines,
            repo.routineLogs(epoch),
            repo.tasksFor(epoch),
        ) { habits, habitLogs, routines, routineLogs, tasks ->
            buildItems(date, habits, habitLogs, routines, routineLogs, tasks)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val weekTaskDays: StateFlow<Set<Long>> = weekStart.flatMapLatest { ws ->
        repo.tasksBetween(ws.toEpochDay(), ws.plusDays(6).toEpochDay())
            .map { list -> list.map { it.date }.toSet() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthTaskDays: StateFlow<Set<Long>> = selectedDate.flatMapLatest { d ->
        val first = d.withDayOfMonth(1)
        val last = d.withDayOfMonth(d.lengthOfMonth())
        repo.tasksBetween(first.toEpochDay(), last.toEpochDay())
            .map { list -> list.map { it.date }.toSet() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private fun buildItems(
        date: LocalDate,
        habits: List<HabitEntity>,
        habitLogs: List<HabitLogEntity>,
        routines: List<RoutineEntity>,
        routineLogs: List<RoutineLogEntity>,
        tasks: List<TaskEntity>,
    ): List<TodayItem> {
        val dayMask = 1 shl (date.dayOfWeek.value - 1)
        val items = mutableListOf<TodayItem>()

        habits.filter { it.daysMask and dayMask != 0 }.forEach { h ->
            val isDone = habitLogs.any { it.habitId == h.id && it.done }
            items.add(TodayItem.Habit(h, isDone))
        }

        routines.filter { it.daysMask and dayMask != 0 }.forEach { r ->
            val steps = r.steps.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
            val doneSteps = routineLogs.firstOrNull { it.routineId == r.id }
                ?.doneSteps
                ?.split(",")
                ?.mapNotNull { it.toIntOrNull() }
                ?.toSet() ?: emptySet()
            items.add(TodayItem.Routine(r, steps, doneSteps))
        }

        tasks.forEach { items.add(TodayItem.Task(it)) }

        return items.sortedBy { it.sortKey }
    }

    fun toggleHabit(habitId: Long, done: Boolean) = viewModelScope.launch {
        repo.toggleHabit(habitId, selectedDate.value.toEpochDay(), done)
    }

    fun toggleRoutineStep(routineId: Long, stepIndex: Int, done: Boolean, current: Set<Int>) =
        viewModelScope.launch {
            repo.toggleRoutineStep(routineId, selectedDate.value.toEpochDay(), stepIndex, done, current)
        }

    fun toggleTask(task: TaskEntity) = viewModelScope.launch {
        repo.toggleTask(task)
        if (!task.done) {
            ReminderScheduler.cancel(app, ReminderScheduler.TYPE_TASK, task.id)
        }
    }

    fun save(editor: EditorState) = viewModelScope.launch {
        val dateEpoch = if (editor.date > 0) editor.date else selectedDate.value.toEpochDay()
        when (editor.type) {
            "TASK" -> {
                val newId = repo.upsertTask(
                    TaskEntity(
                        id = editor.id,
                        title = editor.name.trim(),
                        date = dateEpoch,
                        priority = editor.priority,
                        done = editor.done,
                        reminderMin = editor.reminderMin,
                    ),
                )
                ReminderScheduler.scheduleItem(
                    app, ReminderScheduler.TYPE_TASK, newId, editor.name.trim(),
                    0, dateEpoch, editor.reminderMin,
                )
            }
            "HABIT" -> {
                val newId = repo.upsertHabit(
                    HabitEntity(
                        id = editor.id,
                        name = editor.name.trim(),
                        daysMask = editor.daysMask,
                        timeOfDay = editor.timeOfDay,
                        reminderMin = editor.reminderMin,
                    ),
                )
                ReminderScheduler.scheduleItem(
                    app, ReminderScheduler.TYPE_HABIT, newId, editor.name.trim(),
                    editor.daysMask, 0, editor.reminderMin,
                )
            }
            "ROUTINE" -> {
                val newId = repo.upsertRoutine(
                    RoutineEntity(
                        id = editor.id,
                        name = editor.name.trim(),
                        steps = editor.steps,
                        daysMask = editor.daysMask,
                        timeOfDay = editor.timeOfDay,
                        reminderMin = editor.reminderMin,
                    ),
                )
                ReminderScheduler.scheduleItem(
                    app, ReminderScheduler.TYPE_ROUTINE, newId, editor.name.trim(),
                    editor.daysMask, 0, editor.reminderMin,
                )
            }
        }
    }

    fun delete(editor: EditorState) = viewModelScope.launch(Dispatchers.IO) {
        when (editor.type) {
            "TASK" -> repo.deleteTask(editor.id)
            "HABIT" -> repo.deleteHabit(editor.id)
            "ROUTINE" -> repo.deleteRoutine(editor.id)
        }
        ReminderScheduler.cancel(app, editor.type, editor.id)
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = CadenceDatabase.get(context)
                    return PlanViewModel(
                        context.applicationContext,
                        CadenceRepository(db),
                    ) as T
                }
            }
    }
}
