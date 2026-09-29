package com.cadence.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cadence.app.data.CadenceDatabase
import com.cadence.app.lockout.LockoutService
import com.cadence.app.modes.ModesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = CadenceDatabase.get(context)
                ReminderScheduler.rescheduleAll(context, db)
                val lockout = db.lockoutDao().once()
                if (lockout?.enabled == true || ModesManager.isBlocklistForced(context)) {
                    LockoutService.start(context)
                }
            } finally {
                pending.finish()
            }
        }
    }
}
