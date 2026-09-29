package com.cadence.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val daysMask: Int = 127, // Mon=1 .. Sun=64, default every day
    val timeOfDay: String = "ANYTIME", // MORNING / AFTERNOON / EVENING / ANYTIME
    val reminderMin: Int = -1, // minutes since midnight, -1 = no reminder
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
    val reminderMin: Int = -1,
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
    val reminderMin: Int = -1,
    val doneAt: Long? = null
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val date: Long, // epochDay
    val mood: String = "", // GREAT / OKAY / TOUGH
    val note: String = ""
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String = "",
    val text: String = "",
    val colorKey: String = "paper",
    val isChecklist: Boolean = false,
    val pinned: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "modes")
data class ModeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorKey: String = "leaf",
    val dnd: Boolean = true,
    val blocklist: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "lockout")
data class LockoutEntity(
    @PrimaryKey val id: Long = 1,
    val enabled: Boolean = false,
    val startMin: Int = 21 * 60,
    val endMin: Int = 8 * 60,
    val reason: String = "",
    val blockedPackages: String = "", // comma separated package names
)
