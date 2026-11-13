package com.autograph.app.ui

import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.autograph.app.data.DataSource
import com.autograph.app.data.VehiclePart

/**
 * 케어 시트 — 문제 항목을 탭했을 때만 열리는 '해결 다음 단계'.
 * 광고가 아니라 상태의 연장선: 왜 → 언제 → 무엇을 → (예약 1차 / 정품 부품 2차).
 * 메인 화면에는 구매 버튼을 두지 않는다. 버튼은 모두 76dp 이상.
 */
@Composable
fun PartCareDialog(part: VehiclePart, model: String, info: com.autograph.app.data.CareInfo? = null, asOf: String = "", onDismiss: () -> Unit) {
    val care = part.care ?: return
    var notice by remember { mutableStateOf<String?>(null) }
    var open by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        val shape = RoundedCornerShape(24.dp)
        Column(
            Modifier
                .widthIn(max = 920.dp)
                .padding(horizontal = 40.dp)
                .shadow(24.dp, shape)                         // 뒤 화면과 구분(24752)
                .border(2.dp, Palette.ModalEdge, shape)
                .background(Palette.Panel, shape)
                .padding(horizontal = 32.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(part.name, color = Palette.Ink, fontSize = T.Title, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(14.dp))
                        Text(part.value, color = part.status.color(), fontSize = T.Body, fontWeight = FontWeight.SemiBold)
                    }
                    Text("내 $model · ${part.source.label} 데이터 기준", color = Palette.InkMut, fontSize = T.Caption)
                }
                Btn("닫기", BtnStyle.GHOST, fontSize = T.Label, leading = Icons.Filled.Close, onClick = onDismiss)
            }

            // 기본 화면 — 언제 할지, 얼마인지만
            InfoLine("권장 시점", care.timing)
            val cost = when {
                info != null && info.parts.isNotEmpty() -> "${fmt(info.partsSum)}원~"
                info != null -> "확인 필요"
                else -> null
            }
            if (cost != null) InfoLine("부품값", cost)

            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 1차: 예약(케어)
                Btn("가까운 블루핸즈 예약", BtnStyle.TONAL, Modifier.weight(1f), leading = Icons.Filled.Build) {
                    notice = "데모: 블루핸즈 예약 화면으로 연결됩니다."
                }
                Spacer(Modifier.width(16.dp))
                // 2차: 정품 부품
                Btn("정품 부품 보기", BtnStyle.GHOST, fontSize = T.Label, trailing = IconNext) {
                    notice = "데모: 모비스 정품 부품 페이지로 연결됩니다."
                }
            }
            notice?.let { Text(it, color = Palette.Good, fontSize = T.Label) }

            // 상세 — 스토리 카드(드래그로 넘김)
            Btn("상세 보기", BtnStyle.GHOST, fontSize = T.Label, trailing = IconNext) { open = true }
        }
    }
    if (open) StoryViewer("내 $model · ${part.name}", careStoryCards(part, care, info, asOf, model)) { open = false }
}

/** 글 속 첫 km 숫자("약 1,000 km 이내" → 1000, "10,000km · 12개월" → 10000) */
private fun kmIn(s: String?): Int? =
    s?.let { Regex("([\\d,]+)\\s*km").find(it)?.groupValues?.get(1)?.replace(",", "")?.toIntOrNull() }

/** 글 속 kPa → bar("240 kPa (35 psi)" → 2.4) */
private fun kpaToBar(s: String?): Double? =
    s?.let { Regex("(\\d+)\\s*kPa").find(it)?.groupValues?.get(1)?.toDoubleOrNull()?.div(100.0) }

private fun bar1(v: Double) = "%.1f".format(v)

/**
 * 케어 시트 스토리 카드 — 카드마다 "내 차 vs 비교 대상 → 결론"(24757).
 * 1장: 내 차 상태 vs 차종 권장값, 2장: 그래서 내 차에 할 일·필요한 것. 출처는 카드 맨 아래 한 줄.
 */
