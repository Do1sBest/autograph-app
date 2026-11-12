package com.autograph.app.ui

import android.graphics.BitmapFactory
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * 내 차 vs 비교 대상 — 카드가 '무엇과 비교해서 무슨 말을 하는지' 한눈에 보이게(11-13 24757).
 * frac = 내 차 값 ÷ 비교 값(막대로 표시, null 이면 막대 없음).
 */
data class Versus(
    val mine: String,
    val mineSub: String = "",
    val otherLabel: String,
    val other: String,
    val otherSub: String = "",
    val frac: Float? = null,
    val fracLabel: String = "",
)

/**
 * 스토리 카드 한 장 — 무엇에 대한 카드(kicker) → 내 차 vs 비교 대상 → 결론(headline) → 근거 한 줄(note).
 * art = assets 안 그림 파일(없으면 아이콘 자리표시). visual = 직접 그리는 움직이는 그림(곡선·막대 등).
 */
data class StoryCard(
    val kicker: String,
    val headline: String,
    val sub: String,
    val icon: ImageVector,
    val art: String? = null,
    val tint: Color? = null,
    val visual: (@Composable (enter: Float) -> Unit)? = null,
    val frames: String? = null,          // assets/story/<frames>/f_00.webp … — 드래그하는 만큼 움직임이 따라감(힉스필드 영상 프레임)
    val versus: Versus? = null,
    val note: String = "",               // 맨 아래 작은 글씨: 근거·출처
)

