package com.autograph.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autograph.app.data.PartStatus
import com.autograph.app.data.VehicleSnapshot
import kotlinx.coroutines.delay
import kotlin.math.sin

/**
 * 주행 중 — 한눈에 읽는 큰 타일 3개 + 아래 줄 3개. 터치할 것 없음.
 *
 * 차량용 기준: 주행 중에는 1fps 넘는 움직임(동영상·애니메이션) 금지(CarUxRestrictions NO_VIDEO),
 * 2초 안에 읽혀야 함 → 실시간 선그래프 대신 1초에 한 번 바뀌는 숫자.
 * 속도·브레이크 흐름은 기록해 두었다가 운전 종료 화면에서 그래프로 보여 준다.
 * 실데이터 연동 전까지는 합성 신호로 시연(기존과 같음). 실연동 시 Pleos 값으로 교체.
 */
@Composable
fun DrivingScreen(snapshot: VehicleSnapshot, onRecord: (Float, Float) -> Unit) {
    var shown by remember { mutableStateOf(DriveStats()) }

    LaunchedEffect(Unit) {
        var t = 0.0
        var speed = 46f
        var km = 0.0
        var secs = 0
        var hardBrakes = 0
        var hardAccels = 0
        var lastSecSpeed = speed
        var braking = false
        var brakeMax = 0f
        // 11-13 Pleos 성능 기준(불필요한 타이머·폴링 최소화): 예전엔 0.055초마다 깨어 계산했다(앱을 뒤로 보내도 그대로, CPU 1코어 2.3%).
        // 화면은 원래 1초에 한 번만 바뀌므로 1초에 한 번 깨어 그 1초 몫(0.055초 × 18걸음)을 한꺼번에 계산 — 신호 모양은 같음.
        // 주행 기록(운전 종료 화면 그래프)이 이어져야 해서 뒤에 있어도 1초에 한 번은 돈다
        while (true) {
            delay(1000)
            repeat(18) {
                // 합성 신호(기존 시연과 같은 식)
                t += 0.06
                val target = (48 + 26 * sin(t * 0.22) + 9 * sin(t * 0.63)).toFloat().coerceIn(0f, 150f)
                speed += (target - speed) * 0.08f
                val brake = (0.7f * (sin(t * 0.8).toFloat() - 0.55f)).coerceIn(0f, 1f)
                if (brake > 0.6f && !braking) hardBrakes++
                braking = brake > 0.6f
                brakeMax = maxOf(brakeMax, brake)
                km += speed * 0.055 / 3600.0
            }
            secs++
            if (speed - lastSecSpeed > 15f) hardAccels++
            lastSecSpeed = speed
            onRecord(speed, brakeMax)
            brakeMax = 0f
            shown = DriveStats(km, secs, hardBrakes, hardAccels)
        }
    }

    val issues = snapshot.parts.filter { it.status == PartStatus.WARN || it.status == PartStatus.CRITICAL }
    val rough = shown.hardBrakes + shown.hardAccels
    val smoothWord = if (rough <= 1) "양호" else if (rough <= 3) "주의" else "거침"
    val smoothColor = if (rough <= 1) Palette.Good else if (rough <= 3) Palette.Warn else Palette.Crit

    Column(Modifier.fillMaxSize().padding(horizontal = Dim.Pad, vertical = 8.dp)) {
        Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dim.Gap)) {
            BigTile("운전 부드러움", smoothWord, "급가속 ${shown.hardAccels} · 급제동 ${shown.hardBrakes}", smoothColor, Modifier.weight(1f))
            BigTile(
                "이번 주행", "%.1f km".format(shown.km),
                "${shown.secs / 60}분 ${shown.secs % 60}초", Palette.Ink, Modifier.weight(1f)
            )
            BigTile(
                "주차 후 확인",
                if (issues.isEmpty()) "없음" else "${issues.size}건",
                if (issues.isEmpty()) "점검할 항목 없음" else issues.joinToString(" · ") { it.name },
                if (issues.isEmpty()) Palette.Good else Palette.Warn,
                Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(Dim.Gap))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dim.Gap)) {
            val ev = snapshot.isEv
            StripTile(if (ev) "배터리" else "연료", if (ev) "72%" else "58%", Modifier.weight(1f))
            StripTile("주행 모드", if (ev) "ECO" else "COMFORT", Modifier.weight(1f))
            StripTile("외기", "14°C", Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
    }
}

private data class DriveStats(val km: Double = 0.0, val secs: Int = 0, val hardBrakes: Int = 0, val hardAccels: Int = 0)

@Composable
private fun BigTile(label: String, value: String, sub: String, valueColor: Color, modifier: Modifier) {
    Column(
        modifier.fillMaxHeight()
            .background(Palette.Panel, RoundedCornerShape(Dim.Radius))
            .padding(horizontal = 28.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(label, color = Palette.Ink2, fontSize = T.Body)
        Spacer(Modifier.height(10.dp))
        Text(value, color = valueColor, fontSize = T.Hero, fontWeight = FontWeight.Bold, maxLines = 1,
            style = androidx.compose.material3.LocalTextStyle.current.copy(fontFeatureSettings = "tnum"))
        Spacer(Modifier.height(10.dp))
        Text(sub, color = Palette.Ink2, fontSize = T.Label, maxLines = 2)
    }
}

@Composable
private fun StripTile(label: String, value: String, modifier: Modifier) {
    Row(
        modifier
            .heightIn(min = 96.dp)
            .background(Palette.Panel, RoundedCornerShape(16.dp))
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Palette.Ink2, fontSize = T.Label, modifier = Modifier.weight(1f))
        Text(value, color = Palette.Ink, fontSize = T.Title, fontWeight = FontWeight.Bold)
    }
}
