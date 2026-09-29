package com.cadence.app.data

import kotlinx.coroutines.flow.Flow

class CadenceRepository(private val db: CadenceDatabase) {

    val habits: Flow<List<HabitEntity>> = db.habitDao().active()
    val routines: Flow<List<RoutineEntity>> = db.routineDao().active()
    val notes: Flow<List<NoteEntity>> = db.noteDao().all()
    val modes: Flow<List<ModeEntity>> = db.modeDao().all()
    val lockout: Flow<LockoutEntity?> = db.lockoutDao().get()

    fun habitLogs(date: Long): Flow<List<HabitLogEntity>> = db.habitLogDao().forDate(date)
    fun routineLogs(date: Long): Flow<List<RoutineLogEntity>> = db.routineLogDao().forDate(date)
    fun tasksFor(date: Long): Flow<List<TaskEntity>> = db.taskDao().forDate(date)
    fun tasksBetween(fromDate: Long, toDate: Long): Flow<List<TaskEntity>> =
        db.taskDao().between(fromDate, toDate)
    fun habitLogsBetween(fromDate: Long, toDate: Long): Flow<List<HabitLogEntity>> =
        db.habitLogDao().between(fromDate, toDate)
    fun routineLogsBetween(fromDate: Long, toDate: Long): Flow<List<RoutineLogEntity>> =
        db.routineLogDao().between(fromDate, toDate)
    fun reviewFor(date: Long): Flow<ReviewEntity?> = db.reviewDao().forDate(date)

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

    suspend fun upsertHabit(habit: HabitEntity): Long = db.habitDao().upsert(habit)
    suspend fun upsertRoutine(routine: RoutineEntity): Long = db.routineDao().upsert(routine)
    suspend fun upsertTask(task: TaskEntity): Long = db.taskDao().upsert(task)
    suspend fun upsertHabitLog(log: HabitLogEntity) = db.habitLogDao().upsert(log)
    suspend fun upsertRoutineLog(log: RoutineLogEntity) = db.routineLogDao().upsert(log)
    suspend fun upsertReview(review: ReviewEntity) = db.reviewDao().upsert(review)
    suspend fun upsertNote(note: NoteEntity): Long = db.noteDao().upsert(note)
    suspend fun upsertMode(mode: ModeEntity): Long = db.modeDao().upsert(mode)
    suspend fun upsertLockout(lockout: LockoutEntity) = db.lockoutDao().upsert(lockout)
    suspend fun lockoutOnce(): LockoutEntity? = db.lockoutDao().once()

    suspend fun deleteHabit(id: Long) = db.habitDao().deleteById(id)
    suspend fun deleteRoutine(id: Long) = db.routineDao().deleteById(id)
    suspend fun deleteTask(id: Long) = db.taskDao().deleteById(id)
    suspend fun deleteNote(id: Long) = db.noteDao().deleteById(id)
    suspend fun deleteMode(id: Long) = db.modeDao().deleteById(id)

    suspend fun allHabits(): List<HabitEntity> = db.habitDao().allOnce()
    suspend fun allRoutines(): List<RoutineEntity> = db.routineDao().allOnce()
    suspend fun allTasks(): List<TaskEntity> = db.taskDao().allOnce()
    suspend fun allHabitLogs(): List<HabitLogEntity> = db.habitLogDao().allOnce()
    suspend fun allRoutineLogs(): List<RoutineLogEntity> = db.routineLogDao().allOnce()
    suspend fun allReviews(): List<ReviewEntity> = db.reviewDao().allOnce()
    suspend fun allNotes(): List<NoteEntity> = db.noteDao().allOnce()
    suspend fun allModes(): List<ModeEntity> = db.modeDao().allOnce()
}
