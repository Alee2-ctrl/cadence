package com.cadence.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val daysMask: Int = 127, // Mon=1 .. Sun=64, default every day
    val timeOfDay: String = "ANYTIME", // MORNING / AFTERNOON / EVENING / ANYTIME
    val createdAt: Long = System.currentTimeMillis(),
    val archived: Boolean = false
)

@Entity(tableName = "habit_logs", primaryKeys = ["habitId", "date"])
data class HabitLogEntity(
    val habitId: Long,
    val date: Long, // epochDay
    val done: Boolean = true
)

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val steps: String = "", // one step per line
    val daysMask: Int = 127,
    val timeOfDay: String = "MORNING",
    val createdAt: Long = System.currentTimeMillis(),
    val archived: Boolean = false
)

@Entity(tableName = "routine_logs", primaryKeys = ["routineId", "date"])
data class RoutineLogEntity(
    val routineId: Long,
    val date: Long,
    val doneSteps: String = "" // completed step indexes, comma separated
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val date: Long, // epochDay
    val priority: String = "NORMAL", // LOW / NORMAL / HIGH
    val done: Boolean = false,
    val doneAt: Long? = null
)
