package com.autograph.app.ui

import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
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

/**
 * 누름 반응 — 살짝 눌림(0.97) + 진동 + 클릭음.
 * Pleos 가이드: 누르면 진동·소리로 반응하게.
 */
@Composable
fun Modifier.pressable(onClick: () -> Unit): Modifier {
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val sc by animateFloatAsState(if (pressed) 0.97f else 1f, tween(90), label = "press")
    val view = LocalView.current
    return this
        .scale(sc)
        .clickable(interactionSource = src, indication = androidx.compose.foundation.LocalIndication.current) {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            view.playSoundEffect(SoundEffectConstants.CLICK)
            onClick()
        }
}

/** 원형 컨디션 점수 링 */
@Composable
fun MiniScoreRing(score: Int, color: Color, modifier: Modifier = Modifier, numberSp: Int = 22) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(score) { started = true }
    val sweep by animateFloatAsState(
        targetValue = if (started) score / 100f else 0f,
        animationSpec = tween(900), label = "ring"
    )
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = size.minDimension * 0.10f
            val d = size.minDimension - stroke
            val topLeft = Offset((size.width - d) / 2f, (size.height - d) / 2f)
            val arcSize = Size(d, d)
            drawArc(Palette.Line, -90f, 360f, false, topLeft, arcSize, style = Stroke(stroke))
            drawArc(
                color, -90f, 360f * sweep, false, topLeft, arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
        }
        Text("$score", color = Palette.Ink, fontWeight = FontWeight.Bold, fontSize = numberSp.sp)
    }
}

/** 출처 표시 (실시간 / 추정 / —) — 작은 알약 */
@Composable
fun SourceTag(source: DataSource, modifier: Modifier = Modifier) {
    Text(
        source.label, color = Palette.InkMut, fontSize = T.Caption, fontWeight = FontWeight.Medium,
        modifier = modifier
            .background(Palette.Line.copy(alpha = 0.55f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 2.dp)
    )
}

/**
 * 버튼 — 높이 최소 76dp(차량용 터치 기준), 아이콘 + 글자(Pleos 가이드: 아이콘에 글자 같이).
 * PRIMARY = 채움(화면당 가장 중요한 행동 하나), TONAL = 연한 강조색 바탕(차분한 1차 행동),
 * GHOST = 회색 바탕(보조 행동).
 */
enum class BtnStyle { PRIMARY, TONAL, GHOST }

@Composable
fun Btn(
    text: String,
    style: BtnStyle = BtnStyle.TONAL,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = T.Body,
    leading: ImageVector? = null,
    trailing: ImageVector? = null,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)
    val (bg, fg) = when (style) {
        BtnStyle.PRIMARY -> Palette.Accent to Palette.OnAccent
        BtnStyle.TONAL -> Palette.Accent.copy(alpha = if (Palette.night) 0.20f else 0.12f) to (if (Palette.night) Palette.Ink else Palette.Accent)
        BtnStyle.GHOST -> Palette.Panel2 to Palette.Ink
    }
    Row(
        modifier
            .heightIn(min = Dim.Touch)
            .clip(shape)
            .background(bg, shape)
            .pressable(onClick)
            .padding(horizontal = 26.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (leading != null) {
            Icon(leading, null, tint = fg, modifier = Modifier.size(30.dp))
            Spacer(Modifier.width(10.dp))
        }
        Text(text, color = fg, fontSize = fontSize, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        if (trailing != null) {
            Spacer(Modifier.width(6.dp))
            Icon(trailing, null, tint = fg, modifier = Modifier.size(30.dp))
        }
    }
}

/**
 * 세그먼트 선택(기어 · 화면 모드 · 차종) — 한 줄 틀 안에서 하나만 켜짐.
 * 칸마다 76dp 이상.
 */
@Composable
fun <K> Segmented(
    items: List<Pair<K, String>>,
    selected: K,
    onSelect: (K) -> Unit,
    modifier: Modifier = Modifier,
    minWidth: androidx.compose.ui.unit.Dp = Dim.Touch,
    fontSize: TextUnit = T.Label,
) {
    Row(
        modifier
            .background(Palette.Panel2, RoundedCornerShape(20.dp))
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.forEach { (k, label) ->
            val on = k == selected
            Box(
                Modifier
                    .heightIn(min = Dim.Touch)
                    .widthIn(min = minWidth)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (on) Palette.Accent else Color.Transparent, RoundedCornerShape(16.dp))
                    .pressable { onSelect(k) }
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (on) Palette.OnAccent else Palette.Ink2,
                    fontSize = fontSize,
                    fontWeight = if (on) FontWeight.Bold else FontWeight.Medium,
                )
            }
        }
    }
}

/** 섹션 제목 + 개수 (예: 점검 필요 2) */
@Composable
fun SectionTitle(title: String, count: Int, color: Color, icon: ImageVector? = null, modifier: Modifier = Modifier) {
    Row(modifier.padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
        else Box(Modifier.size(12.dp).background(color, CircleShape))
        Spacer(Modifier.width(10.dp))
        Text(title, color = Palette.Ink, fontSize = T.Body, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(10.dp))
        Text("$count", color = color, fontSize = T.Body, fontWeight = FontWeight.Bold)
    }
}

