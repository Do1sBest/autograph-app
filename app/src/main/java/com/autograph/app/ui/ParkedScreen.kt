package com.autograph.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
                // 차량 렌더 이미지 + 오버레이
                Box(Modifier.weight(1.5f).fillMaxHeight()) {
                    BoxWithConstraints(Modifier.fillMaxSize()) {
                        // 가용 영역의 CAR_SCALE 비율 안에서 종횡비 유지하며 '맞춤'(넘침 방지)
                        val bw = maxWidth.value * CAR_SCALE
                        val bh = maxHeight.value * CAR_SCALE
                        val wide = bw / bh > CAR_ASPECT
                        val w = if (wide) bh * CAR_ASPECT else bw
                        val h = if (wide) bh else bw / CAR_ASPECT
                        Box(Modifier.align(Alignment.Center).size(w.dp, h.dp)) {
                            CarImageView(snapshot.carImage, Modifier.fillMaxSize())
                            snapshot.parts
                                .filter { it.status == PartStatus.WARN && it.hotspot != HotspotRole.NONE }
                                .forEach { part -> HotspotPin(part, this) }
                        }
                    }
                    ScoreBadge(snapshot, Modifier.align(Alignment.TopStart).padding(4.dp))
                    Text(
                        "3/4 뷰 · 상태 인포그래픽",
                        color = Palette.InkMut, fontSize = 11.sp,
                        modifier = Modifier.align(Alignment.BottomStart).padding(4.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                // 부품 리스트
                Column(
                    Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    snapshot.parts.forEachIndexed { i, p ->
                        PartRow(i, p)
                        if (i != snapshot.parts.lastIndex) Spacer(Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ValueBand(s: VehicleSnapshot, onOpenPassport: () -> Unit) {
    val diff = s.valueDiffManwon
    val up = diff >= 0
    val heroColor = if (up) Palette.Good else Palette.Crit
    Row(
        Modifier
            .fillMaxWidth()
            .background(Palette.Panel, RoundedCornerShape(14.dp))
            .border(1.dp, Palette.Accent.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("내 차 예상 자산가치", color = Palette.Ink2, fontSize = 12.sp)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(fmt(s.adjustedPriceManwon), color = Palette.Ink, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text(" 만원", color = Palette.Ink2, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.width(16.dp))
        Text("VS", color = Palette.InkMut, fontSize = 13.sp)
        Spacer(Modifier.width(16.dp))
        Column {
            Text("동일 연식 평균 중고가", color = Palette.Ink2, fontSize = 12.sp)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(fmt(s.standardPriceManwon), color = Palette.Ink2, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(" 만원", color = Palette.InkMut, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.weight(1f))
        // 핵심: 동일 연식 평균 대비 차이
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .background(Palette.Bg, RoundedCornerShape(12.dp))
                .border(1.dp, Palette.Line, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("${if (up) "▲" else "▼"} 동일 연식 평균 대비", color = Palette.Ink2, fontSize = 12.sp)
            Text(
                "${if (up) "+" else "-"}${fmt(kotlin.math.abs(diff))}만원",
                color = heroColor, fontSize = 30.sp, fontWeight = FontWeight.Bold
            )
            Text(
                if (up) "관리 양호 · 평균보다 높음" else "평균 이하",
                color = heroColor, fontSize = 12.sp
            )
        }
        Spacer(Modifier.width(12.dp))
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
private fun HotspotPin(part: VehiclePart, scope: BoxScope) {
    val bias = when (part.hotspot) {
        HotspotRole.HOOD -> BiasAlignment(-0.40f, -0.08f)
        HotspotRole.TIRE_RR -> BiasAlignment(0.68f, -0.16f)
        HotspotRole.TIRE_RL -> BiasAlignment(-0.22f, 0.55f)
        HotspotRole.TIRE_FR -> BiasAlignment(0.38f, 0.22f)
        HotspotRole.TIRE_FL -> BiasAlignment(-0.46f, 0.32f)
        HotspotRole.BRAKE -> BiasAlignment(0.0f, 0.35f)
        HotspotRole.NONE -> return
    }
    with(scope) {
        Row(
            Modifier
                .align(bias)
                .background(Color(0xE60C1017), RoundedCornerShape(999.dp))
                .border(1.dp, Palette.Warn, RoundedCornerShape(999.dp))
                .padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(8.dp).background(Palette.Warn, androidx.compose.foundation.shape.CircleShape))
            Spacer(Modifier.width(6.dp))
            Text(part.name, color = Palette.Ink, fontSize = 11.sp)
        }
    }
}
