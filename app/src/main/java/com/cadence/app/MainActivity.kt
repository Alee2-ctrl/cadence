package com.cadence.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.cadence.app.ui.CadenceRoot
import com.cadence.app.ui.theme.CadenceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CadenceTheme {
                CadenceRoot()
            }
        }
    }
}
