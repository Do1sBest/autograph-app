package com.autograph.app.ui

import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autograph.app.data.HotspotRole
import com.autograph.app.data.PartStatus
import com.autograph.app.data.VehiclePart
import com.autograph.app.data.VehicleSnapshot

/**
 * 주차 중 — 위: 핵심 숫자 하나(동일 연식 평균 대비) + 컨디션.
 * 아래: 차량(드래그 회전·문제 부위 핀) | 점검 필요 항목 먼저, 정상 항목은 작게.
 */
@Composable
fun ParkedScreen(
    snapshot: VehicleSnapshot,
    onOpenPassport: () -> Unit,
) {
    var carIndex by remember(snapshot.key) { mutableStateOf(TURNTABLE_DEFAULT) }
    val atFront = carIndex == TURNTABLE_DEFAULT
    val hotspots = rememberHotspots()
    var carePart by remember(snapshot.key) { mutableStateOf<VehiclePart?>(null) }
    val issues = snapshot.parts.filter { it.status == PartStatus.WARN || it.status == PartStatus.CRITICAL }
    val oks = snapshot.parts.filter { it !in issues }

    Column(Modifier.fillMaxSize().padding(horizontal = Dim.Pad, vertical = 8.dp)) {
        ValueHero(snapshot, onOpenPassport)
        Spacer(Modifier.height(Dim.Gap))
        Row(Modifier.weight(1f).fillMaxWidth()) {
            // 차량 턴테이블(드래그 회전) + 문제 부위 핀
            Box(
                Modifier.weight(1.1f).fillMaxHeight()
                    .background(Palette.Panel, RoundedCornerShape(Dim.Radius))
            ) {
                BoxWithConstraints(Modifier.fillMaxSize().padding(bottom = Dim.Touch)) {
                    val bw = maxWidth.value * CAR_SCALE
                    val bh = maxHeight.value * CAR_SCALE
                    val wide = bw / bh > CAR_ASPECT
                    val w = if (wide) bh * CAR_ASPECT else bw
                    val h = if (wide) bh else bw / CAR_ASPECT
                    Box(Modifier.align(Alignment.Center).size(w.dp, h.dp)) {
                        CarTurntable(snapshot.key, carIndex, { carIndex = it }, Modifier.fillMaxSize())
                        issues.filter { it.hotspot != HotspotRole.NONE }.forEach { part ->
                            val role = hotspotRoleKey(part.hotspot)
                            val pos = role?.let { hotspots[snapshot.key]?.get(it)?.getOrNull(carIndex) }
                            if (pos != null) HotspotPin(part, pos.x, pos.y, pos.visible, this)
                        }
                    }
                }
                Box(Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                    if (atFront) {
                        Box(Modifier.heightIn(min = Dim.Touch).padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
                            Text("↔ 드래그하여 회전", color = Palette.InkMut, fontSize = T.Caption)
                        }
                    } else {
                        Btn("정면으로", BtnStyle.GHOST, fontSize = T.Label, leading = Icons.Filled.Refresh) { carIndex = TURNTABLE_DEFAULT }
                    }
                }
            }
            Spacer(Modifier.width(20.dp))
            // 항목 — 점검 필요 먼저
            Column(Modifier.weight(1f).fillMaxHeight()) {
                SectionTitle("점검 필요", issues.size, if (issues.isEmpty()) Palette.Good else Palette.Warn, IconIssue)
                if (issues.isEmpty()) {
                    Text("지금 점검할 항목이 없어요", color = Palette.Ink2, fontSize = T.Body)
                }
                issues.forEach { p ->
                    IssueCard(p, if (p.care != null) ({ carePart = p }) else null)
                    Spacer(Modifier.height(12.dp))
                }
                Spacer(Modifier.height(12.dp))
                SectionTitle("정상", oks.size, Palette.Good, IconOk)
                oks.chunked(2).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { OkCell(it, Modifier.weight(1f)) }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
    val pd = rememberPriceData()
    carePart?.let { part ->
        PartCareDialog(part, snapshot.model, pd?.cars?.get(snapshot.key)?.items?.get(part.name), pd?.asOf ?: "") { carePart = null }
    }
}

/** 핵심 숫자 하나 — 동일 연식 평균 대비 차이. 두 금액은 한 줄 근거로만. */
@Composable
private fun ValueHero(s: VehicleSnapshot, onOpenPassport: () -> Unit) {
    val diff = s.valueDiffManwon
    val up = diff >= 0
    val heroColor = if (up) Palette.Good else Palette.Crit
    Row(
        Modifier
            .fillMaxWidth()
            .background(Palette.Panel, RoundedCornerShape(Dim.Radius))
            .padding(horizontal = 28.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("동일 연식 평균 대비", color = Palette.Ink2, fontSize = T.Label)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    "${if (up) "+" else "-"}${fmt(kotlin.math.abs(diff))}",
                    color = heroColor, fontSize = T.Hero, fontWeight = FontWeight.Bold, letterSpacing = (-1.5).sp
                )
                Text("만원", color = heroColor, fontSize = T.Title, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 12.dp))
                Spacer(Modifier.width(20.dp))
                Text(
                    if (up) "관리 양호 · 평균보다 높음" else "평균보다 낮음",
                    color = heroColor, fontSize = T.Body, modifier = Modifier.padding(bottom = 14.dp)
                )
            }
            Text(
                "내 차 예상 ${fmt(s.adjustedPriceManwon)}만원 · 동일 연식 평균 ${fmt(s.standardPriceManwon)}만원",
                color = Palette.Ink2, fontSize = T.Label
            )
        }
        // 컨디션
        Row(verticalAlignment = Alignment.CenterVertically) {
            MiniScoreRing(s.conditionScore, Palette.Good, Modifier.size(112.dp), numberSp = 40)
            Spacer(Modifier.width(16.dp))
            Column {
                Text("컨디션", color = Palette.Ink2, fontSize = T.Label)
                Text(s.conditionWord, color = Palette.Ink, fontSize = T.H1, fontWeight = FontWeight.Bold)
                Text(s.conditionTag, color = Palette.Good, fontSize = T.Caption)
            }
        }
        Spacer(Modifier.width(32.dp))
        Btn("밸류 패스포트", BtnStyle.TONAL, trailing = IconNext, onClick = onOpenPassport)
    }
}

