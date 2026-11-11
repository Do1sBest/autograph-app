package com.autograph.app.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext

/** 턴테이블 프레임 수 / 기본(정면 3/4) 프레임 인덱스 (az≈30°). */
const val TURNTABLE_N = 24
const val TURNTABLE_DEFAULT = 2

/**
 * 프리렌더 360° 턴테이블. 좌우 드래그로 프레임을 넘겨 '회전'처럼 보여준다.
 * Filament 없이 동작(이미지 시퀀스) — 에뮬레이터에서 안정적.
 *
 * - 프레임 에셋: car_<key>_00.webp .. car_<key>_23.webp (동일 스케일로 정규화되어 제자리 회전)
 * - 메모리 절약을 위해 현재 프레임만 디코드.
 */
@Composable
fun CarTurntable(
    carKey: String,
    index: Int,
    onIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val ctx = LocalContext.current
    val bitmap = remember(carKey, index) {
        val name = "car_${carKey}_${index.toString().padStart(2, '0')}.webp"
        runCatching { ctx.assets.open(name).use { BitmapFactory.decodeStream(it) }.asImageBitmap() }.getOrNull()
    }
    val idx = rememberUpdatedState(index)
    var acc by remember { mutableFloatStateOf(0f) }
    val pxPerFrame = 16f

    Box(
        modifier.pointerInput(carKey) {
            detectHorizontalDragGestures { change, dragAmount ->
                change.consume()
                acc += dragAmount
                var steps = 0
                while (acc >= pxPerFrame) { acc -= pxPerFrame; steps-- }   // 오른쪽 드래그 → 한 방향
                while (acc <= -pxPerFrame) { acc += pxPerFrame; steps++ }
                if (steps != 0) {
                    val next = ((idx.value + steps) % TURNTABLE_N + TURNTABLE_N) % TURNTABLE_N
                    onIndexChange(next)
                }
            }
        }
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "차량 회전 뷰",
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Fit,
            )
        }
    }
}
