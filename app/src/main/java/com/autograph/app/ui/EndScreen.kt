package com.autograph.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autograph.app.data.TripReport
import com.autograph.app.data.VehicleSnapshot

/**
 * 운전 종료 — 주행 리포트(경로 맵 + 타일 4개). 웹 프로토타입과 동일 구성.
 * D/R→P 전환 직후 표시, '주차 중으로'로 안착.
 */
@Composable
fun EndScreen(snapshot: VehicleSnapshot, trip: TripReport, onDone: () -> Unit) {
    val smooth = trip.hardBrakes + trip.hardAccels
    val smoothWord = if (smooth <= 1) "양호" else if (smooth <= 3) "주의" else "거침"
    val smoothColor = if (smooth <= 1) Palette.Good else if (smooth <= 3) Palette.Warn else Palette.Crit

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("운전 종료 리포트", color = Palette.Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Spacer(Modifier.width(10.dp))
            Text("오늘 · 방금 도착", color = Palette.Ink2, fontSize = 13.sp)
            Spacer(Modifier.weight(1f))
            Text(
                "주차 중으로 →",
                color = Color(0xFF06101F), fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .background(Palette.Accent, RoundedCornerShape(8.dp))
                    .clickable(onClick = onDone)
                    .padding(horizontal = 16.dp, vertical = 9.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            // 경로 맵
            Box(
                Modifier.weight(1.3f).fillMaxHeight()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Palette.Panel)
                    .border(1.dp, Palette.Line, RoundedCornerShape(14.dp))
            ) {
                Canvas(Modifier.fillMaxSize()) { drawRouteMap() }
                Text(
                    "${trip.todayKm} km · ${trip.durationMin}분",
                    color = Palette.Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace,
                    modifier = Modifier.align(Alignment.TopStart).padding(10.dp)
                        .background(Color(0xCC0C1017), RoundedCornerShape(999.dp))
                        .border(1.dp, Palette.Line, RoundedCornerShape(999.dp))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                )
                Text(
                    "● 자택", color = Palette.Ink, fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.BottomStart).padding(14.dp)
                        .background(Color(0xCC0C1017), RoundedCornerShape(7.dp)).padding(horizontal = 8.dp, vertical = 3.dp)
                )
                Text(
                    "◆ 회사", color = Palette.Ink, fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.TopEnd).padding(14.dp)
                        .background(Color(0xCC0C1017), RoundedCornerShape(7.dp)).padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
            // 타일 2x2
            Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Tile("오늘 주행", "${trip.todayKm}", " km", "누적 ${fmt(snapshot.odometerKm)} km", Palette.Ink, Modifier.weight(1f))
                    Tile("운전 부드러움", smoothWord, "", "급제동 ${trip.hardBrakes} · 급가속 ${trip.hardAccels}", smoothColor, Modifier.weight(1f))
                }
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Tile(trip.effLabel, trip.eff, "", "직전 운행 기준", Palette.Value, Modifier.weight(1f))
                    Tile("이번 주 누적", "${trip.weekKm}", " km", "주간 리포트 금요일", Palette.Ink, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun Tile(label: String, value: String, unit: String, sub: String, valueColor: Color, modifier: Modifier) {
    Column(
        modifier.fillMaxHeight()
            .background(Palette.Panel, RoundedCornerShape(12.dp))
            .border(1.dp, Palette.Line, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(label, color = Palette.Ink2, fontSize = 13.sp)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, color = valueColor, fontSize = 26.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            if (unit.isNotEmpty()) Text(unit, color = Palette.Ink2, fontSize = 13.sp)
        }
        Spacer(Modifier.height(4.dp))
        Text(sub, color = Palette.InkMut, fontSize = 11.sp)
    }
}

/** 웹 SVG 경로 맵(viewBox 400x300)을 Canvas로 재현. */
private fun DrawScope.drawRouteMap() {
    val sx = size.width / 400f
    val sy = size.height / 300f
    val ss = (sx + sy) / 2f
    fun p(x: Float, y: Float) = Offset(x * sx, y * sy)

    drawRect(Color(0xFF0C1420), size = size)
    // 도로망(굵은)
    val road1 = Color(0xFF1A2635)
    listOf(
        floatArrayOf(-10f, 90f, 410f, 70f), floatArrayOf(-10f, 180f, 410f, 210f),
        floatArrayOf(90f, -10f, 120f, 310f), floatArrayOf(250f, -10f, 270f, 310f),
    ).forEach { drawLine(road1, p(it[0], it[1]), p(it[2], it[3]), 6f * ss, cap = StrokeCap.Round) }
    // 도로망(얇은)
    val road2 = Color(0xFF162130)
    listOf(
        floatArrayOf(-10f, 135f, 410f, 140f), floatArrayOf(180f, -10f, 195f, 310f), floatArrayOf(330f, -10f, 345f, 310f),
    ).forEach { drawLine(road2, p(it[0], it[1]), p(it[2], it[3]), 3f * ss) }
    // 공원
    drawRoundRect(
        Color(0xFF122A1F), topLeft = p(300f, 150f), size = Size(90f * sx, 70f * sy), alpha = 0.7f,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f * ss, 10f * ss)
    )
    // 강
    val river = Path().apply {
        moveTo(p(-10f, 250f).x, p(-10f, 250f).y)
        quadraticBezierTo(p(120f, 235f).x, p(120f, 235f).y, p(200f, 255f).x, p(200f, 255f).y)
        quadraticBezierTo(p(280f, 275f).x, p(280f, 275f).y, p(410f, 250f).x, p(410f, 250f).y)
    }
    drawPath(river, Color(0xFF12334A), alpha = 0.7f, style = Stroke(width = 14f * ss))
    // 경로
    val route = Path().apply {
        moveTo(p(66f, 250f).x, p(66f, 250f).y)
        cubicTo(p(110f, 210f).x, p(110f, 210f).y, p(120f, 150f).x, p(120f, 150f).y, p(180f, 140f).x, p(180f, 140f).y)
        cubicTo(p(240f, 130f).x, p(240f, 130f).y, p(250f, 90f).x, p(250f, 90f).y, p(330f, 72f).x, p(330f, 72f).y)
    }
    drawPath(route, Color(0xFF3D8BFF), style = Stroke(width = 6f * ss, cap = StrokeCap.Round))
    drawPath(route, Color(0xFF9FC6FF), alpha = 0.7f, style = Stroke(width = 2f * ss, cap = StrokeCap.Round))
    // 출발/도착
    drawCircle(Color(0xFF34D39A), 9f * ss, p(66f, 250f))
    drawCircle(Color(0xFFFFC24B), 9f * ss, p(330f, 72f))
}
