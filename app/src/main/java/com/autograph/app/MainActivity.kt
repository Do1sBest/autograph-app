package com.autograph.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.autograph.app.ui.AutographApp
import com.autograph.app.ui.AutographTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AutographTheme {
                AutographApp()
            }
        }
    }
}
