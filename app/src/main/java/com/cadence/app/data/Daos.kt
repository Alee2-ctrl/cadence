package com.cadence.app.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE archived = 0 ORDER BY createdAt")
    fun active(): Flow<List<HabitEntity>>

    @Upsert
    suspend fun upsert(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface HabitLogDao {
    @Query("SELECT * FROM habit_logs WHERE date = :date")
    fun forDate(date: Long): Flow<List<HabitLogEntity>>

    @Upsert
    suspend fun upsert(log: HabitLogEntity)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND date = :date")
    suspend fun delete(habitId: Long, date: Long)
}

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines WHERE archived = 0 ORDER BY createdAt")
    fun active(): Flow<List<RoutineEntity>>

    @Upsert
    suspend fun upsert(routine: RoutineEntity)

    @Query("DELETE FROM routines WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface RoutineLogDao {
    @Query("SELECT * FROM routine_logs WHERE date = :date")
    fun forDate(date: Long): Flow<List<RoutineLogEntity>>

    @Upsert
    suspend fun upsert(log: RoutineLogEntity)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY done, id")
    fun forDate(date: Long): Flow<List<TaskEntity>>

    @Upsert
    suspend fun upsert(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Long)
}
