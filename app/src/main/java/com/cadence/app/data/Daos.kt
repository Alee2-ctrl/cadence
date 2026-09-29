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

    @Query("SELECT * FROM habit_logs WHERE date BETWEEN :fromDate AND :toDate")
    fun between(fromDate: Long, toDate: Long): Flow<List<HabitLogEntity>>

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

    @Query("SELECT * FROM routine_logs WHERE date BETWEEN :fromDate AND :toDate")
    fun between(fromDate: Long, toDate: Long): Flow<List<RoutineLogEntity>>

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

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE date = :date")
    fun forDate(date: Long): Flow<ReviewEntity?>

    @Upsert
    suspend fun upsert(review: ReviewEntity)

    @Query("SELECT * FROM reviews")
    suspend fun allOnce(): List<ReviewEntity>
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY pinned DESC, updatedAt DESC")
    fun all(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: NoteEntity): Long

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM notes")
    suspend fun allOnce(): List<NoteEntity>
}

@Dao
interface ModeDao {
    @Query("SELECT * FROM modes ORDER BY createdAt")
    fun all(): Flow<List<ModeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(mode: ModeEntity): Long

    @Query("DELETE FROM modes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM modes")
    suspend fun allOnce(): List<ModeEntity>
}

@Dao
interface LockoutDao {
    @Query("SELECT * FROM lockout WHERE id = 1")
    fun get(): Flow<LockoutEntity?>

    @Query("SELECT * FROM lockout WHERE id = 1")
    suspend fun once(): LockoutEntity?

    @Upsert
    suspend fun upsert(lockout: LockoutEntity)
}
