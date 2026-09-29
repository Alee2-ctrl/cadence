package com.cadence.app.lockout

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.cadence.app.R
import com.cadence.app.data.CadenceDatabase
import com.cadence.app.data.LockoutEntity
import com.cadence.app.modes.ModesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

// Line-art Kadie standing guard, drawn to match the roadmap T7 sketch:
// rounded head outline, clay antenna ball, clay eyes, small smile, shield.
private class KadieGuardView(context: Context) : View(context) {

    private val paperStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(0xF5, 0xF8, 0xEC)
        style = Paint.Style.STROKE
        strokeWidth = 5f
        strokeCap = Paint.Cap.ROUND
    }
    private val clayFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(0xD1, 0x9A, 0x3D)
        style = Paint.Style.FILL
    }
    private val matchaStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(0xA9, 0xC6, 0x32)
        style = Paint.Style.STROKE
        strokeWidth = 5f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2f

        // Head: rounded square outline
        val headW = w * 0.46f
        val headH = h * 0.52f
        val headLeft = cx - headW / 2f
        val headTop = h * 0.24f
        val head = RectF(headLeft, headTop, headLeft + headW, headTop + headH)
        canvas.drawRoundRect(head, headW * 0.32f, headW * 0.32f, paperStroke)

        // Antenna: stem + clay ball
        val stemTop = headTop - h * 0.16f
        canvas.drawLine(cx, headTop, cx, stemTop + h * 0.05f, paperStroke)
        canvas.drawCircle(cx, stemTop, h * 0.045f, clayFill)

        // Eyes: two clay dots
        val eyeY = headTop + headH * 0.42f
        canvas.drawCircle(cx - headW * 0.18f, eyeY, h * 0.032f, clayFill)
        canvas.drawCircle(cx + headW * 0.18f, eyeY, h * 0.032f, clayFill)

        // Smile: shallow arc
        val smile = RectF(
            cx - headW * 0.12f,
            eyeY + headH * 0.10f,
            cx + headW * 0.12f,
            eyeY + headH * 0.30f,
        )
        canvas.drawArc(smile, 15f, 150f, false, paperStroke)

        // Shield at lower right of the head
        val sw = w * 0.22f
        val sh = h * 0.26f
        val sLeft = headLeft + headW - sw * 0.30f
        val sTop = headTop + headH - sh * 0.55f
        val path = android.graphics.Path().apply {
            moveTo(sLeft, sTop)
            lineTo(sLeft + sw, sTop)
            lineTo(sLeft + sw, sTop + sh * 0.55f)
            quadraticTo(sLeft + sw, sTop + sh * 0.95f, sLeft + sw / 2f, sTop + sh)
            quadraticTo(sLeft, sTop + sh * 0.95f, sLeft, sTop + sh * 0.55f)
            close()
        }
        canvas.drawPath(path, matchaStroke)

        // Check inside the shield
        val ck = sh * 0.28f
        val ckx = sLeft + sw / 2f
        val cky = sTop + sh * 0.48f
        canvas.drawLine(ckx - ck * 0.7f, cky, ckx - ck * 0.15f, cky + ck * 0.55f, matchaStroke)
        canvas.drawLine(ckx - ck * 0.15f, cky + ck * 0.55f, ckx + ck * 0.8f, cky - ck * 0.5f, matchaStroke)
    }
}

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

    private fun fmt(min: Int): String =
        String.format(java.util.Locale.getDefault(), "%02d:%02d", min / 60, min % 60)

    private fun showOverlay(pkg: String) {
        if (!Settings.canDrawOverlays(this)) return
        if (overlayView != null && overlayFor == pkg) return
        hideOverlay()

        val wm = getSystemService(WindowManager::class.java) ?: return
        val density = resources.displayMetrics.density
        fun dp(v: Float) = (v * density).toInt()

        val poppinsBold = ResourcesCompat.getFont(this, R.font.poppins_bold)
        val poppinsMedium = ResourcesCompat.getFont(this, R.font.poppins_medium)

        val paper = Color.rgb(0xF5, 0xF8, 0xEC)
        val matcha = Color.rgb(0xA9, 0xC6, 0x32)
        val muted = Color.rgb(0xC9, 0xC4, 0xBC)
        val faintOnDark = Color.rgb(0x8A, 0x83, 0x78)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(28f), dp(56f), dp(28f), dp(36f))
            setBackgroundColor(Color.rgb(0x14, 0x13, 0x12))
        }

        // Mono status strip: BLOCKED 21:00 - 08:00
        val strip = TextView(this).apply {
            text = "BLOCKED  ${fmt(config.startMin)} – ${fmt(config.endMin)}"
            setTextColor(matcha)
            textSize = 11f
            letterSpacing = 0.3f
            typeface = poppinsBold
            gravity = Gravity.CENTER
        }
        layout.addView(strip, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ))

        // Kadie line art
        val kadie = KadieGuardView(this)
        layout.addView(kadie, LinearLayout.LayoutParams(dp(190f), dp(190f)).apply {
            topMargin = dp(28f)
        })

        val title = TextView(this).apply {
            text = "${appLabel(pkg)} is locked."
            setTextColor(paper)
            textSize = 25f
            typeface = poppinsBold
            gravity = Gravity.CENTER
            setPadding(0, dp(24f), 0, 0)
        }
        layout.addView(title, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ))

        val reasonText = config.reason.ifBlank { "This app is locked right now." }
        val reason = TextView(this).apply {
            text = "\"$reasonText\""
            setTextColor(muted)
            textSize = 14f
            typeface = poppinsMedium
            gravity = Gravity.CENTER
            setPadding(dp(12f), dp(12f), dp(12f), 0)
        }
        layout.addView(reason, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ))

        // Paper pill: Take me back
        val backButton = TextView(this).apply {
            text = "Take me back"
            setTextColor(Color.rgb(0x14, 0x13, 0x12))
            textSize = 15f
            typeface = poppinsBold
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                setColor(paper)
                cornerRadius = dp(28f).toFloat()
            }
            setOnClickListener {
                goHome()
                hideOverlay()
            }
        }
        layout.addView(backButton, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(54f),
        ).apply { topMargin = dp(36f) })

        // Break chips: 5m / 10m / 15m
        val chipRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        listOf(5, 10, 15).forEach { minutes ->
            val chip = TextView(this).apply {
                text = "${minutes}m break"
                setTextColor(paper)
                textSize = 12f
                typeface = poppinsMedium
                gravity = Gravity.CENTER
                background = GradientDrawable().apply {
                    setColor(Color.TRANSPARENT)
                    cornerRadius = dp(20f).toFloat()
                    setStroke(dp(1.5f), Color.rgb(0x4A, 0x45, 0x3E))
                }
                setPadding(dp(18f), 0, dp(18f), 0)
                setOnClickListener {
                    getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                        .putLong(
                            KEY_BREAK_UNTIL,
                            System.currentTimeMillis() + minutes * 60 * 1000L,
                        )
                        .apply()
                    goHome()
                    hideOverlay()
                }
            }
            chipRow.addView(chip, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(40f),
            ).apply {
                marginStart = dp(6f)
                marginEnd = dp(6f)
            })
        }
        layout.addView(chipRow, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply { topMargin = dp(22f) })

        val hint = TextView(this).apply {
            text = "Emergency break pauses the lockout briefly."
            setTextColor(faintOnDark)
            textSize = 11f
            typeface = poppinsMedium
            gravity = Gravity.CENTER
            setPadding(0, dp(18f), 0, 0)
        }
        layout.addView(hint, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ))

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
