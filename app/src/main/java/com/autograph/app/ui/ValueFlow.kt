package com.autograph.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autograph.app.data.PriceData
import com.autograph.app.data.VehicleSnapshot
import java.util.Calendar
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun rememberPriceData(): PriceData? {
    val ctx = LocalContext.current
    return remember { PriceData.load(ctx) }
}

/** 만원 단위 반올림 표기 */
fun manwon(won: Double): String = "%,d".format((won / 10_000).roundToInt())

/**
 * 가치 흐름 — 신차가 × 정부 잔가율 곡선(모든 차 공통 감가 모양) 위에 '지금' 위치와 인증중고차 기준점.
 * 실제 시세 샘플로 맞추는 차종 보정 전이라 '시세 보정 전'으로 표시.
 */
@Composable
fun ValueFlowCard(s: VehicleSnapshot, pd: PriceData, modifier: Modifier = Modifier) {
    val car = pd.cars[s.key] ?: return
    val age = max(0, Calendar.getInstance().get(Calendar.YEAR) - s.year)     // 연식 기준(등록월 모름)
    val base = car.newPrice * pd.rate(age)
    val maxYear = 10
    val kmLeft = pd.cpoKm - s.odometerKm
    val yrLeft = pd.cpoYears - age

    Column(
        modifier
            .background(Palette.Panel, RoundedCornerShape(Dim.Radius))
            .padding(horizontal = 24.dp, vertical = 18.dp)
    ) {
        Row {
            Text("가치 흐름", color = Palette.Ink, fontSize = T.Body, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(12.dp))
            Text("정부 잔가율 기준 · 시세 보정 전", color = Palette.InkMut, fontSize = T.Caption,
                modifier = Modifier.align(androidx.compose.ui.Alignment.Bottom).padding(bottom = 3.dp))
        }
        Spacer(Modifier.height(8.dp))
        val premium = if (s.standardPriceManwon > 0) s.valueDiffManwon.toDouble() / s.standardPriceManwon else 0.0
        ValueChart(pd, age, premium, 1f, Modifier.fillMaxWidth().weight(1f), maxYear = 6f, yMin = 0.25)
        Spacer(Modifier.height(6.dp))
        Legend()
        Spacer(Modifier.height(10.dp))
        // 기본 — 한 줄
        Text(
            "기준가 ${manwon(base)}만원 · " + if (kmLeft > 0 && yrLeft > 0) "인증중고차 기준까지 ${fmt(kmLeft)}km" else "인증중고차 기준 지남",
            color = Palette.Ink, fontSize = T.Label, fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        // 상세 — 스토리 카드(드래그로 넘김)
        var open by remember { mutableStateOf(false) }
        Btn("상세 보기", BtnStyle.GHOST, fontSize = T.Label, trailing = IconNext) { open = true }
        if (open) StoryViewer("내 ${s.model} · 가치", valueStoryCards(s, pd, age, kmLeft, yrLeft, premium)) { open = false }
    }
}


/**
 * 가치 스토리 카드 — 카드마다 "내 차 vs 같은 연식(또는 기준) → 결론"(24757).
 * 값 비교 → 컨디션 순위 → 앞으로(곡선) → 인증중고차 기준. 숫자가 다른 '기준가(정부 잔가율)' 카드는 헷갈려서 뺌.
 */
private fun valueStoryCards(
    s: VehicleSnapshot, pd: PriceData, age: Int, kmLeft: Int, yrLeft: Int, premium: Double,
): List<StoryCard> {
    val info = androidx.compose.material.icons.Icons.Filled.Info
    val diff = s.valueDiffManwon
    val sign = if (diff >= 0) "+" else "−"
    val up = if (diff >= 0) Palette.Good else Palette.Crit
    val top = Regex("상위\\s*(\\d+)%").find(s.conditionTag)?.groupValues?.get(1)?.toIntOrNull()
    val inCpo = kmLeft > 0 && yrLeft > 0
    return listOfNotNull(
        StoryCard(
            kicker = "내 ${s.model} 값 · 같은 연식과 비교",
            headline = "$sign${fmt(kotlin.math.abs(diff))}만원 ${if (diff >= 0) "높음" else "낮음"}",
            sub = "같은 연식 평균보다 $sign${"%.1f".format(kotlin.math.abs(premium) * 100)}% · 관리 상태 반영",
            icon = info, tint = up,
            visual = { e -> PriceBarsVisual(s.adjustedPriceManwon, s.standardPriceManwon, up, e) },
            versus = Versus(
                mine = "${fmt(s.adjustedPriceManwon)}만원", mineSub = "관리 상태 반영",
                otherLabel = "같은 연식 평균", other = "${fmt(s.standardPriceManwon)}만원", otherSub = "${s.model} ${s.year}년식",
            ),
            note = "데모 값 · 실제 시세 보정 전",
        ),
        top?.let {
            StoryCard(
                kicker = "내 ${s.model} 컨디션 · 같은 연식과 비교",
                headline = "100대 중 상위 ${it}대 안",
                sub = "컨디션 ${s.conditionScore}점 · ${s.conditionWord}",
                icon = info, tint = Palette.Good,
                visual = { e -> RankVisual(it, e) },
                versus = Versus(
                    mine = "상위 $it%", mineSub = "${s.conditionScore}점 · ${s.conditionWord}",
                    otherLabel = "같은 연식 보통", other = "상위 50%", otherSub = "100대 중 가운데",
                ),
                note = "데모 값",
            )
        },
        StoryCard(
            kicker = "앞으로 · 내 ${s.model} vs 같은 연식 평균",
            headline = if (premium >= 0) "관리 유지하면 평균 위" else "평균보다 아래",
            sub = "파랑 선 = 내 차 · 회색 선 = 같은 연식 평균\n출고 직후 −${((1 - pd.rate(0)) * 100).roundToInt()}%는 모든 차 공통",
            icon = info, tint = up,
            visual = { e -> CurveVisual(pd, age, premium, e) },
            note = "근거: 정부 잔가율(세금용) 곡선 · 데모 값 · 시세 보정 전",
        ),
        StoryCard(
            kicker = "내 ${s.model} · 인증중고차 기준과 비교",
            headline = if (inCpo) "${fmt(kmLeft)}km 남음" else "기준 지남",
            sub = if (inCpo) "${yrLeft}년·${fmt(kmLeft)}km 안이면 현대 인증중고차 대상" else "현대 인증중고차 대상 기준을 넘음",
            icon = info,
            visual = { e -> BarsVisual(s.odometerKm, pd.cpoKm, age, pd.cpoYears, e) },
            versus = Versus(
                mine = "${fmt(s.odometerKm)}km", mineSub = "${age}년 경과 (연식 기준)",
                otherLabel = "인증중고차 기준", other = "${fmt(pd.cpoKm)}km", otherSub = "${pd.cpoYears}년 이내",
            ),
            note = "근거: 현대 인증중고차 대상 기준 · ${pd.asOf}",
        ),
    )
}

/** 내 차 값 vs 같은 연식 평균 — 막대 두 개가 0에서 자람(바닥 0 기준, 차이를 부풀리지 않음) */
@Composable
private fun PriceBarsVisual(mine: Int, avg: Int, mineColor: androidx.compose.ui.graphics.Color, e: Float) {
    val mx = max(mine, avg).toFloat()
    Row(Modifier.fillMaxWidth(0.8f).fillMaxHeight(0.82f), horizontalArrangement = Arrangement.spacedBy(36.dp)) {
        PriceBar("내 차", "${fmt(mine)}만원", mine / mx * e, mineColor, Modifier.weight(1f))
        PriceBar("같은 연식 평균", "${fmt(avg)}만원", avg / mx * e, Palette.InkMut, Modifier.weight(1f))
    }
}

@Composable
private fun PriceBar(label: String, value: String, frac: Float, color: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    val f = frac.coerceIn(0.001f, 1f)
    Column(modifier.fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Palette.Ink, fontSize = T.Title, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Spacer(Modifier.weight(1.001f - f))
        Box(Modifier.fillMaxWidth().weight(f).background(color, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)))
        Spacer(Modifier.height(10.dp))
        Text(label, color = Palette.Ink2, fontSize = T.Label, fontWeight = FontWeight.Bold)
    }
}

