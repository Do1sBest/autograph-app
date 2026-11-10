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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autograph.app.data.DataSource
import com.autograph.app.data.PartStatus
import com.autograph.app.data.VehiclePart

/** 1,000 단위 콤마 */
fun fmt(n: Int): String = "%,d".format(n)

fun keyName(key: String): String = when (key) {
    "ioniq5" -> "아이오닉 5"
    "elantra" -> "아반떼"
    else -> "투싼"
}

/** 원형 컨디션 점수 링 */
@Composable
fun MiniScoreRing(score: Int, color: Color, modifier: Modifier = Modifier, numberSp: Int = 22) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = size.minDimension * 0.11f
            val d = size.minDimension - stroke
            val topLeft = Offset((size.width - d) / 2f, (size.height - d) / 2f)
            val arcSize = Size(d, d)
            drawArc(Palette.Line, -90f, 360f, false, topLeft, arcSize, style = Stroke(stroke))
            drawArc(
                color, -90f, 360f * (score / 100f), false, topLeft, arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
        }
        Text(
            "$score", color = Palette.Ink, fontWeight = FontWeight.Bold,
            fontSize = numberSp.sp
        )
    }
}

/** 출처 태그 (실시간 / 추정 / —) */
@Composable
fun SourceTag(source: DataSource, modifier: Modifier = Modifier) {
    Text(
        source.label,
        color = Palette.InkMut,
        fontSize = 11.sp,
        modifier = modifier
            .border(1.dp, Palette.Line, RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 1.dp)
    )
}

/** 상태 맵 부품 1행 */
@Composable
fun PartRow(index: Int, part: VehiclePart) {
    val dot = part.status.color()
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(
            Modifier.size(20.dp).background(dot, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("${index + 1}", color = Color(0xFF06101F), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(8.dp))
        Text(part.name, color = Palette.Ink, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(
            part.value,
            color = if (part.status == PartStatus.WARN) Palette.Warn else Palette.Ink2,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.width(6.dp))
        SourceTag(part.source)
    }
}

/** 차종 선택 칩 Row */
@Composable
fun ModelChipRow(keys: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        keys.forEach { k ->
            val on = k == selected
            Text(
                keyName(k),
                color = if (on) Color(0xFF06101F) else Palette.Ink2,
                fontSize = 12.sp,
                fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier
                    .background(if (on) Palette.Accent else Palette.Panel2, RoundedCornerShape(999.dp))
                    .border(1.dp, if (on) Palette.Accent else Palette.Line, RoundedCornerShape(999.dp))
                    .clickable { onSelect(k) }
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            )
        }
    }
}
