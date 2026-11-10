package com.autograph.app.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext

/**
 * 차량 3/4 뷰 렌더 이미지(assets 폴더의 PNG)를 표시한다.
 *
 * 배경: 에뮬레이터에서 Filament 네이티브 3D가 SIGSEGV로 크래시해, 데모 안정성을 위해
 * GLB를 미리 3/4 뷰로 렌더한 투명 PNG로 대체했다. (실제 헤드유닛용 Filament 뷰는
 * docs/filament-reference/Car3DView.kt.txt 로 보존)
 *
 * 이미지는 1500x1000(비율 1.5)로 정규화되어, 호출부가 같은 비율의 박스에 넣으면
 * 하이라이트 핀 좌표(BiasAlignment)가 그대로 일치한다.
 */
@Composable
fun CarImageView(assetName: String, modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    val bitmap = remember(assetName) {
        runCatching {
            ctx.assets.open(assetName).use { BitmapFactory.decodeStream(it) }.asImageBitmap()
        }.getOrNull()
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = "차량 3/4 뷰",
            modifier = modifier,
            contentScale = ContentScale.Fit,
        )
    } else {
        Box(modifier) // 이미지 로드 실패 시 빈 영역(크래시 방지)
    }
}

/** 차량 렌더 이미지의 가로:세로 비율 (정규화 캔버스 1500x1000). */
const val CAR_ASPECT = 1.5f

/** 차량 이미지를 가용 영역 대비 얼마나 크게 그릴지(0~1). 작을수록 차가 작아짐. */
const val CAR_SCALE = 0.8f
