package com.autograph.app

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.autograph.app.ui.AutographApp
import com.autograph.app.ui.AutographTheme
import com.autograph.app.ui.Palette

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Palette.systemNight = isNight(resources.configuration)
        setContent {
            AutographTheme {
                AutographApp()
            }
        }
    }

    /** 차의 낮/밤 전환(안드로이드 야간 모드) — 화면을 다시 만들지 않고 색만 바꿈(주행 중 상태 유지). */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Palette.systemNight = isNight(newConfig)
    }

    private fun isNight(c: Configuration) =
        (c.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
}
