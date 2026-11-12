package com.autograph.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autograph.app.data.TripReport
import com.autograph.app.data.VehicleSnapshot

/**
 * 운전 종료 — 맨 위에 '이번 주행이 가치에 준 영향' 한 줄, 아래 경로·주행 기록 | 타일 4개.
 * 주행 중에 모아 둔 속도·브레이크 기록을 여기서 그래프로 보여 준다(정차 상태).
 */
@Composable
fun EndScreen(
    snapshot: VehicleSnapshot,
    trip: TripReport,
    speedLog: List<Float>,
    brakeLog: List<Float>,
    onDone: () -> Unit,
) {
    val smooth = trip.hardBrakes + trip.hardAccels
    val smoothWord = if (smooth <= 1) "양호" else if (smooth <= 3) "주의" else "거침"
    val smoothColor = if (smooth <= 1) Palette.Good else if (smooth <= 3) Palette.Warn else Palette.Crit
    val impactColor = if (trip.valueImpact.contains("감소")) Palette.Warn else Palette.Good

    Column(Modifier.fillMaxSize().padding(horizontal = Dim.Pad, vertical = 8.dp)) {
        // 핵심 한 줄 + 끝내기
        Row(
            Modifier.fillMaxWidth()
                .background(Palette.Panel, RoundedCornerShape(Dim.Radius))
                .padding(horizontal = 28.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("운전 종료 · 오늘 방금 도착", color = Palette.Ink2, fontSize = T.Label)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("차량 가치 영향", color = Palette.Ink, fontSize = T.Title, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 10.dp))
                    Spacer(Modifier.width(20.dp))
                    Text(trip.valueImpact, color = impactColor, fontSize = T.H1, fontWeight = FontWeight.Bold)
                }
            }
            Btn("주차 중으로", BtnStyle.PRIMARY, Modifier.widthIn(min = 240.dp), trailing = IconNext, onClick = onDone)
        }
        Spacer(Modifier.height(Dim.Gap))
        Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dim.Gap)) {
            // 경로 + 주행 기록
            Column(Modifier.weight(1.15f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(Dim.Gap)) {
                Box(
                    Modifier.weight(1f).fillMaxWidth()
                        .clip(RoundedCornerShape(Dim.Radius))
                        .background(Palette.Panel)
                ) {
                    Canvas(Modifier.fillMaxSize()) { drawRouteMap() }
                    Text(
                        "${trip.todayKm} km · ${trip.durationMin}분",
                        color = Palette.Ink, fontSize = T.Label, fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.TopStart).padding(14.dp)
                            .background(Palette.Overlay, RoundedCornerShape(999.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    Text(
                        "● 자택", color = Palette.Ink, fontSize = T.Caption,
                        modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
                            .background(Palette.Overlay, RoundedCornerShape(10.dp)).padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                    Text(
                        "◆ 회사", color = Palette.Ink, fontSize = T.Caption,
                        modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                            .background(Palette.Overlay, RoundedCornerShape(10.dp)).padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                if (speedLog.size >= 2) TripTrace(speedLog, brakeLog, Modifier.height(210.dp).fillMaxWidth())
            }
            // 타일 2x2
            Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(Dim.Gap)) {
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(Dim.Gap)) {
                    Tile("오늘 주행", "${trip.todayKm}", "km", "누적 ${fmt(snapshot.odometerKm)} km", Palette.Ink, Modifier.weight(1f))
                    Tile("운전 부드러움", smoothWord, "", "급제동 ${trip.hardBrakes} · 급가속 ${trip.hardAccels}", smoothColor, Modifier.weight(1f))
                }
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(Dim.Gap)) {
                    Tile(trip.effLabel, trip.eff, "", "직전 운행 기준", Palette.Value, Modifier.weight(1f))
                    Tile("이번 주 누적", "${trip.weekKm}", "km", "주간 리포트 금요일", Palette.Ink, Modifier.weight(1f))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun Tile(label: String, value: String, unit: String, sub: String, valueColor: Color, modifier: Modifier) {
    Column(
        modifier.fillMaxHeight()
            .background(Palette.Panel, RoundedCornerShape(Dim.Radius))
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(label, color = Palette.Ink2, fontSize = T.Label)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, color = valueColor, fontSize = T.H1, fontWeight = FontWeight.Bold, maxLines = 1)
            if (unit.isNotEmpty()) Text(" $unit", color = Palette.Ink2, fontSize = T.Body, modifier = Modifier.padding(bottom = 8.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(sub, color = Palette.InkMut, fontSize = T.Caption)
    }
}

/** 이번 주행 속도 기록(1초 간격) + 브레이크 구간 표시 — 정지 그림 */
@Composable
private fun TripTrace(speed: List<Float>, brake: List<Float>, modifier: Modifier) {
    Column(
        modifier.background(Palette.Panel, RoundedCornerShape(Dim.Radius))
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("주행 기록", color = Palette.Ink, fontSize = T.Label, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(16.dp))
            Text("— 속도", color = Palette.Accent, fontSize = T.Caption)
            Spacer(Modifier.width(14.dp))
            Text("▮ 브레이크", color = Palette.Crit, fontSize = T.Caption)
            Spacer(Modifier.weight(1f))
            Text("최고 ${speed.maxOrNull()?.toInt() ?: 0} km/h", color = Palette.Ink2, fontSize = T.Caption)
        }
        Spacer(Modifier.height(8.dp))
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val maxV = (speed.maxOrNull() ?: 1f).coerceAtLeast(40f) * 1.1f
            val n = speed.size
            // 브레이크 막대
            brake.forEachIndexed { i, b ->
                if (b > 0.05f) {
                    val x = w * i / (n - 1)
                    drawRect(Palette.Crit.copy(alpha = 0.25f + 0.5f * b), topLeft = Offset(x - 2f, h * (1f - b)), size = Size(4f, h * b))
                }
            }
            val path = Path()
            speed.forEachIndexed { i, v ->
                val x = w * i / (n - 1)
                val y = h - (v / maxV) * h
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            val fill = Path().apply { addPath(path); lineTo(w, h); lineTo(0f, h); close() }
            drawPath(fill, brush = Brush.verticalGradient(listOf(Palette.Accent.copy(alpha = 0.22f), Palette.Accent.copy(alpha = 0f))))
            drawPath(path, Palette.Accent, style = Stroke(width = 3f, cap = StrokeCap.Round))
        }
    }
}

/** 웹 SVG 경로 맵(viewBox 400x300)을 Canvas로 재현. */
private fun DrawScope.drawRouteMap() {
    val sx = size.width / 400f
    val sy = size.height / 300f
    val ss = (sx + sy) / 2f
    fun p(x: Float, y: Float) = Offset(x * sx, y * sy)

    val m = Palette.s
    drawRect(m.mapBg, size = size)
    val road1 = m.mapRoad1
    listOf(
        floatArrayOf(-10f, 90f, 410f, 70f), floatArrayOf(-10f, 180f, 410f, 210f),
        floatArrayOf(90f, -10f, 120f, 310f), floatArrayOf(250f, -10f, 270f, 310f),
    ).forEach { drawLine(road1, p(it[0], it[1]), p(it[2], it[3]), 6f * ss, cap = StrokeCap.Round) }
    val road2 = m.mapRoad2
    listOf(
        floatArrayOf(-10f, 135f, 410f, 140f), floatArrayOf(180f, -10f, 195f, 310f), floatArrayOf(330f, -10f, 345f, 310f),
    ).forEach { drawLine(road2, p(it[0], it[1]), p(it[2], it[3]), 3f * ss) }
    drawRoundRect(
        m.mapPark, topLeft = p(300f, 150f), size = Size(90f * sx, 70f * sy), alpha = 0.7f,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f * ss, 10f * ss)
    )
    val river = Path().apply {
        moveTo(p(-10f, 250f).x, p(-10f, 250f).y)
        quadraticBezierTo(p(120f, 235f).x, p(120f, 235f).y, p(200f, 255f).x, p(200f, 255f).y)
        quadraticBezierTo(p(280f, 275f).x, p(280f, 275f).y, p(410f, 250f).x, p(410f, 250f).y)
    }
    drawPath(river, m.mapRiver, alpha = 0.7f, style = Stroke(width = 14f * ss))
    val route = Path().apply {
        moveTo(p(66f, 250f).x, p(66f, 250f).y)
        cubicTo(p(110f, 210f).x, p(110f, 210f).y, p(120f, 150f).x, p(120f, 150f).y, p(180f, 140f).x, p(180f, 140f).y)
        cubicTo(p(240f, 130f).x, p(240f, 130f).y, p(250f, 90f).x, p(250f, 90f).y, p(330f, 72f).x, p(330f, 72f).y)
    }
    drawPath(route, m.route, style = Stroke(width = 6f * ss, cap = StrokeCap.Round))
    drawPath(route, m.routeGlow, alpha = 0.7f, style = Stroke(width = 2f * ss, cap = StrokeCap.Round))
    drawCircle(Color(0xFF34D39A), 9f * ss, p(66f, 250f))
    drawCircle(Color(0xFFFFC24B), 9f * ss, p(330f, 72f))
}
