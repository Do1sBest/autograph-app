package com.autograph.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autograph.app.data.PriceData
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 가치 흐름 그래프 — 회색 = 같은 연식 평균(정부 잔가율), 파랑 = 내 차(관리 반영).
 * 관리를 잘 한 차는 평균 위로 올라가고, 그 사이를 초록으로 칠함(관리로 지킨 가치).
 * premium = 같은 연식 평균 대비 내 차 비율(예: +0.041). 지금은 데모 값(시세 보정 전).
 * progress = 0→1 이면 왼쪽부터 그려짐(스토리 카드 움직임).
 */
@Composable
fun ValueChart(pd: PriceData, age: Int, premium: Double, progress: Float, modifier: Modifier, big: Boolean = false,
               maxYear: Float = 10f, yMin: Double = 0.0) {
    val tm = rememberTextMeasurer()
    val ink2 = Palette.Ink2; val mut = Palette.InkMut; val line = Palette.Line; val accent = Palette.Accent
    val warn = Palette.Warn; val area = if (premium >= 0) Palette.Good else Palette.Crit; val onAccent = Palette.OnAccent
    val avgColor = Palette.InkMut
    val fs = if (big) 18.sp else 15.sp
    val label = TextStyle(fontFamily = Pretendard, fontSize = fs, color = mut)
    val labelInk = TextStyle(fontFamily = Pretendard, fontSize = fs, color = ink2, fontWeight = FontWeight.SemiBold)
    val bubble = TextStyle(fontFamily = Pretendard, fontSize = if (big) 22.sp else 17.sp, color = onAccent, fontWeight = FontWeight.Bold)

    Canvas(modifier) {
        val padL = (if (big) 64 else 52).dp.toPx(); val padR = 12.dp.toPx()
        val padT = (if (big) 44 else 34).dp.toPx(); val padB = (if (big) 40 else 32).dp.toPx()
        val w = size.width - padL - padR; val h = size.height - padT - padB
        val yMax = 1.08
        fun X(y: Float) = padL + w * y / maxYear
        fun Y(r: Double) = padT + h * (1f - ((r - yMin) / (yMax - yMin)).toFloat())

        // 눈금: 세로 %(신차 대비), 가로 연수
        listOf(0.0, 0.25, 0.5, 0.75, 1.0).filter { it >= yMin }.forEach { p ->
            drawLine(line.copy(alpha = 0.7f), Offset(padL, Y(p)), Offset(padL + w, Y(p)), 1.5f)
            val t = tm.measure("${(p * 100).roundToInt()}%", label)
            drawText(t, topLeft = Offset(padL - t.size.width - 10f, Y(p) - t.size.height / 2f))
        }
        (0..maxYear.toInt() step (if (maxYear > 6f) 2 else 1)).forEach { yr ->
            val t = tm.measure(if (yr == 0) "출고" else "${yr}년", label)
            drawText(t, topLeft = Offset(X(yr.toFloat()) - t.size.width / 2f, padT + h + 8f))
        }

        // 점: 평균(정부 잔가율), 내 차 = 평균 × (1 + 관리 차이). 관리 차이는 출고 0 → 지금까지 점점 벌어지고 이후 유지(관리 유지 시)
        val xs = listOf(0f, 0.5f) + (1..maxYear.toInt()).map { it.toFloat() }
        val avg = listOf(1.0, pd.rate(0)) + (1..maxYear.toInt()).map { pd.rate(it) }
        val now = if (age == 0) 0.5f else age.toFloat()
        val mine = xs.mapIndexed { i, x -> avg[i] * (1 + premium * min(1f, x / now)) }

        fun smooth(ys: List<Double>, from: Int = 0, to: Int = xs.lastIndex): Path {
            val p = Path()
            val pts = (from..to).map { Offset(X(xs[it]), Y(ys[it])) }
            p.moveTo(pts[0].x, pts[0].y)
            for (i in 0 until pts.lastIndex) {
                val p0 = pts[maxOf(i - 1, 0)]; val p1 = pts[i]; val p2 = pts[i + 1]; val p3 = pts[minOf(i + 2, pts.lastIndex)]
                val c1 = Offset(p1.x + (p2.x - p0.x) / 6f, p1.y + (p2.y - p0.y) / 6f)
                val c2 = Offset(p2.x - (p3.x - p1.x) / 6f, p2.y - (p3.y - p1.y) / 6f)
                p.cubicTo(c1.x, c1.y, c2.x, c2.y, p2.x, p2.y)
            }
            return p
        }

        clipRect(right = padL + w * progress.coerceIn(0f, 1f)) {
            // 평균 아래 은은한 그라데이션
            val avgPath = smooth(avg)
            val fill = Path().apply { addPath(avgPath); lineTo(X(maxYear), padT + h); lineTo(X(0f), padT + h); close() }
            drawPath(fill, Brush.verticalGradient(listOf(accent.copy(alpha = 0.16f), accent.copy(alpha = 0f)), startY = padT, endY = padT + h))
            // 관리로 지킨 가치(평균과 내 차 사이)
            val mineP = smooth(mine)
            val band = Path().apply {
                addPath(mineP)
                for (i in xs.indices.reversed()) lineTo(X(xs[i]), Y(avg[i]))
                close()
            }
            drawPath(band, area.copy(alpha = 0.35f))
            // 평균선(회색), 출고 직후 구간(주황)
            drawPath(avgPath, avgColor, style = Stroke(width = 4f, cap = StrokeCap.Round))
            // 내 차: 지금까지 실선, 이후 점선(관리 유지 시)
            val nowIdx = xs.indexOfFirst { it >= now }.coerceAtLeast(1)
            drawPath(smooth(mine, 0, nowIdx), accent, style = Stroke(width = 7f, cap = StrokeCap.Round))
            drawPath(smooth(mine, nowIdx, xs.lastIndex), accent.copy(alpha = 0.75f),
                style = Stroke(width = 5f, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 12f))))
            drawLine(warn, Offset(X(0f), Y(1.0)), Offset(X(0.5f), Y(pd.rate(0))), 9f, cap = StrokeCap.Round)
        }

        // 출고 직후 표시
        if (progress > 0.1f) {
            val t = tm.measure("출고 직후 −${((1 - pd.rate(0)) * 100).roundToInt()}%", TextStyle(fontFamily = Pretendard, fontSize = fs, color = warn, fontWeight = FontWeight.Bold))
            drawText(t, topLeft = Offset(X(0.6f) + 8f, Y(pd.rate(0)) - t.size.height - 6f))
        }
        // 인증중고차 기준선
        if (pd.cpoYears <= maxYear) {
            val cx = X(pd.cpoYears.toFloat())
            drawLine(ink2.copy(alpha = 0.6f), Offset(cx, padT), Offset(cx, padT + h), 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
            val ct = tm.measure("인증중고차 기준 ${pd.cpoYears}년", labelInk)
            drawText(ct, topLeft = Offset(cx - ct.size.width / 2f, padT - ct.size.height - 6f))
        }

        // 지금 + 말풍선
        if (progress >= min(1f, now / maxYear)) {
            val i = xs.indexOfFirst { it >= now }.coerceAtLeast(1)
            val c = Offset(X(xs[i]), Y(mine[i]))
            drawCircle(accent.copy(alpha = 0.22f), if (big) 30f else 22f, c)
            drawCircle(accent, if (big) 14f else 10f, c)
            val sign = if (premium >= 0) "+" else "−"
            val bt = tm.measure(
                if (premium == 0.0) "지금 ${(pd.rate(age) * 100).roundToInt()}%"          // 평균만: 신차 대비 남은 비율
                else "지금 $sign${"%.1f".format(abs(premium) * 100)}%", bubble)
            val bw = bt.size.width + 28f; val bh = bt.size.height + 14f
            val bx = (c.x - bw / 2f).coerceIn(padL, padL + w - bw); val by = c.y - bh - 22f
            drawRoundRect(accent, Offset(bx, by), Size(bw, bh), CornerRadius(bh / 2f))
            drawText(bt, topLeft = Offset(bx + 14f, by + 7f))
        }
    }
}