/** 섹션 아이콘 */
val IconIssue: ImageVector get() = Icons.Filled.Warning
val IconOk: ImageVector get() = Icons.Filled.CheckCircle
val IconNext: ImageVector get() = Icons.AutoMirrored.Filled.KeyboardArrowRight

/**
 * 점검 필요 항목 카드 — 탭하면 케어 시트(해결 다음 단계).
 * 무엇이(이름) · 지금 상태(값) · 언제까지(권장 시점)를 한 카드에.
 */
@Composable
fun IssueCard(part: VehiclePart, onClick: (() -> Unit)?, modifier: Modifier = Modifier) {
    val c = part.status.color()
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 112.dp)
            .clip(shape)
            .background(Palette.Panel2, shape)
            .then(if (onClick != null) Modifier.pressable(onClick) else Modifier)
            .padding(end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(8.dp).height(112.dp).background(c))
        Spacer(Modifier.width(20.dp))
        Column(Modifier.weight(1f).padding(vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(part.name, color = Palette.Ink, fontSize = T.Title, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(14.dp))
                Text(part.value, color = c, fontSize = T.Body, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(10.dp))
                SourceTag(part.source)
            }
            part.care?.let {
                Spacer(Modifier.height(4.dp))
                Text(it.timing, color = Palette.Ink2, fontSize = T.Label)
            }
        }
        if (onClick != null) {
            Box(
                Modifier.size(56.dp).background(Palette.Panel, CircleShape),
                contentAlignment = Alignment.Center
            ) { Icon(IconNext, "자세히", tint = Palette.Ink, modifier = Modifier.size(34.dp)) }
        }
    }
}

/** 정상 항목 — 탭 없음, 작게 한 칸 */
@Composable
fun OkCell(part: VehiclePart, modifier: Modifier = Modifier) {
    val muted = part.status == PartStatus.MUTED
    Row(
        modifier
            .heightIn(min = 64.dp)
            .background(Palette.Panel, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(10.dp).background(part.status.color(), CircleShape))
        Spacer(Modifier.width(12.dp))
        Text(part.name, color = if (muted) Palette.InkMut else Palette.Ink, fontSize = T.Label, modifier = Modifier.weight(1f), maxLines = 1)
        Text(part.value, color = if (muted) Palette.InkMut else Palette.Ink2, fontSize = T.Label, fontWeight = FontWeight.Medium)
        if (!muted) {
            Spacer(Modifier.width(8.dp))
            SourceTag(part.source)
        }
    }
}

/** 차종 선택 (데모 전용 — 하단 데모 바) */
@Composable
fun ModelChipRow(keys: List<String>, selected: String, onSelect: (String) -> Unit) {
    Segmented(keys.map { it to keyName(it) }, selected, onSelect, minWidth = 96.dp)
}


/** 상세 보기 / 접기 — 기본 화면은 짧게, 나머지는 누르면 펼침 */
@Composable
fun DetailToggle(open: Boolean, modifier: Modifier = Modifier, onToggle: () -> Unit) {
    Btn(
        if (open) "상세 접기" else "상세 보기", BtnStyle.GHOST, modifier, fontSize = T.Label,
        trailing = if (open) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
        onClick = onToggle,
    )
}

/* ---------- 상세 정리용 부품: 문장 나열 대신 칸으로 ---------- */

/** 상세 묶음 — 작은 제목 + 연한 바탕 칸 */
@Composable
fun DetailSection(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier) {
        Text(title, color = Palette.Ink2, fontSize = T.Caption, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Column(
            Modifier.fillMaxWidth()
                .background(Palette.Panel2, RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            content = content
        )
    }
}

/** 숫자 한 칸 — 작은 이름 위, 큰 값 아래 */
@Composable
fun StatBox(label: String, value: String, sub: String = "", modifier: Modifier = Modifier, valueColor: Color = Palette.Ink) {
    Column(
        modifier
            .background(Palette.Panel2, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(label, color = Palette.InkMut, fontSize = T.Caption)
        Text(value, color = valueColor, fontSize = T.Body, fontWeight = FontWeight.Bold, maxLines = 2)
        if (sub.isNotEmpty()) Text(sub, color = Palette.Ink2, fontSize = T.Caption, maxLines = 1)
    }
}

/** 부품 표 한 줄 — 이름 | 품번 | 가격(오른쪽 정렬) */
@Composable
fun PartLine(name: String, ptno: String, price: String, priceColor: Color = Palette.Ink) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(name, color = Palette.Ink, fontSize = T.Label, fontWeight = FontWeight.Medium)
            if (ptno.isNotEmpty()) Text(ptno, color = Palette.InkMut, fontSize = T.Caption)
        }
        Text(price, color = priceColor, fontSize = T.Label, fontWeight = FontWeight.Bold)
    }
}

/** 출처 한 줄 — 짧게 */
@Composable
fun SourceLine(text: String) {
    Text("출처 · $text", color = Palette.InkMut, fontSize = T.Caption, maxLines = 2)
}
