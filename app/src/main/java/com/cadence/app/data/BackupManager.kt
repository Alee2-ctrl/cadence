package com.cadence.app.data

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject

object BackupManager {

    suspend fun export(context: Context, repo: CadenceRepository, uri: Uri): Int {
        val root = JSONObject()
        root.put("version", 3)

        val habits = JSONArray()
        repo.allHabits().forEach { h ->
            habits.put(JSONObject().apply {
                put("id", h.id); put("name", h.name); put("daysMask", h.daysMask)
                put("timeOfDay", h.timeOfDay); put("reminderMin", h.reminderMin)
                put("createdAt", h.createdAt); put("archived", h.archived)
            })
        }
        root.put("habits", habits)

        val habitLogs = JSONArray()
        repo.allHabitLogs().forEach { l ->
            habitLogs.put(JSONObject().apply {
                put("habitId", l.habitId); put("date", l.date); put("done", l.done)
            })
        }
        root.put("habitLogs", habitLogs)

        val routines = JSONArray()
        repo.allRoutines().forEach { r ->
            routines.put(JSONObject().apply {
                put("id", r.id); put("name", r.name); put("steps", r.steps)
                put("daysMask", r.daysMask); put("timeOfDay", r.timeOfDay)
                put("reminderMin", r.reminderMin)
                put("createdAt", r.createdAt); put("archived", r.archived)
            })
        }
        root.put("routines", routines)

        val routineLogs = JSONArray()
        repo.allRoutineLogs().forEach { l ->
            routineLogs.put(JSONObject().apply {
                put("routineId", l.routineId); put("date", l.date); put("doneSteps", l.doneSteps)
            })
        }
        root.put("routineLogs", routineLogs)

        val tasks = JSONArray()
        repo.allTasks().forEach { t ->
            tasks.put(JSONObject().apply {
                put("id", t.id); put("title", t.title); put("date", t.date)
                put("priority", t.priority); put("done", t.done)
                put("reminderMin", t.reminderMin)
                put("doneAt", t.doneAt?.let { it } ?: JSONObject.NULL)
            })
        }
        root.put("tasks", tasks)

        val reviews = JSONArray()
        repo.allReviews().forEach { r ->
            reviews.put(JSONObject().apply {
                put("date", r.date); put("mood", r.mood); put("note", r.note)
            })
        }
        root.put("reviews", reviews)

        context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use {
            it.write(root.toString())
        } ?: throw IllegalStateException("Cannot write to selected file")

        return habits.length() + routines.length() + tasks.length()
    }

    suspend fun import(context: Context, repo: CadenceRepository, uri: Uri): Int {
        val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: throw IllegalStateException("Cannot read selected file")
        val root = JSONObject(text)
        var count = 0

        root.optJSONArray("habits")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                repo.upsertHabit(
                    HabitEntity(
                        id = o.getLong("id"),
                        name = o.getString("name"),
                        daysMask = o.optInt("daysMask", 127),
                        timeOfDay = o.optString("timeOfDay", "ANYTIME"),
                        reminderMin = o.optInt("reminderMin", -1),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                        archived = o.optBoolean("archived", false)
                    )
                )
                count++
            }
        }

        root.optJSONArray("habitLogs")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                repo.upsertHabitLog(
                    HabitLogEntity(
                        habitId = o.getLong("habitId"),
                        date = o.getLong("date"),
                        done = o.optBoolean("done", true)
                    )
                )
            }
        }

        root.optJSONArray("routines")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                repo.upsertRoutine(
                    RoutineEntity(
                        id = o.getLong("id"),
                        name = o.getString("name"),
                        steps = o.optString("steps", ""),
                        daysMask = o.optInt("daysMask", 127),
                        timeOfDay = o.optString("timeOfDay", "MORNING"),
                        reminderMin = o.optInt("reminderMin", -1),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                        archived = o.optBoolean("archived", false)
                    )
                )
                count++
            }
        }

        root.optJSONArray("routineLogs")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                repo.upsertRoutineLog(
                    RoutineLogEntity(
                        routineId = o.getLong("routineId"),
                        date = o.getLong("date"),
                        doneSteps = o.optString("doneSteps", "")
                    )
                )
            }
        }

        root.optJSONArray("tasks")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                repo.upsertTask(
                    TaskEntity(
                        id = o.getLong("id"),
                        title = o.getString("title"),
                        date = o.getLong("date"),
                        priority = o.optString("priority", "NORMAL"),
                        done = o.optBoolean("done", false),
                        reminderMin = o.optInt("reminderMin", -1),
                        doneAt = if (o.isNull("doneAt")) null else o.optLong("doneAt")
                    )
                )
                count++
            }
        }

        root.optJSONArray("reviews")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                repo.upsertReview(
                    ReviewEntity(
                        date = o.getLong("date"),
                        mood = o.optString("mood", ""),
                        note = o.optString("note", "")
                    )
                )
            }
        }

        return count
    }
}