private fun careStoryCards(
    part: VehiclePart, care: com.autograph.app.data.PartCare, info: com.autograph.app.data.CareInfo?, asOf: String, model: String,
): List<StoryCard> {
    val tire = part.name.startsWith("타이어")
    val warn = Palette.Warn
    val note = if (info == null) "" else listOf(
        if (info.source.isEmpty()) "공식 자료 못 찾음" else "근거: ${info.sourceShort.ifEmpty { info.source }}",
        if (asOf.isNotEmpty()) "$asOf 조회" else "",
    ).filter { it.isNotEmpty() }.joinToString(" · ")

    if (tire) {
        val mine = Regex("([\\d.]+)").find(part.value)?.groupValues?.get(1)?.toDoubleOrNull()
        val rec = kpaToBar(info?.specMain)
        val gap = if (mine != null && rec != null) rec - mine else null
        val first = StoryCard(
            kicker = "내 $model · ${part.name} 공기압",
            headline = when {
                gap == null -> part.value
                gap > 0.05 -> "${bar1(gap)} bar 부족"
                gap < -0.05 -> "${bar1(-gap)} bar 높음"
                else -> "권장과 같음"
            },
            sub = care.reason,
            icon = Icons.Filled.Warning, art = "tire_gauge.webp", tint = warn,
            versus = Versus(
                mine = part.value, mineSub = if (part.source == DataSource.REALTIME) "실시간 측정" else "${part.source.label}값",
                otherLabel = "$model 권장", other = rec?.let { "${bar1(it)} bar" } ?: "확인 필요",
                otherSub = info?.specMain ?: "",
                frac = if (mine != null && rec != null) (mine / rec).toFloat() else null,
                fracLabel = if (mine != null && rec != null) "권장 공기압의 ${Math.round(mine / rec * 100)}%" else "",
            ),
            note = note,
        )
        val todo = care.timing.split(" · ")
        val second = StoryCard(
            kicker = "그래서 할 일 · 내 $model",
            headline = todo[0],
            sub = listOfNotNull(
                if (todo.size > 1) todo.drop(1).joinToString(" · ") else null,
                info?.let { "넣을 공기압 ${it.specMain}" },
                info?.specSub?.ifEmpty { null },
            ).joinToString("\n"),
            icon = Icons.Filled.Build, art = "tire.webp", tint = warn, note = note,
        )
        return listOf(first, second)
    }

    // 엔진오일·미션오일: 지난 교체 후 주행(추정) vs 차종 교체 주기
    val interval = kmIn(info?.intervalNormal)
    val left = kmIn(care.timing)
    val used = if (interval != null && left != null && left < interval) interval - left else null
    val n = info?.intervalNormal?.split(" · ") ?: emptyList()
    val severe = info?.intervalSevere?.split(" · ")?.firstOrNull()?.ifEmpty { null }
    val first = StoryCard(
        kicker = "내 $model · ${part.name} 교체 시점",
        headline = left?.let { "약 ${fmt(it)}km 남음" } ?: part.value,
        sub = listOfNotNull(
            if (left != null) "곧 교체하세요" else care.timing,
            severe?.let { "가혹 조건이면 ${it}마다" },
        ).joinToString(" · "),
        icon = Icons.Filled.Warning,
        art = if (part.name == "엔진오일") "engine_oil.webp" else "transmission.webp", tint = warn,
        versus = Versus(
            mine = used?.let { "${fmt(it)}km" } ?: part.value,
            mineSub = if (used != null) "지난 교체 후 주행 (${part.source.label})" else "${part.source.label} 기준",
            otherLabel = "$model 권장",
            other = interval?.let { "${fmt(it)}km" } ?: "확인 필요",
            otherSub = if (interval != null) (if (n.size > 1) "또는 ${n.drop(1).joinToString(" · ")}마다" else "마다") else "공식 교체 주기 자료 없음",
            frac = if (used != null && interval != null) used.toFloat() / interval else null,
            fracLabel = if (used != null && interval != null) "교체 주기의 ${used * 100 / interval}% 지남" else "",
        ),
        note = note,
    )
    if (info == null) return listOf(first, StoryCard("그래서 할 일 · 내 $model", care.timing, "${care.spec}\n${care.partNo}", Icons.Filled.Build))
    val second = StoryCard(
        kicker = "교체할 때 필요한 것 · 내 $model 규격",
        headline = info.specMain.ifEmpty { info.spec },
        sub = (listOfNotNull(info.specSub.ifEmpty { null }) +
            info.parts.map { "${it.name} ${it.ptno} · ${fmt(it.price)}원" } +
            listOfNotNull(info.missing.ifEmpty { null })).joinToString("\n"),
        icon = Icons.Filled.ShoppingCart,
        art = when {
            info.parts.isNotEmpty() -> "oil_filter.webp"
            part.name == "엔진오일" -> "oil_spec.webp"
            else -> "transmission.webp"
        }, tint = warn,
        note = listOf(note, "품번은 차대번호(VIN)로 최종 확인").filter { it.isNotEmpty() }.joinToString(" · "),
    )
    return listOf(first, second)
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, color = Palette.Ink2, fontSize = T.Label, modifier = Modifier.width(180.dp))
        Text(value, color = Palette.Ink, fontSize = T.Body, modifier = Modifier.weight(1f))
    }
}