/** 같은 연식 100대 — 1등이 왼쪽 위. 내 차 자리(크게, 초록)와 보통(50등, 회색) 표시 */
@Composable
private fun RankVisual(top: Int, e: Float) {
    val good = Palette.Good; val mut = Palette.InkMut; val line = Palette.Line
    Column(Modifier.fillMaxHeight(0.86f), horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(Modifier.weight(1f).aspectRatio(1f)) {
            val c = size.minDimension / 10f
            val shown = (100 * e).roundToInt()
            for (i in 0 until minOf(shown, 100)) {
                val ctr = Offset(c * (i % 10) + c / 2f, c * (i / 10) + c / 2f)
                when {
                    i == top - 1 -> { drawCircle(good.copy(alpha = 0.3f), c * 0.5f, ctr); drawCircle(good, c * 0.34f, ctr) }
                    i == 49 -> drawCircle(mut, c * 0.30f, ctr)
                    i < top -> drawCircle(good.copy(alpha = 0.4f), c * 0.22f, ctr)
                    else -> drawCircle(line, c * 0.22f, ctr)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(18.dp).background(good, RoundedCornerShape(99.dp)))
            Spacer(Modifier.width(8.dp))
            Text("내 차 ${top}등", color = Palette.Ink, fontSize = T.Label, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(28.dp))
            Box(Modifier.size(16.dp).background(mut, RoundedCornerShape(99.dp)))
            Spacer(Modifier.width(8.dp))
            Text("보통 50등", color = Palette.Ink2, fontSize = T.Label, fontWeight = FontWeight.Bold)
        }
    }
}

/** 곡선이 왼쪽부터 그려짐(e = 0→1) — 평균 + 내 차(관리 반영) */
@Composable
private fun CurveVisual(pd: PriceData, age: Int, premium: Double, e: Float) {
    // 관리 카드(premium ≠ 0)는 지금 근처를 크게 보여 줘서 평균과의 차이가 보이게
    if (premium != 0.0) ValueChart(pd, age, premium, e, Modifier.fillMaxWidth().fillMaxHeight(0.85f), big = true, maxYear = 4f, yMin = 0.45)
    else ValueChart(pd, age, premium, e, Modifier.fillMaxWidth().fillMaxHeight(0.85f), big = true)
}

/** 신차가 × 잔가율 = 기준가 — 숫자가 0에서 올라감 */
@Composable
private fun EquationVisual(newPrice: Double, rate: Double, base: Double, e: Float) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        StatBox("신차가", "${manwon(newPrice * e)}만원", modifier = Modifier.width(320.dp))
        Text("× ${"%.3f".format(rate * e)}", color = Palette.Ink2, fontSize = T.H1, fontWeight = FontWeight.Bold)
        StatBox("기준가 (시세 보정 전)", "${manwon(base * e)}만원", modifier = Modifier.width(320.dp), valueColor = Palette.Accent)
    }
}

