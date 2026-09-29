package com.cadence.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cadence.app.data.CadenceDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ReminderScheduler.rescheduleAll(context, CadenceDatabase.get(context))
            } finally {
                pending.finish()
            }
        }
    }
}
