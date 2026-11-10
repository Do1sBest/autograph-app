package com.autograph.app.data

/**
 * 잔존가치 보정 로직 (초기 룰 기반 — 추후 서버/ML 모델로 대체).
 * 표준 시세에 컨디션 점수와 배터리 SoH를 반영해 '내 차 보정가'를 산출합니다.
 */
object ValueEstimator {
    fun adjustedPrice(standardManwon: Int, conditionScore: Int, batterySoH: Int): Int {
        val conditionFactor = (conditionScore - 80) * 0.004  // 80점 기준 가감
        val sohFactor = (batterySoH - 90) * 0.003            // SoH 90% 기준 가감
        val multiplier = 1.0 + conditionFactor + sohFactor
        return (standardManwon * multiplier).toInt()
    }
}
