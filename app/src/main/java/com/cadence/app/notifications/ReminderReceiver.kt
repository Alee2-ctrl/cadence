package com.cadence.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.cadence.app.data.CadenceDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(ReminderScheduler.EXTRA_TYPE) ?: return
        val id = intent.getLongExtra(ReminderScheduler.EXTRA_ID, -1)
        val name = intent.getStringExtra(ReminderScheduler.EXTRA_NAME) ?: "Reminder"
        if (id <= 0) return

        postNotification(context, type, name)

        // Re-arm the next occurrence (habits/routines repeat weekly)
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = CadenceDatabase.get(context)
                when (type) {
                    ReminderScheduler.TYPE_HABIT ->
                        db.habitDao().allOnce().firstOrNull { it.id == id }?.let {
                            ReminderScheduler.scheduleItem(
                                context, type, id, it.name, it.daysMask, 0, it.reminderMin,
                            )
                        }
                    ReminderScheduler.TYPE_ROUTINE ->
                        db.routineDao().allOnce().firstOrNull { it.id == id }?.let {
                            ReminderScheduler.scheduleItem(
                                context, type, id, it.name, it.daysMask, 0, it.reminderMin,
                            )
                        }
                    else -> Unit // tasks fire once
                }
            } finally {
                pending.finish()
            }
        }
    }

    private fun postNotification(context: Context, type: String, name: String) {
        val nm = context.getSystemService(NotificationManager::class.java) ?: return
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Habit, routine and task reminders"
                setSound(
                    sound,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build(),
                )
            }
            nm.createNotificationChannel(channel)
        }

        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val contentPi = launch?.let {
            PendingIntent.getActivity(
                context, 0, it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        val label = when (type) {
            ReminderScheduler.TYPE_HABIT -> "Habit"
            ReminderScheduler.TYPE_ROUTINE -> "Routine"
            else -> "Task"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("$label time")
            .setContentText(name)
            .setSound(sound)
            .setAutoCancel(true)
            .setContentIntent(contentPi)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        nm.notify((type + name).hashCode(), notification)
    }

    companion object {
        const val CHANNEL_ID = "cadence_reminders"
    }
}