/** 문제 부위 핀 — 프레임별 위치로 부위를 따라다님(반대편이면 반투명). */
@Composable
private fun HotspotPin(part: VehiclePart, fx: Float, fy: Float, faceVisible: Boolean, scope: BoxScope) {
    // 11-13 Pleos 성능 기준(주차 중 CPU 25% 이하): 끝없이 깜빡이면 1초에 60번 다시 그려 CPU 1코어 34%(에뮬레이터 측정) →
    // 나타날 때(차종이 바뀔 때) 3번만 깜빡이고 멈춤. 알파는 그리기 단계에서만 읽어 깜빡이는 동안에도 화면 구성을 다시 하지 않음
    val pulse = remember { Animatable(1f) }
    LaunchedEffect(part.name) {
        repeat(3) {
            pulse.animateTo(0.35f, tween(900))
            pulse.animateTo(1f, tween(900))
        }
    }
    val pinAlpha = if (faceVisible) 1f else 0.3f
    with(scope) {
        Row(
            Modifier
                .align(BiasAlignment(fx * 2f - 1f, fy * 2f - 1f))
                .alpha(pinAlpha)
                .background(Palette.Overlay, RoundedCornerShape(999.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(12.dp).graphicsLayer { alpha = pulse.value }.background(part.status.color(), CircleShape))
            Spacer(Modifier.width(8.dp))
            Text(part.name, color = Palette.Ink, fontSize = T.Caption, fontWeight = FontWeight.SemiBold)
        }
    }
}
