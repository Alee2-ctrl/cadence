package com.cadence.app.lockout

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.cadence.app.R
import com.cadence.app.data.CadenceDatabase
import com.cadence.app.data.LockoutEntity
import com.cadence.app.modes.ModesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LockoutService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var overlayView: View? = null
    private var overlayFor: String? = null
    @Volatile private var config: LockoutEntity = LockoutEntity()

    private val tick = object : Runnable {
        override fun run() {
            scope.launch {
                config = CadenceDatabase.get(applicationContext).lockoutDao().once()
                    ?: LockoutEntity()
            }
            checkForeground()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Lockout",
            NotificationManager.IMPORTANCE_LOW,
        )
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Cadence Lockout is on")
            .setContentText("Kadie is guarding your focus.")
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIF_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else if (Build.VERSION.SDK_INT >= 29) {
            startForeground(NOTIF_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST)
        } else {
            startForeground(NOTIF_ID, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handler.removeCallbacks(tick)
        handler.post(tick)
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(tick)
        hideOverlay()
        super.onDestroy()
    }

    private fun isBlockingActive(): Boolean {
        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (System.currentTimeMillis() < prefs.getLong(KEY_BREAK_UNTIL, 0)) return false
        if (ModesManager.isBlocklistForced(this)) return true
        if (!config.enabled) return false
        val now = java.util.Calendar.getInstance()
        val minuteOfDay = now.get(java.util.Calendar.HOUR_OF_DAY) * 60 + now.get(java.util.Calendar.MINUTE)
        val start = config.startMin
        val end = config.endMin
        return if (start <= end) {
            minuteOfDay in start until end
        } else {
            minuteOfDay >= start || minuteOfDay < end
        }
    }

    private fun checkForeground() {
        if (!isBlockingActive()) {
            hideOverlay()
            return
        }
        val blocked = config.blockedPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (blocked.isEmpty()) {
            hideOverlay()
            return
        }
        val pkg = foregroundPackage() ?: return
        if (pkg == packageName || pkg !in blocked) {
            hideOverlay()
        } else {
            showOverlay(pkg)
        }
    }

    private fun foregroundPackage(): String? {
        val usm = getSystemService(UsageStatsManager::class.java) ?: return null
        val now = System.currentTimeMillis()
        val events = usm.queryEvents(now - 5000, now) ?: return null
        val event = UsageEvents.Event()
        var pkg: String? = null
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                pkg = event.packageName
            }
        }
        return pkg
    }

    private fun appLabel(pkg: String): String = try {
        val pm = packageManager
        pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
    } catch (e: Exception) {
        pkg
    }

    private fun showOverlay(pkg: String) {
        if (!Settings.canDrawOverlays(this)) return
        if (overlayView != null && overlayFor == pkg) return
        hideOverlay()

        val wm = getSystemService(WindowManager::class.java) ?: return
        val density = resources.displayMetrics.density
        fun dp(v: Int) = (v * density).toInt()

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(32), dp(32), dp(32), dp(32))
            setBackgroundColor(Color.rgb(0x14, 0x13, 0x12))
        }

        val label = TextView(this).apply {
            text = "CADENCE LOCKOUT"
            setTextColor(Color.rgb(0xA9, 0xC6, 0x32))
            textSize = 11f
            letterSpacing = 0.25f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        layout.addView(label)

        val title = TextView(this).apply {
            text = "${appLabel(pkg)} is locked."
            setTextColor(Color.rgb(0xF5, 0xF8, 0xEC))
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(0, dp(20), 0, 0)
        }
        layout.addView(title)

        val reasonText = config.reason.ifBlank { "This app is locked right now." }
        val reason = TextView(this).apply {
            text = "\"$reasonText\""
            setTextColor(Color.rgb(0xC9, 0xC4, 0xBC))
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, 0)
        }
        layout.addView(reason)

        val backButton = Button(this).apply {
            text = "Take me back"
            setTextColor(Color.rgb(0x1C, 0x19, 0x17))
            typeface = Typeface.DEFAULT_BOLD
            background = GradientDrawable().apply {
                setColor(Color.rgb(0xF5, 0xF8, 0xEC))
                cornerRadius = dp(24).toFloat()
            }
            setOnClickListener {
                goHome()
                hideOverlay()
            }
        }
        val buttonParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply { topMargin = dp(32) }
        layout.addView(backButton, buttonParams)

        val breakButton = TextView(this).apply {
            text = "Emergency: 5 minute break"
            setTextColor(Color.rgb(0x8A, 0x83, 0x78))
            textSize = 12f
            gravity = Gravity.CENTER
            setPadding(0, dp(20), 0, 0)
            setOnClickListener {
                getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                    .putLong(KEY_BREAK_UNTIL, System.currentTimeMillis() + 5 * 60 * 1000)
                    .apply()
                goHome()
                hideOverlay()
            }
        }
        layout.addView(breakButton)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        )
        try {
            wm.addView(layout, params)
            overlayView = layout
            overlayFor = pkg
        } catch (e: Exception) {
            overlayView = null
            overlayFor = null
        }
    }

    private fun hideOverlay() {
        val view = overlayView ?: return
        try {
            getSystemService(WindowManager::class.java)?.removeView(view)
        } catch (e: Exception) {
            // already removed
        }
        overlayView = null
        overlayFor = null
    }

    private fun goHome() {
        val home = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(home)
    }

    companion object {
        private const val CHANNEL_ID = "cadence_lockout"
        private const val NOTIF_ID = 42
        private const val PREFS = "cadence_lockout"
        private const val KEY_BREAK_UNTIL = "break_until"

        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, LockoutService::class.java),
            )
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, LockoutService::class.java))
        }
    }
}
