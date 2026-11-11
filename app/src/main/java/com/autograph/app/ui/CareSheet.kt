package com.autograph.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.autograph.app.data.VehiclePart

/**
 * 케어 시트 — 문제 항목을 탭했을 때만 열리는 '해결 다음 단계'.
 * 광고가 아니라 상태의 연장선: 왜 → 언제 → 무엇을 → (예약 1차 / 정품 부품 2차).
 * 메인 화면에는 구매 버튼을 두지 않는다.
 */
@Composable
fun PartCareDialog(part: VehiclePart, model: String, onDismiss: () -> Unit) {
    val care = part.care ?: return
    var notice by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .widthIn(max = 540.dp)
                .background(Palette.Panel, RoundedCornerShape(16.dp))
                .border(1.dp, Palette.Line, RoundedCornerShape(16.dp))
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(part.name, color = Palette.Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(10.dp))
                Text(part.value, color = Palette.Warn, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Text(
                    "닫기", color = Palette.Ink2, fontSize = 13.sp,
                    modifier = Modifier.clickable(onClick = onDismiss).padding(6.dp)
                )
            }
            Text("$model · ${part.source.label} 데이터 기준", color = Palette.InkMut, fontSize = 12.sp)

            InfoLine("왜 지금인가요", care.reason)
            InfoLine("권장 시점", care.timing)
            InfoLine("권장 사양", care.spec)
            InfoLine("정품 부품", care.partNo)

            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 1차: 예약(케어) — 차분한 아웃라인
                Text(
                    "가까운 블루핸즈 예약",
                    color = Palette.Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .border(1.dp, Palette.Accent, RoundedCornerShape(10.dp))
                        .clickable { notice = "데모: 블루핸즈 예약 화면으로 연결됩니다." }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
                Spacer(Modifier.width(16.dp))
                // 2차: 정품 부품 — 텍스트 링크
                Text(
                    "정품 부품 보기 ›",
                    color = Palette.Ink2, fontSize = 13.sp,
                    modifier = Modifier
                        .clickable { notice = "데모: 모비스 정품 부품 페이지로 연결됩니다." }
                        .padding(vertical = 10.dp)
                )
            }
            notice?.let { Text(it, color = Palette.Good, fontSize = 12.sp) }
            Text(
                "※ 부품번호·사양은 데모용 예시입니다. 실제 연동 시 차대번호(VIN) 기준 정품 카탈로그로 확정합니다.",
                color = Palette.InkMut, fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, color = Palette.Ink2, fontSize = 13.sp, modifier = Modifier.width(110.dp))
        Text(value, color = Palette.Ink, fontSize = 13.sp, modifier = Modifier.weight(1f))
    }
}