/** 인증중고차 기준까지 — 주행거리·기간 막대가 차오름 */
@Composable
private fun BarsVisual(km: Int, kmMax: Int, age: Int, yrMax: Int, e: Float) {
    Column(Modifier.fillMaxWidth(0.9f), verticalArrangement = Arrangement.spacedBy(28.dp)) {
        Bar("주행거리", "${fmt((km * e).roundToInt())} / ${fmt(kmMax)}km", (km.toFloat() / kmMax) * e)
        Bar("기간(연식 기준)", "${age}년 / ${yrMax}년", (age.toFloat() / yrMax) * e)
    }
}

@Composable
private fun Bar(label: String, value: String, frac: Float) {
    Column {
        Row {
            Text(label, color = Palette.Ink2, fontSize = T.Label)
            Spacer(Modifier.weight(1f))
            Text(value, color = Palette.Ink, fontSize = T.Label, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(28.dp).background(Palette.Line, RoundedCornerShape(99.dp))) {
            Box(Modifier.fillMaxHeight().fillMaxWidth(frac.coerceIn(0f, 1f)).background(Palette.Accent, RoundedCornerShape(99.dp)))
        }
    }
}


/** 범례 — 파랑 내 차(관리 반영), 회색 같은 연식 평균 */
@Composable
private fun Legend() {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Box(Modifier.size(width = 22.dp, height = 6.dp).background(Palette.Accent, RoundedCornerShape(99.dp)))
        Spacer(Modifier.width(8.dp))
        Text("내 차(관리 반영 · 데모 값)", color = Palette.Ink2, fontSize = T.Caption)
        Spacer(Modifier.width(18.dp))
        Box(Modifier.size(width = 22.dp, height = 4.dp).background(Palette.InkMut, RoundedCornerShape(99.dp)))
        Spacer(Modifier.width(8.dp))
        Text("같은 연식 평균(정부 잔가율)", color = Palette.Ink2, fontSize = T.Caption)
    }
}
