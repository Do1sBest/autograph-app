package com.autograph.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autograph.app.data.VehicleSnapshot
import kotlin.math.abs

/** 밸류 패스포트 — 주차 중 '자세히'로 진입. 웹 프로토타입과 동일(이력 + 보정가치 카드 + CTA). */
@Composable
fun PassportScreen(s: VehicleSnapshot, onBack: () -> Unit) {
    val diff = s.valueDiffManwon
    Row(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 좌측 — 이력
        Column(Modifier.weight(1.1f)) {
            Text(
                "← 주차 중으로",
                color = Palette.Ink2, fontSize = 14.sp,
                modifier = Modifier
                    .background(Palette.Panel2, RoundedCornerShape(8.dp))
                    .border(1.dp, Palette.Line, RoundedCornerShape(8.dp))
                    .clickable(onClick = onBack)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text("밸류 패스포트", color = Palette.Ink, fontSize = 26.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Spacer(Modifier.height(8.dp))
            s.passport.forEach { row ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(row.label, color = Palette.Ink2, fontSize = 15.sp, modifier = Modifier.weight(1f))
                    Text(row.value, color = Palette.Ink, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(Palette.Line))
            }
            Spacer(Modifier.height(12.dp))
            Text("🛡 차량 데이터 기반 검증 리포트", color = Palette.InkMut, fontSize = 13.sp)
        }

        // 우측 — 보정 자산가치 카드
        Column(
            Modifier.weight(0.9f)
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF15283A), Color(0xFF101A26))),
                    RoundedCornerShape(16.dp)
                )
                .border(1.dp, Palette.Accent.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .padding(horizontal = 22.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("상태 반영 보정 자산가치", color = Palette.Ink2, fontSize = 15.sp)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(fmt(s.adjustedPriceManwon), color = Palette.Value2, fontSize = 44.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text(" 만원", color = Palette.Ink2, fontSize = 15.sp)
            }
            Text(
                "${if (diff >= 0) "▲" else "▼"} 표준 시세 대비 ${if (diff >= 0) "+" else "-"}${fmt(abs(diff))}만원 (관리 양호)",
                color = Palette.Good, fontSize = 14.sp
            )
            Text(
                "현대 인증중고차 견적 받기 →",
                color = Color(0xFF05101F), fontSize = 15.sp, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
                    .background(Palette.Accent, RoundedCornerShape(10.dp))
                    .clickable { }
                    .padding(horizontal = 16.dp, vertical = 13.dp)
            )
            Text("* 데모용 예시 수치입니다", color = Palette.InkMut, fontSize = 12.sp)
        }
    }
}