/**
 * 스토리 보기 — 앱 칸 전체. 위 진행 막대, 좌우 드래그 또는 양옆 탭으로 넘김, 닫기.
 * 주차 중에만 열리는 화면(케어 시트·밸류 패스포트)에서만 쓴다.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StoryViewer(title: String, cards: List<StoryCard>, onClose: () -> Unit) {
    val pager = rememberPagerState(pageCount = { cards.size })
    val scope = rememberCoroutineScope()
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        // 창이 화면 전체를 덮으면 '화면이 바뀐 것/오류'처럼 보임(24752) → 둘레를 비워 어두워진 뒤 화면이 보이게 하고 테두리·그림자
        val shape = RoundedCornerShape(28.dp)
        Box(Modifier.fillMaxSize().padding(28.dp)) {
          Box(
            Modifier.fillMaxSize()
                .shadow(28.dp, shape)
                .border(2.dp, Palette.ModalEdge, shape)
                .clip(shape)
                .background(Brush.verticalGradient(listOf(Palette.Bg, Palette.Panel)))
          ) {
            Column(Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 18.dp)) {
                // 진행 막대 + 제목 + 닫기
                StoryProgress(cards.size, pager)
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, color = Palette.Ink, fontSize = T.Body, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(14.dp))
                    Text("${pager.currentPage + 1} / ${cards.size}", color = Palette.InkMut, fontSize = T.Label)
                    Spacer(Modifier.weight(1f))
                    Btn("닫기", BtnStyle.GHOST, fontSize = T.Label, leading = Icons.Filled.Close, onClick = onClose)
                }
                Spacer(Modifier.height(10.dp))
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    HorizontalPager(state = pager, modifier = Modifier.fillMaxSize(), pageSpacing = 24.dp) { page ->
                        val off = ((pager.currentPage - page) + pager.currentPageOffsetFraction).absoluteValue.coerceIn(0f, 1f)
                        StoryPage(
                            cards[page], active = pager.currentPage == page, reveal = 1f - off,
                            modifier = Modifier.pointerInput(page) {
                                // 인스타식 탭: 왼쪽 1/4 이전, 오른쪽 1/4 다음 (드래그는 페이저가 받음)
                                detectTapGestures { o ->
                                    val w = size.width
                                    scope.launch {
                                        if (o.x < w * 0.25f && pager.currentPage > 0) pager.animateScrollToPage(pager.currentPage - 1)
                                        else if (o.x > w * 0.75f) {
                                            if (pager.currentPage < cards.lastIndex) pager.animateScrollToPage(pager.currentPage + 1) else onClose()
                                        }
                                    }
                                }
                            }.graphicsLayer {
                                // 넘길 때 살짝 작아지고 흐려짐 → 카드가 겹쳐 넘어가는 느낌
                                val s = 1f - 0.08f * off
                                scaleX = s; scaleY = s; alpha = 1f - 0.5f * off
                            }
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Btn("이전", BtnStyle.GHOST, fontSize = T.Label, leading = Icons.AutoMirrored.Filled.KeyboardArrowLeft) {
                        scope.launch { if (pager.currentPage > 0) pager.animateScrollToPage(pager.currentPage - 1) }
                    }
                    Spacer(Modifier.weight(1f))
                    Text("← 드래그해서 넘기기 →", color = Palette.InkMut, fontSize = T.Caption)
                    Spacer(Modifier.weight(1f))
                    if (pager.currentPage < cards.lastIndex)
                        Btn("다음", BtnStyle.TONAL, fontSize = T.Label, trailing = IconNext) {
                            scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
                        }
                    else Btn("완료", BtnStyle.PRIMARY, fontSize = T.Label, onClick = onClose)
                }
            }
          }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StoryProgress(n: Int, pager: PagerState) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(n) { i ->
            val fill = when {
                i < pager.currentPage -> 1f
                i == pager.currentPage -> 1f - pager.currentPageOffsetFraction.coerceIn(0f, 1f)
                i == pager.currentPage + 1 -> (pager.currentPageOffsetFraction).coerceIn(0f, 1f)
                else -> 0f
            }
            Box(Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(99.dp)).background(Palette.Line)) {
                Box(Modifier.fillMaxHeight().fillMaxWidth(fill).background(Palette.Accent))
            }
        }
    }
}

@Composable
private fun StoryPage(card: StoryCard, active: Boolean, reveal: Float, modifier: Modifier) {
    // 들어올 때 움직임: 그림 확대 + 글씨 위로 올라오며 나타남
    val enter = remember { Animatable(0f) }
    LaunchedEffect(active) {
        if (active) { enter.snapTo(0f); enter.animateTo(1f, tween(650, easing = FastOutSlowInEasing)) }
    }
    val e = enter.value
    val tint = card.tint ?: Palette.Accent
    Row(
        modifier.fillMaxSize()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(tint.copy(alpha = if (Palette.night) 0.28f else 0.16f), Palette.Panel2)))
            .padding(36.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 그림
        Box(Modifier.weight(0.85f).fillMaxHeight(), contentAlignment = Alignment.Center) {
            when {
                card.visual != null -> card.visual.invoke(e)
                else -> StoryArt(card, tint, reveal, Modifier.scale(0.88f + 0.12f * e).alpha(0.3f + 0.7f * e))
            }
        }
        Spacer(Modifier.width(32.dp))
        // 글씨: 무엇에 대한 카드 → 내 차 vs 비교 → 결론 → 근거
        Column(
            Modifier.weight(1.15f).graphicsLayer { translationY = (1f - e) * 60f; alpha = e },
            verticalArrangement = Arrangement.Center
        ) {
            Text(card.kicker, color = Palette.Ink, fontSize = T.Body, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            card.versus?.let { VersusRow(it, tint, e); Spacer(Modifier.height(22.dp)) }
            Text(card.headline, color = if (card.versus != null) tint else Palette.Ink, fontSize = if (card.versus != null) T.H1 else T.Hero,
                fontWeight = FontWeight.Bold, lineHeight = (if (card.versus != null) T.H1 else T.Hero) * 1.12f)
            if (card.sub.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(card.sub, color = Palette.Ink2, fontSize = T.Body, lineHeight = T.Body * 1.35f)
            }
            if (card.note.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(card.note, color = Palette.InkMut, fontSize = T.Caption, lineHeight = T.Caption * 1.3f)
            }
        }
    }
}

/** 내 차(색 테두리·색 바탕) vs 비교 대상(회색) — 두 칸 높이 같게, 아래 막대(내 차 ÷ 비교 값) */
@Composable
private fun VersusRow(v: Versus, tint: Color, e: Float) {
    Column {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
            VsBox("내 차", v.mine, v.mineSub, tint, mine = true, Modifier.weight(1f).fillMaxHeight())
            Text("vs", color = Palette.InkMut, fontSize = T.Title, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
            VsBox(v.otherLabel, v.other, v.otherSub, Palette.Ink2, mine = false, Modifier.weight(1f).fillMaxHeight())
        }
        v.frac?.let { f ->
            Spacer(Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth().height(18.dp).clip(RoundedCornerShape(99.dp)).background(Palette.Line)) {
                Box(Modifier.fillMaxHeight().fillMaxWidth((f * e).coerceIn(0f, 1f)).clip(RoundedCornerShape(99.dp)).background(tint))
            }
            if (v.fracLabel.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(v.fracLabel, color = Palette.Ink2, fontSize = T.Label, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun VsBox(label: String, value: String, sub: String, color: Color, mine: Boolean, modifier: Modifier) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier
            .border(if (mine) 3.dp else 2.dp, if (mine) color else Palette.ModalEdge, shape)
            .background(if (mine) color.copy(alpha = if (Palette.night) 0.18f else 0.08f) else Palette.Panel, shape)
            .padding(horizontal = 22.dp, vertical = 16.dp)
    ) {
        Text(label, color = if (mine) color else Palette.Ink2, fontSize = T.Label, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(value, color = if (mine) Palette.Ink else Palette.Ink2, fontSize = T.H1, fontWeight = FontWeight.Bold, maxLines = 1)
        if (sub.isNotEmpty()) Text(sub, color = Palette.InkMut, fontSize = T.Caption, lineHeight = T.Caption * 1.3f, maxLines = 2)
    }
}

/** 그림 자리 — assets/story/<art> 가 있으면 그림, 없으면 아이콘 원(임시) */
@Composable
private fun StoryArt(card: StoryCard, tint: Color, reveal: Float, modifier: Modifier) {
    val ctx = LocalContext.current
    // 움직임 프레임이 있으면 드래그 정도(reveal 0→1)에 맞춰 프레임을 고름, 없으면 정지 그림
    val frameNames = remember(card.frames) {
        card.frames?.let { f -> runCatching { ctx.assets.list("story/$f")?.filter { it.endsWith(".webp") }?.sorted() }.getOrNull() } ?: emptyList()
    }
    val idx = if (frameNames.isEmpty()) -1 else (reveal.coerceIn(0f, 1f) * frameNames.lastIndex).roundToInt()
    val bmp = remember(card.art, idx) {
        fun load(pth: String) = runCatching { ctx.assets.open(pth).use { BitmapFactory.decodeStream(it) }.asImageBitmap() }.getOrNull()
        when {
            idx >= 0 -> load("story/${card.frames}/${frameNames[idx]}")
            card.art != null -> load("story/${card.art}")   // 투명 배경 그림 — 낮·밤 같은 한 장
            else -> null
        }
    }
    if (bmp != null) {
        // 정사각 그림에 맞춰 둥근 모서리(Fit 으로 그리면 모서리가 안 잘리던 문제)
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(bmp, null, Modifier.fillMaxSize(0.92f), contentScale = ContentScale.Fit)
        }
    } else {
        Box(
            modifier.size(380.dp).clip(CircleShape)
                .background(Brush.radialGradient(listOf(tint.copy(alpha = 0.35f), tint.copy(alpha = 0.06f)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(card.icon, null, tint = tint, modifier = Modifier.size(180.dp))
        }
    }
}
