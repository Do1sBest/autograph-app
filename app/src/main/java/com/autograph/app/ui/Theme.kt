package com.autograph.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.autograph.app.data.PartStatus

/** 오토그래프 팔레트 (automotive 다크 UI) */
object Palette {
    val Bg = Color(0xFF0B1018)
    val Panel = Color(0xFF121A26)
    val Panel2 = Color(0xFF17222F)
    val Line = Color(0xFF26303D)
    val Ink = Color(0xFFEAF1FB)
    val Ink2 = Color(0xFF9DB0C6)
    val InkMut = Color(0xFF64748B)
    val Accent = Color(0xFF3D8BFF)
    val Good = Color(0xFF34D39A)
    val Warn = Color(0xFFFFC24B)
    val Crit = Color(0xFFFF6A6A)
    val Value = Color(0xFFFFD166)
    val Value2 = Color(0xFF7BE0C0)
}

fun PartStatus.color(): Color = when (this) {
    PartStatus.GOOD -> Palette.Good
    PartStatus.WARN -> Palette.Warn
    PartStatus.CRITICAL -> Palette.Crit
    PartStatus.MUTED -> Palette.InkMut
}

private val AutographColors = darkColorScheme(
    primary = Palette.Accent,
    background = Palette.Bg,
    surface = Palette.Panel,
    onPrimary = Color(0xFF06101F),
    onBackground = Palette.Ink,
    onSurface = Palette.Ink,
)

@Composable
fun AutographTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AutographColors,
        typography = Typography(),
        content = content,
    )
}
