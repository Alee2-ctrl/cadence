package com.cadence.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.cadence.app.data.CadenceDatabase
import java.time.LocalDate
import java.time.ZoneId

object ReminderScheduler {

    const val EXTRA_TYPE = "extra_type"
    const val EXTRA_ID = "extra_id"
    const val EXTRA_NAME = "extra_name"

    const val TYPE_HABIT = "HABIT"
    const val TYPE_ROUTINE = "ROUTINE"
    const val TYPE_TASK = "TASK"

    fun scheduleItem(
        context: Context,
        type: String,
        id: Long,
        name: String,
        daysMask: Int,
        dateEpoch: Long,
        reminderMin: Int,
    ) {
        if (id <= 0 || reminderMin < 0) return
        val triggerAt = nextTrigger(type, daysMask, dateEpoch, reminderMin)
        if (triggerAt == null) {
            cancel(context, type, id)
            return
        }
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        val pi = pendingIntent(context, type, id, name)
        try {
            if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
        } catch (e: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    fun cancel(context: Context, type: String, id: Long) {
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        am.cancel(pendingIntent(context, type, id, ""))
    }

    private fun pendingIntent(context: Context, type: String, id: Long, name: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_TYPE, type)
            putExtra(EXTRA_ID, id)
            putExtra(EXTRA_NAME, name)
        }
        return PendingIntent.getBroadcast(
            context,
            (type + id).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun nextTrigger(type: String, daysMask: Int, dateEpoch: Long, reminderMin: Int): Long? {
        val now = System.currentTimeMillis()
        val zone = ZoneId.systemDefault()
        val hour = reminderMin / 60
        val minute = reminderMin % 60

        if (type == TYPE_TASK) {
            val date = LocalDate.ofEpochDay(dateEpoch)
            val at = date.atTime(hour, minute).atZone(zone).toInstant().toEpochMilli()
            return if (at > now) at else null
        }

        for (i in 0..7) {
            val date = LocalDate.now().plusDays(i.toLong())
            if (daysMask and (1 shl (date.dayOfWeek.value - 1)) == 0) continue
            val at = date.atTime(hour, minute).atZone(zone).toInstant().toEpochMilli()
            if (at > now) return at
        }
        return null
    }

    suspend fun rescheduleAll(context: Context, db: CadenceDatabase) {
        val today = LocalDate.now().toEpochDay()
        db.habitDao().allOnce()
            .filter { !it.archived && it.reminderMin >= 0 }
            .forEach {
                scheduleItem(context, TYPE_HABIT, it.id, it.name, it.daysMask, 0, it.reminderMin)
            }
        db.routineDao().allOnce()
            .filter { !it.archived && it.reminderMin >= 0 }
            .forEach {
                scheduleItem(context, TYPE_ROUTINE, it.id, it.name, it.daysMask, 0, it.reminderMin)
            }
        db.taskDao().allOnce()
            .filter { !it.done && it.reminderMin >= 0 && it.date >= today }
            .forEach {
                scheduleItem(context, TYPE_TASK, it.id, it.title, 0, it.date, it.reminderMin)
            }
    }
}
