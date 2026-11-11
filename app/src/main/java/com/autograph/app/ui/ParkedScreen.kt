package com.autograph.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autograph.app.data.HotspotRole
import com.autograph.app.data.PartStatus
import com.autograph.app.data.VehiclePart
import com.autograph.app.data.VehicleSnapshot

@Composable
fun ParkedScreen(
    snapshot: VehicleSnapshot,
    modelKeys: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    onOpenPassport: () -> Unit,
) {
    var carIndex by remember(snapshot.key) { mutableStateOf(TURNTABLE_DEFAULT) }
    val atFront = carIndex == TURNTABLE_DEFAULT
    val hotspots = rememberHotspots()
    var carePart by remember(snapshot.key) { mutableStateOf<VehiclePart?>(null) }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        ValueBand(snapshot, onOpenPassport)
        Spacer(Modifier.height(12.dp))
        // 차량 상태 맵 패널
        Column(
            Modifier
                .fillMaxSize()
                .background(Palette.Panel, RoundedCornerShape(14.dp))
                .border(1.dp, Palette.Line, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("차량 상태 맵", color = Palette.Ink2, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                ModelChipRow(modelKeys, selected, onSelect)
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxSize()) {
                // 차량 턴테이블(드래그 회전) + 오버레이
                Box(Modifier.weight(1.5f).fillMaxHeight()) {
                    BoxWithConstraints(Modifier.fillMaxSize()) {
                        val bw = maxWidth.value * CAR_SCALE
                        val bh = maxHeight.value * CAR_SCALE
                        val wide = bw / bh > CAR_ASPECT
                        val w = if (wide) bh * CAR_ASPECT else bw
                        val h = if (wide) bh else bw / CAR_ASPECT
                        Box(Modifier.align(Alignment.Center).size(w.dp, h.dp)) {
                            CarTurntable(snapshot.key, carIndex, { carIndex = it }, Modifier.fillMaxSize())
                            // 프레임별 위치로 핀이 부위를 따라다님(뒤로 돌면 자동 숨김)
                            snapshot.parts
                                .filter { it.status == PartStatus.WARN && it.hotspot != HotspotRole.NONE }
                                .forEach { part ->
                                    val role = hotspotRoleKey(part.hotspot)
                                    val pos = role?.let { hotspots[snapshot.key]?.get(it)?.getOrNull(carIndex) }
                                    if (pos != null) HotspotPin(part, pos.x, pos.y, pos.visible, this)
                                }
                        }
                    }
                    ScoreBadge(snapshot, Modifier.align(Alignment.TopStart).padding(4.dp))
                    if (atFront) {
                        Text(
                            "↔ 드래그하여 회전",
                            color = Palette.InkMut, fontSize = 11.sp,
                            modifier = Modifier.align(Alignment.BottomStart).padding(4.dp)
                        )
                    } else {
                        Text(
                            "⟲ 정면으로",
                            color = Palette.Ink2, fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.BottomStart).padding(4.dp)
                                .background(Palette.Panel2, RoundedCornerShape(8.dp))
                                .border(1.dp, Palette.Line, RoundedCornerShape(8.dp))
                                .clickable { carIndex = TURNTABLE_DEFAULT }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                // 부품 리스트
                Column(
                    Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    snapshot.parts.forEachIndexed { i, p ->
                        // 문제 항목만 탭 가능 → 케어 시트(해결 다음 단계)
                        PartRow(i, p, if (p.care != null) ({ carePart = p }) else null)
                        if (i != snapshot.parts.lastIndex) Spacer(Modifier.height(6.dp))
                    }
                }
            }
        }
    }
    carePart?.let { part -> PartCareDialog(part, snapshot.model) { carePart = null } }
}

@Composable
private fun ValueBand(s: VehicleSnapshot, onOpenPassport: () -> Unit) {
    val diff = s.valueDiffManwon
    val up = diff >= 0
    val heroColor = if (up) Palette.Good else Palette.Crit
    Row(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(Color(0xFF132132), Color(0xFF111A26))),
                RoundedCornerShape(14.dp)
            )
            .border(1.dp, Palette.Accent.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(horizontal = 22.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 내 차 예상 자산가치
        Column {
            Text("내 차 예상 자산가치", color = Palette.Ink2, fontSize = 13.sp)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(fmt(s.adjustedPriceManwon), color = Palette.Ink, fontSize = 32.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text(" 만원", color = Palette.Ink2, fontSize = 13.sp)
            }
        }
        Spacer(Modifier.width(18.dp))
        Text("vs", color = Palette.InkMut, fontSize = 15.sp)
        Spacer(Modifier.width(18.dp))
        // 동일 연식 평균 중고가
        Column {
            Text("동일 연식 평균 중고가", color = Palette.Ink2, fontSize = 13.sp)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(fmt(s.standardPriceManwon), color = Palette.Ink2, fontSize = 32.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text(" 만원", color = Palette.InkMut, fontSize = 13.sp)
            }
        }
        Spacer(Modifier.weight(1f)) // margin-left:auto → hero 우측 정렬
        // 동일 연식 평균 대비 (hero, 별도 다크 카드)
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .background(Color(0xFF0B0F16), RoundedCornerShape(12.dp))
                .border(1.dp, Palette.Line, RoundedCornerShape(12.dp))
                .padding(horizontal = 18.dp, vertical = 8.dp)
        ) {
            Text("${if (up) "▲" else "▼"} 동일 연식 평균 대비", color = Palette.Ink2, fontSize = 12.sp)
            Text(
                "${if (up) "+" else "-"}${fmt(kotlin.math.abs(diff))}만원",
                color = heroColor, fontSize = 42.sp, fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                if (up) "관리 양호 · 평균보다 높음" else "평균보다 낮음",
                color = heroColor, fontSize = 13.sp
            )
        }
        Spacer(Modifier.width(14.dp))
        Text(
            "자세히 →",
            color = Palette.Ink2, fontSize = 13.sp,
            modifier = Modifier
                .background(Palette.Panel2, RoundedCornerShape(8.dp))
                .border(1.dp, Palette.Line, RoundedCornerShape(8.dp))
                .clickable(onClick = onOpenPassport)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun ScoreBadge(s: VehicleSnapshot, modifier: Modifier) {
    Row(
        modifier
            .background(Color(0xE60C1017), RoundedCornerShape(12.dp))
            .border(1.dp, Palette.Line, RoundedCornerShape(12.dp))
            .padding(start = 8.dp, top = 6.dp, end = 12.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MiniScoreRing(s.conditionScore, Palette.Good, Modifier.size(46.dp), numberSp = 18)
        Spacer(Modifier.width(8.dp))
        Column {
            Text("컨디션", color = Palette.InkMut, fontSize = 11.sp)
            Text(s.conditionWord, color = Palette.Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(s.conditionTag, color = Palette.Good, fontSize = 11.sp)
        }
    }
}

/** 문제 부위 핀 — 역할별 화면 비율 위치(에뮬레이터에서 미세조정). */
@Composable
private fun HotspotPin(part: VehiclePart, fx: Float, fy: Float, faceVisible: Boolean, scope: BoxScope) {
    val inf = rememberInfiniteTransition(label = "pin")
    val pulse by inf.animateFloat(
        0.3f, 1f, infiniteRepeatable(tween(850), RepeatMode.Reverse), label = "pinA"
    )
    // 부위가 반대편을 향할 땐 숨기지 않고 반투명 처리
    val pinAlpha = if (faceVisible) 1f else 0.28f
    with(scope) {
        Row(
            Modifier
                .align(BiasAlignment(fx * 2f - 1f, fy * 2f - 1f))
                .alpha(pinAlpha)
                .background(Color(0xE60C1017), RoundedCornerShape(999.dp))
                .border(1.dp, Palette.Warn, RoundedCornerShape(999.dp))
                .padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(8.dp).alpha(pulse).background(Palette.Warn, androidx.compose.foundation.shape.CircleShape))
            Spacer(Modifier.width(6.dp))
            Text(part.name, color = Palette.Ink, fontSize = 11.sp)
        }
    }
}
