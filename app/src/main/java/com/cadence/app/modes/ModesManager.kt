package com.cadence.app.modes

import android.app.AppOpsManager
import android.app.NotificationManager
import android.content.Context
import android.os.Process
import android.provider.Settings
import com.cadence.app.data.ModeEntity
import com.cadence.app.lockout.LockoutService

object ModesManager {

    private const val PREFS = "cadence_modes"
    private const val KEY_ID = "active_mode_id"
    private const val KEY_NAME = "active_mode_name"
    private const val KEY_FORCED = "blocklist_forced"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun activeModeId(context: Context): Long = prefs(context).getLong(KEY_ID, -1L)

    fun activeModeName(context: Context): String? = prefs(context).getString(KEY_NAME, null)

    fun isBlocklistForced(context: Context): Boolean =
        activeModeId(context) != -1L && prefs(context).getBoolean(KEY_FORCED, false)

    fun hasPolicyAccess(context: Context): Boolean =
        getNotificationManager(context)?.isNotificationPolicyAccessGranted == true

    fun hasUsageAccess(context: Context): Boolean {
        val appOps = context.getSystemService(AppOpsManager::class.java) ?: return false
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName,
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun hasOverlayAccess(context: Context): Boolean = Settings.canDrawOverlays(context)

    fun activate(context: Context, mode: ModeEntity) {
        prefs(context).edit()
            .putLong(KEY_ID, mode.id)
            .putString(KEY_NAME, mode.name)
            .putBoolean(KEY_FORCED, mode.blocklist)
            .apply()
        if (mode.dnd && hasPolicyAccess(context)) {
            getNotificationManager(context)?.setInterruptionFilter(
                NotificationManager.INTERRUPTION_FILTER_NONE,
            )
        }
        if (mode.blocklist) {
            LockoutService.start(context)
        }
    }

    fun deactivate(context: Context) {
        prefs(context).edit()
            .putLong(KEY_ID, -1L)
            .putString(KEY_NAME, null)
            .putBoolean(KEY_FORCED, false)
            .apply()
        if (hasPolicyAccess(context)) {
            getNotificationManager(context)?.setInterruptionFilter(
                NotificationManager.INTERRUPTION_FILTER_ALL,
            )
        }
    }

    private fun getNotificationManager(context: Context): NotificationManager? =
        context.getSystemService(NotificationManager::class.java)
}
