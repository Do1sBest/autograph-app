package com.autograph.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autograph.app.data.VehicleSnapshot
import kotlinx.coroutines.delay
import kotlin.math.sin

/**
 * 주행 중 — 라이브 트레이스(속도·브레이크·스티어링).
 * 실데이터 연동 전까지는 부드러운 합성 신호로 시연(웹 프로토타입과 동일 콘셉트).
 * 실연동 시 Pleos Odometer/Brake/Steeringwheel 값을 shift()로 밀어넣으면 됨.
 */
@Composable
fun DrivingScreen(snapshot: VehicleSnapshot) {
    val n = 140
    val speedBuf = remember { FloatArray(n) { 46f } }
    val brakeBuf = remember { FloatArray(n) }
    val steerBuf = remember { FloatArray(n) { 0.5f } }
    var frame by remember { mutableIntStateOf(0) }
    var speed by remember { mutableFloatStateOf(46f) }
    var steerNow by remember { mutableFloatStateOf(0.5f) }
    var brakeNow by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var t = 0.0
        while (true) {
            t += 0.06
            val target = (48 + 26 * sin(t * 0.22) + 9 * sin(t * 0.63)).toFloat().coerceIn(0f, 150f)
            speed += (target - speed) * 0.08f
            brakeNow = (0.7f * (sin(t * 0.8).toFloat() - 0.55f)).coerceIn(0f, 1f)
            steerNow = (0.5 + 0.30 * sin(t * 0.5) + 0.12 * sin(t * 1.3)).toFloat().coerceIn(0f, 1f)
            shift(speedBuf, speed)
            shift(brakeBuf, brakeNow)
            shift(steerBuf, steerNow)
            frame++
            delay(55)
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        // 상단: 기어/속도
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("D", color = Palette.Accent, fontSize = 30.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Spacer(Modifier.width(14.dp))
            Text("${speed.toInt()}", color = Palette.Ink, fontSize = 40.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text(" km/h", color = Palette.Ink2, fontSize = 14.sp)
            Spacer(Modifier.weight(1f))
            Text("주행 중 · 라이브 텔레메트리", color = Palette.Ink2, fontSize = 13.sp)
        }
        Spacer(Modifier.height(10.dp))
        // 라이브 트레이스 3종
        Column(
            Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Trace("속도", "${speed.toInt()} km/h", speedBuf, frame, 150f, Palette.Accent, Modifier.weight(1f))
            Trace("브레이크", "${(brakeNow * 100).toInt()}%", brakeBuf, frame, 1f, Palette.Crit, Modifier.weight(1f))
            Trace("스티어링", steerLabel(steerNow), steerBuf, frame, 1f, Palette.Value2, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        // 하단 스트립
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val ev = snapshot.isEv
            StripTile(if (ev) "배터리" else "연료", if (ev) "72%" else "58%", Modifier.weight(1f))
            StripTile("타이어(뒤우)", "2.1 bar", Modifier.weight(1f), warn = true)
            StripTile("주행 모드", if (ev) "ECO" else "COMFORT", Modifier.weight(1f))
            StripTile("외기", "14°C", Modifier.weight(1f))
        }
    }
}

private fun steerLabel(v: Float): String {
    val d = ((v - 0.5f) * 2 * 100).toInt()
    return when {
        d > 4 -> "우 $d%"
        d < -4 -> "좌 ${-d}%"
        else -> "중립"
    }
}

private fun shift(a: FloatArray, v: Float) {
    System.arraycopy(a, 1, a, 0, a.size - 1)
    a[a.size - 1] = v
}

@Composable
private fun Trace(
    label: String, value: String, buf: FloatArray, frame: Int,
    maxV: Float, color: Color, modifier: Modifier,
) {
    Column(
        modifier
            .fillMaxWidth()
            .background(Palette.Panel, RoundedCornerShape(12.dp))
            .border(1.dp, Palette.Line, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = Palette.Ink2, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Text(value, color = color, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
        Spacer(Modifier.height(6.dp))
        Canvas(Modifier.fillMaxSize()) {
            frame.let { } // 프레임 상태를 읽어 매 틱 재그리기
            val w = size.width
            val h = size.height
            drawLine(Palette.Line, Offset(0f, h), Offset(w, h), 1f) // 베이스라인
            val path = Path()
            var lastX = 0f
            var lastY = h
            for (i in buf.indices) {
                val x = w * i / (buf.size - 1)
                val y = h - (buf[i] / maxV).coerceIn(0f, 1f) * (h * 0.92f) - h * 0.04f
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                lastX = x; lastY = y
            }
            // 면적 그라데이션
            val fill = Path().apply { addPath(path); lineTo(w, h); lineTo(0f, h); close() }
            drawPath(fill, brush = Brush.verticalGradient(listOf(color.copy(alpha = 0.26f), color.copy(alpha = 0f))))
            drawPath(path, color, style = Stroke(width = 3f, cap = StrokeCap.Round))
            drawCircle(color, 4.5f, Offset(lastX, lastY)) // 현재값 점
        }
    }
}

@Composable
private fun StripTile(label: String, value: String, modifier: Modifier, warn: Boolean = false) {
    Column(
        modifier
            .background(Palette.Panel, RoundedCornerShape(10.dp))
            .border(1.dp, Palette.Line, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(label, color = Palette.InkMut, fontSize = 11.sp)
        Text(
            value,
            color = if (warn) Palette.Warn else Palette.Ink,
            fontSize = 16.sp, fontWeight = FontWeight.Bold
        )
    }
}
