package com.cadence.app.data

import kotlinx.coroutines.flow.Flow

class CadenceRepository(private val db: CadenceDatabase) {

    val habits: Flow<List<HabitEntity>> = db.habitDao().active()
    val routines: Flow<List<RoutineEntity>> = db.routineDao().active()

    fun habitLogs(date: Long): Flow<List<HabitLogEntity>> = db.habitLogDao().forDate(date)
    fun routineLogs(date: Long): Flow<List<RoutineLogEntity>> = db.routineLogDao().forDate(date)
    fun tasksFor(date: Long): Flow<List<TaskEntity>> = db.taskDao().forDate(date)

    suspend fun toggleHabit(habitId: Long, date: Long, done: Boolean) {
        if (done) {
            db.habitLogDao().upsert(HabitLogEntity(habitId = habitId, date = date, done = true))
        } else {
            db.habitLogDao().delete(habitId, date)
        }
    }

    suspend fun toggleRoutineStep(routineId: Long, date: Long, stepIndex: Int, done: Boolean, current: Set<Int>) {
        val updated = if (done) current + stepIndex else current - stepIndex
        db.routineLogDao().upsert(
            RoutineLogEntity(
                routineId = routineId,
                date = date,
                doneSteps = updated.sorted().joinToString(",")
            )
        )
    }

    suspend fun toggleTask(task: TaskEntity) {
        val nowDone = !task.done
        db.taskDao().upsert(task.copy(done = nowDone, doneAt = if (nowDone) System.currentTimeMillis() else null))
    }

    suspend fun upsertHabit(habit: HabitEntity) = db.habitDao().upsert(habit)
    suspend fun upsertRoutine(routine: RoutineEntity) = db.routineDao().upsert(routine)
    suspend fun upsertTask(task: TaskEntity) = db.taskDao().upsert(task)

    suspend fun deleteHabit(id: Long) = db.habitDao().deleteById(id)
    suspend fun deleteRoutine(id: Long) = db.routineDao().deleteById(id)
    suspend fun deleteTask(id: Long) = db.taskDao().deleteById(id)
}
