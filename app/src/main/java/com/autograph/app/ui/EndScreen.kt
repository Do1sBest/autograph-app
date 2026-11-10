package com.autograph.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autograph.app.data.TripReport
import com.autograph.app.data.VehicleSnapshot

/**
 * 운전 종료 — 방금 끝낸 주행 리포트. (D/R→P 전환 직후 표시)
 * 확인을 누르면 주차 중으로 안착.
 */
@Composable
fun EndScreen(snapshot: VehicleSnapshot, trip: TripReport, onDone: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("운전 종료 · 오늘의 주행", color = Palette.Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text(
                "주차 중으로 →",
                color = Color(0xFF06101F), fontSize = 14.sp,
                modifier = Modifier
                    .background(Palette.Accent, RoundedCornerShape(8.dp))
                    .clickable(onClick = onDone)
                    .padding(horizontal = 16.dp, vertical = 9.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // 경로 맵
            Column(
                Modifier.weight(1.4f).fillMaxHeight()
                    .background(Palette.Panel, RoundedCornerShape(14.dp))
                    .border(1.dp, Palette.Line, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Text("주행 경로", color = Palette.Ink2, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                RouteMap(Modifier.weight(1f).fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Dot(Palette.Good); Spacer(Modifier.width(6.dp))
                    Text("자택", color = Palette.Ink2, fontSize = 12.sp)
                    Spacer(Modifier.weight(1f))
                    Text("${trip.todayKm} km · ${trip.durationMin}분", color = Palette.Ink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.weight(1f))
                    Text("회사", color = Palette.Ink2, fontSize = 12.sp)
                    Spacer(Modifier.width(6.dp)); Dot(Palette.Accent)
                }
            }
            // 2x2 지표
            Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Tile("오늘 주행", "${trip.todayKm} km", Modifier.weight(1f))
                    Tile("주행 시간", "${trip.durationMin} 분", Modifier.weight(1f))
                }
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Tile("급제동 · 급가속", "${trip.hardBrakes} · ${trip.hardAccels}", Modifier.weight(1f),
                        warn = trip.hardBrakes + trip.hardAccels > 2)
                    Tile("가치 영향", trip.valueImpact, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun RouteMap(modifier: Modifier) {
    Box(
        modifier
            .background(Palette.Bg, RoundedCornerShape(10.dp))
            .border(1.dp, Palette.Line, RoundedCornerShape(10.dp))
    ) {
        Canvas(Modifier.fillMaxSize().padding(16.dp)) {
            val w = size.width; val h = size.height
            val pts = listOf(
                Offset(0.06f * w, 0.82f * h),
                Offset(0.26f * w, 0.60f * h),
                Offset(0.40f * w, 0.66f * h),
                Offset(0.58f * w, 0.36f * h),
                Offset(0.74f * w, 0.44f * h),
                Offset(0.94f * w, 0.16f * h),
            )
            val path = Path().apply {
                moveTo(pts.first().x, pts.first().y)
                for (i in 1 until pts.size) lineTo(pts[i].x, pts[i].y)
            }
            drawPath(path, Palette.Accent, style = Stroke(width = 5f, cap = StrokeCap.Round))
            drawCircle(Palette.Good, 9f, pts.first())
            drawCircle(Palette.Accent, 9f, pts.last())
        }
    }
}

@Composable
private fun Dot(color: Color) {
    Box(Modifier.size(9.dp).background(color, CircleShape))
}

@Composable
private fun Tile(label: String, value: String, modifier: Modifier, warn: Boolean = false) {
    Column(
        modifier.fillMaxHeight()
            .background(Palette.Panel, RoundedCornerShape(12.dp))
            .border(1.dp, Palette.Line, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(label, color = Palette.InkMut, fontSize = 12.sp)
        Spacer(Modifier.height(6.dp))
        Text(value, color = if (warn) Palette.Warn else Palette.Ink, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}
