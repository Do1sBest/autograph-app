package com.autograph.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autograph.app.data.VehicleSnapshot

/** 밸류 패스포트 — 주차 중에서 '자세히'로 진입하는 상세(기어 상태 아님). */
@Composable
fun PassportScreen(s: VehicleSnapshot, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "← 주차 중으로",
            color = Palette.Ink2, fontSize = 14.sp,
            modifier = Modifier
                .background(Palette.Panel2, RoundedCornerShape(8.dp))
                .border(1.dp, Palette.Line, RoundedCornerShape(8.dp))
                .clickable(onClick = onBack)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        )
        Spacer(Modifier.height(14.dp))
        Column(
            Modifier.fillMaxSize()
                .background(Palette.Panel, RoundedCornerShape(14.dp))
                .border(1.dp, Palette.Line, RoundedCornerShape(14.dp))
                .padding(20.dp)
        ) {
            Text("밸류 패스포트", color = Palette.Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(s.valueNote, color = Palette.Ink2, fontSize = 13.sp)
            Spacer(Modifier.height(16.dp))
            s.passport.forEach { row ->
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Text(row.label, color = Palette.Ink2, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Text(row.value, color = Palette.Ink, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(Palette.Line))
            }
            Spacer(Modifier.height(16.dp))
            Text("※ 정비이력 연동(마이현대/블루핸즈)은 데모 이후 중장기 로드맵.", color = Palette.InkMut, fontSize = 11.sp)
        }
    }
}
