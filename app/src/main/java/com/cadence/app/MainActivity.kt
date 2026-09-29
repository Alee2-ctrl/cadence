package com.cadence.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.cadence.app.data.CadenceDatabase
import com.cadence.app.notifications.ReminderScheduler
import com.cadence.app.ui.CadenceRoot
import com.cadence.app.ui.theme.CadenceTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        lifecycleScope.launch(Dispatchers.IO) {
            ReminderScheduler.rescheduleAll(this@MainActivity, CadenceDatabase.get(this@MainActivity))
        }

        setContent {
            CadenceTheme {
                CadenceRoot()
            }
        }
    }
}
