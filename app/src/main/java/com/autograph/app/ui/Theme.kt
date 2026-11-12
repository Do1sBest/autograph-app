package com.autograph.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.autograph.app.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autograph.app.data.PartStatus

/**
 * 색 묶음 — 낮(라이트) / 밤(다크).
 * 글자색은 바탕(Panel·Panel2·Bg) 위에서 대비 4.5:1 이상으로 맞춤(Google 운전 디자인·Pleos 가이드 공통 기준).
 * 밤에는 다크 필수, 낮에는 라이트·다크 모두 허용(Google Design for Driving).
 */
data class Scheme(
    val bg: Color, val panel: Color, val panel2: Color, val line: Color,
    val ink: Color, val ink2: Color, val inkMut: Color,
    val accent: Color, val onAccent: Color,
    val good: Color, val warn: Color, val crit: Color, val value: Color, val value2: Color,
    val overlay: Color,                       // 그림 위 이름표 바탕(핀·지도 라벨)
    val mapBg: Color, val mapRoad1: Color, val mapRoad2: Color, val mapPark: Color, val mapRiver: Color,
    val route: Color, val routeGlow: Color,
)

val NightScheme = Scheme(
    bg = Color(0xFF0B1018), panel = Color(0xFF121A26), panel2 = Color(0xFF17222F), line = Color(0xFF26303D),
    ink = Color(0xFFEAF1FB), ink2 = Color(0xFF9DB0C6), inkMut = Color(0xFF8091A7),   // InkMut 64748B(3.4~4.0:1) → 8091A7(5.0~5.9:1)
    accent = Color(0xFF3D8BFF), onAccent = Color(0xFF06101F),
    good = Color(0xFF34D39A), warn = Color(0xFFFFC24B), crit = Color(0xFFFF6A6A),
    value = Color(0xFFFFD166), value2 = Color(0xFF7BE0C0),
    overlay = Color(0xF00C1017),
    mapBg = Color(0xFF0C1420), mapRoad1 = Color(0xFF1A2635), mapRoad2 = Color(0xFF162130),
    mapPark = Color(0xFF122A1F), mapRiver = Color(0xFF12334A),
    route = Color(0xFF3D8BFF), routeGlow = Color(0xFF9FC6FF),
)

val DayScheme = Scheme(
    bg = Color(0xFFEEF1F5), panel = Color(0xFFFFFFFF), panel2 = Color(0xFFE9EDF3), line = Color(0xFFD6DDE6),
    ink = Color(0xFF0F172A), ink2 = Color(0xFF3F4B5E), inkMut = Color(0xFF5A6678),
    accent = Color(0xFF1D64D8), onAccent = Color(0xFFFFFFFF),
    good = Color(0xFF07704B), warn = Color(0xFF9A5800), crit = Color(0xFFC2261F),
    value = Color(0xFF8A5A00), value2 = Color(0xFF0A7461),
    overlay = Color(0xF2FFFFFF),
    mapBg = Color(0xFFE6EBF1), mapRoad1 = Color(0xFFFFFFFF), mapRoad2 = Color(0xFFF6F8FA),
    mapPark = Color(0xFFCFE6D5), mapRiver = Color(0xFFBBD6E9),
    route = Color(0xFF1D64D8), routeGlow = Color(0xFF7FB0F5),
)

/** 화면 모드 — 자동(차의 낮/밤 = 안드로이드 야간 모드 따름) / 낮 / 밤 */
enum class ThemeMode(val label: String) { AUTO("자동"), DAY("낮"), NIGHT("밤") }

/** 오토그래프 팔레트 — 현재 모드의 색을 돌려준다(상태 기반이라 바뀌면 화면이 다시 그려짐). */
object Palette {
    var mode by mutableStateOf(ThemeMode.AUTO)
    /** 시스템(차량) 야간 모드 — MainActivity 에서 설정 */
    var systemNight by mutableStateOf(true)

    val night: Boolean get() = when (mode) {
        ThemeMode.AUTO -> systemNight
        ThemeMode.DAY -> false
        ThemeMode.NIGHT -> true
    }
    val s: Scheme get() = if (night) NightScheme else DayScheme

    val Bg get() = s.bg
    val Panel get() = s.panel
    val Panel2 get() = s.panel2
    val Line get() = s.line
    val Ink get() = s.ink
    val Ink2 get() = s.ink2
    val InkMut get() = s.inkMut
    val Accent get() = s.accent
    val OnAccent get() = s.onAccent
    val Good get() = s.good
    val Warn get() = s.warn
    val Crit get() = s.crit
    val Value get() = s.value
    val Value2 get() = s.value2
    val Overlay get() = s.overlay
    /** 팝업 창 테두리 — Line 은 어두워진 뒤 화면 위에서 안 보여서 더 진한 회색(24752) */
    val ModalEdge get() = if (night) Color(0xFF4A5868) else Color(0xFFAEB9C7)
}

/**
 * 글꼴 — Pretendard(SIL OFL 1.1, Copyright (c) 2021 Kil Hyung-jin, github.com/orioncactus/pretendard).
 * 굵기 4단계. 계속 바뀌는 숫자(주행 타일)만 tnum(고정폭 숫자) — 큰 고정 숫자는 기본 폭이 더 보기 좋음.
 */
val Pretendard = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
    Font(R.font.pretendard_bold, FontWeight.Bold),
)

/**
 * 글자 크기 단계 (차량용 기준).
 * Google 운전 디자인: 주요 글자 32dp · 보조 24dp, 터치 76dp. Pleos: 최소 12sp, 대비 4.5:1.
 * 캡션(19sp)은 보조 정보에만 쓰고, 읽어야 하는 값은 Body 이상.
 */
object T {
    val Hero = 76.sp
    val H1 = 48.sp
    val Title = 32.sp
    val Body = 26.sp
    val Label = 22.sp
    val Caption = 19.sp
}

/** 간격·터치 크기 */
object Dim {
    val Touch = 76.dp      // 최소 터치 높이
    val Pad = 24.dp
    val Gap = 16.dp
    val Radius = 20.dp
}

fun PartStatus.color(): Color = when (this) {
    PartStatus.GOOD -> Palette.Good
    PartStatus.WARN -> Palette.Warn
    PartStatus.CRITICAL -> Palette.Crit
    PartStatus.MUTED -> Palette.InkMut
}

@Composable
fun AutographTheme(content: @Composable () -> Unit) {
    val s = Palette.s
    val scheme = if (Palette.night) darkColorScheme(
        primary = s.accent, background = s.bg, surface = s.panel,
        onPrimary = s.onAccent, onBackground = s.ink, onSurface = s.ink,
    ) else lightColorScheme(
        primary = s.accent, background = s.bg, surface = s.panel,
        onPrimary = s.onAccent, onBackground = s.ink, onSurface = s.ink,
    )
    MaterialTheme(colorScheme = scheme, typography = Typography()) {
        ProvideTextStyle(TextStyle(fontFamily = Pretendard), content)
    }
}
