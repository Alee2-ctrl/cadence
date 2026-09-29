package com.cadence.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE archived = 0 ORDER BY createdAt")
    fun active(): Flow<List<HabitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(habit: HabitEntity): Long

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM habits")
    suspend fun allOnce(): List<HabitEntity>
}

@Dao
interface HabitLogDao {
    @Query("SELECT * FROM habit_logs WHERE date = :date")
    fun forDate(date: Long): Flow<List<HabitLogEntity>>

    @Upsert
    suspend fun upsert(log: HabitLogEntity)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND date = :date")
    suspend fun delete(habitId: Long, date: Long)

    @Query("SELECT * FROM habit_logs")
    suspend fun allOnce(): List<HabitLogEntity>
}

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines WHERE archived = 0 ORDER BY createdAt")
    fun active(): Flow<List<RoutineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(routine: RoutineEntity): Long

    @Query("DELETE FROM routines WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM routines")
    suspend fun allOnce(): List<RoutineEntity>
}

@Dao
interface RoutineLogDao {
    @Query("SELECT * FROM routine_logs WHERE date = :date")
    fun forDate(date: Long): Flow<List<RoutineLogEntity>>

    @Upsert
    suspend fun upsert(log: RoutineLogEntity)

    @Query("SELECT * FROM routine_logs")
    suspend fun allOnce(): List<RoutineLogEntity>
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY done, id")
    fun forDate(date: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE date BETWEEN :fromDate AND :toDate")
    fun between(fromDate: Long, toDate: Long): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: TaskEntity): Long

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM tasks")
    suspend fun allOnce(): List<TaskEntity>
}
