package com.autograph.app.ui

import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autograph.app.data.VehicleSnapshot
import kotlin.math.abs

/** 밸류 패스포트 — 주차 중 '밸류 패스포트'로 진입. 왼쪽 이력, 오른쪽 보정가치 + 한 가지 행동. */
@Composable
fun PassportScreen(s: VehicleSnapshot, onBack: () -> Unit) {
    val diff = s.valueDiffManwon
    Row(
        Modifier.fillMaxSize().padding(horizontal = Dim.Pad, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        // 왼쪽 — 이력
        Column(Modifier.weight(1.1f).fillMaxHeight()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Btn("주차 중으로", BtnStyle.GHOST, fontSize = T.Label, leading = Icons.AutoMirrored.Filled.ArrowBack, onClick = onBack)
                Spacer(Modifier.width(24.dp))
                Text("밸류 패스포트", color = Palette.Ink, fontSize = T.Title, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(Dim.Gap))
            Column(
                Modifier.fillMaxWidth()
                    .background(Palette.Panel, RoundedCornerShape(Dim.Radius))
                    .padding(horizontal = 28.dp, vertical = 8.dp)
            ) {
                s.passport.forEachIndexed { i, row ->
                    HistoryRow(row.label, row.value)
                    Divider()
                }
                // 외관 — 평소엔 이벤트 감지만, 촬영은 선택(매각 준비 시 가치 근거)
                HistoryRow(
                    "외관 상태",
                    if (s.exterior.verified) "검증됨 · ${s.exterior.lastCheck}" else "미검증 · ${s.exterior.note}",
                    if (s.exterior.verified) Palette.Good else Palette.Ink
                )
            }
            Spacer(Modifier.height(12.dp))
            var extNotice by remember { mutableStateOf(false) }
            Btn("외관 촬영으로 검증하기 (선택)", BtnStyle.GHOST, fontSize = T.Label, trailing = IconNext) { extNotice = true }
            if (extNotice) {
                Text("데모: 10초 워크어라운드 촬영 가이드로 연결됩니다. 검증된 외관은 매각 시 가치 근거가 됩니다.", color = Palette.Good, fontSize = T.Caption)
            }
            Spacer(Modifier.weight(1f))
            Text("🛡 차량 데이터 기반 검증 리포트", color = Palette.InkMut, fontSize = T.Caption)
        }

        // 오른쪽 — 보정 자산가치 카드 + 가치 흐름(정부 잔가율 곡선, 시세 보정 전)
        val pd = rememberPriceData()
        Column(Modifier.weight(0.9f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(Dim.Gap)) {
            Column(
                Modifier.fillMaxWidth()
                    .background(Palette.Panel, RoundedCornerShape(Dim.Radius))
                    .padding(horizontal = 28.dp, vertical = 22.dp)
            ) {
                Text("상태 반영 보정 자산가치", color = Palette.Ink2, fontSize = T.Body)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(fmt(s.adjustedPriceManwon), color = Palette.Value2, fontSize = T.H1, fontWeight = FontWeight.Bold)
                    Text(" 만원", color = Palette.Ink2, fontSize = T.Body, modifier = Modifier.padding(bottom = 8.dp))
                    Spacer(Modifier.width(16.dp))
                    Text("* 데모 값", color = Palette.InkMut, fontSize = T.Caption, modifier = Modifier.padding(bottom = 10.dp))
                }
                Text(
                    "${if (diff >= 0) "▲" else "▼"} 표준 시세 대비 ${if (diff >= 0) "+" else "-"}${fmt(abs(diff))}만원 (관리 양호)",
                    color = Palette.Good, fontSize = T.Label
                )
                Spacer(Modifier.height(14.dp))
                Btn("현대 인증중고차 견적 받기", BtnStyle.PRIMARY, Modifier.fillMaxWidth(), trailing = IconNext) { }
            }
            if (pd != null) ValueFlowCard(s, pd, Modifier.fillMaxWidth().weight(1f))
        }
    }
}

@Composable
private fun HistoryRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color = Palette.Ink) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 72.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Palette.Ink2, fontSize = T.Label, modifier = Modifier.weight(1f))
        Text(value, color = valueColor, fontSize = T.Body, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun Divider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(Palette.Line))
}
