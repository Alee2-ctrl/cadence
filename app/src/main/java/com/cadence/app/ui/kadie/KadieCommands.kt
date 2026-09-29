package com.cadence.app.ui.kadie

import android.content.Context
import android.content.Intent
import com.cadence.app.data.CadenceDatabase
import com.cadence.app.data.CadenceRepository
import com.cadence.app.data.HabitEntity
import com.cadence.app.data.LockoutEntity
import com.cadence.app.data.NoteEntity
import com.cadence.app.data.RoutineEntity
import com.cadence.app.data.TaskEntity
import com.cadence.app.lockout.LockoutService
import com.cadence.app.modes.ModesManager
import java.time.LocalDate

// T9.1: on-device command parsing for the Kadie command sheet. Fully offline.
object KadieCommands {

    const val HELP =
        "Try: \"add task call mom\", \"habit stretch\", \"routine shutdown\", " +
            "\"note buy figs\", \"block tiktok\", \"unblock tiktok\", " +
            "\"start focus\", \"bedtime\", \"chill\"."

    suspend fun execute(app: Context, raw: String): String {
        val db = CadenceDatabase.get(app)
        val repo = CadenceRepository(db)
        val input = raw.trim()
        if (input.isEmpty()) return "Say something and I'll handle it."
        val lower = input.lowercase()
        val today = LocalDate.now().toEpochDay()

        return when {
            lower == "help" || lower == "?" -> HELP

            lower.startsWith("task ") || lower.startsWith("todo ") || lower.startsWith("add ") -> {
                val name = input.substringAfter(' ').trim()
                if (name.isEmpty()) return "A task needs a name. Try \"add task water the plants\"."
                repo.upsertTask(TaskEntity(title = name, date = today))
                "Done. \"$name\" is on today's list."
            }

            lower.startsWith("habit ") -> {
                val name = input.substringAfter(' ').trim()
                if (name.isEmpty()) return "A habit needs a name. Try \"habit stretch\"."
                repo.upsertHabit(HabitEntity(name = name, daysMask = 127, timeOfDay = "ANYTIME"))
                "\"$name\" is now a daily habit. Small steps."
            }

            lower.startsWith("routine ") -> {
                val name = input.substringAfter(' ').trim()
                if (name.isEmpty()) return "A routine needs a name. Try \"routine shutdown\"."
                repo.upsertRoutine(RoutineEntity(name = name, steps = "", daysMask = 127, timeOfDay = "ANYTIME"))
                "Routine \"$name\" created. Add steps by tapping it."
            }

            lower.startsWith("note ") -> {
                val text = input.substringAfter(' ').trim()
                if (text.isEmpty()) return "A note needs words. Try \"note buy figs\"."
                repo.upsertNote(NoteEntity(title = "", text = text, updatedAt = System.currentTimeMillis()))
                "Noted. It's safe in Notes."
            }

            lower.startsWith("block ") -> {
                val name = input.substringAfter(' ').trim()
                val found = findApp(app, name)
                    ?: return "I couldn't find an app called \"$name\"."
                val current = repo.lockoutOnce() ?: LockoutEntity()
                val set = current.blockedPackages.split(",")
                    .map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet()
                if (!set.add(found.second)) return "${found.first} is already blocked."
                repo.upsertLockout(current.copy(blockedPackages = set.joinToString(",")))
                if (current.enabled) LockoutService.start(app)
                "${found.first} is on the blocklist. I'll guard your focus."
            }

            lower.startsWith("unblock ") -> {
                val name = input.substringAfter(' ').trim()
                val current = repo.lockoutOnce() ?: LockoutEntity()
                val set = current.blockedPackages.split(",")
                    .map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet()
                val found = findApp(app, name)
                val pkg = found?.second ?: set.firstOrNull { it.contains(name, ignoreCase = true) }
                if (pkg == null || !set.remove(pkg)) return "\"$name\" isn't on the blocklist."
                repo.upsertLockout(current.copy(blockedPackages = set.joinToString(",")))
                "${found?.first ?: name} is free again. Use it well."
            }

            lower.startsWith("start ") || lower == "focus" -> {
                val name = if (lower == "focus") "focus" else input.substringAfter(' ').trim()
                val mode = repo.allModes().firstOrNull { it.name.lowercase().contains(name.lowercase()) }
                    ?: return "No mode called \"$name\". Build one in Modes."
                ModesManager.activate(app, mode)
                "${mode.name} mode is on. ${if (mode.dnd) "Shhh." else "Go."}"
            }

            lower == "bedtime" || lower == "sleep" -> {
                val mode = repo.allModes().firstOrNull { it.name.lowercase().contains("bedtime") }
                    ?: return "No Bedtime mode found. Build one in Modes."
                ModesManager.activate(app, mode)
                "Bedtime mode on. Goodnight. I'll sleep too."
            }

            lower == "chill" || lower == "stop" || lower == "mode off" || lower == "stop mode" || lower == "off" -> {
                ModesManager.deactivate(app)
                "Mode off. Enjoy the noise."
            }

            else -> "Hmm, I don't know that one yet. $HELP"
        }
    }

    private fun findApp(app: Context, name: String): Pair<String, String>? {
        if (name.isEmpty()) return null
        val pm = app.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val matches = pm.queryIntentActivities(intent, 0)
        val hit = matches.firstOrNull {
            it.loadLabel(pm).toString().contains(name, ignoreCase = true)
        } ?: return null
        val label = hit.loadLabel(pm).toString()
        val pkg = hit.activityInfo?.packageName ?: return null
        return label to pkg
    }
}
