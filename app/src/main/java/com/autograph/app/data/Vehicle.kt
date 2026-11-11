package com.autograph.app.data

/** 부위 상태 등급 (프로토타입: good / warn / mut) */
enum class PartStatus { GOOD, WARN, CRITICAL, MUTED }

/** 데이터 출처 — 기술적 정직성 태그 (실시간 / 추정 / 해당없음) */
enum class DataSource(val label: String) {
    REALTIME("실시간"),   // Pleos Vehicle SDK로 직접 조회
    ESTIMATED("추정"),    // 정비주기·주행 기반 추정
    NONE("—");            // 해당 없음 (예: EV의 엔진/미션오일)
}

/** 3D 인포그래픽에서 문제 부위를 가리킬 위치 */
enum class HotspotRole { NONE, HOOD, TIRE_FL, TIRE_FR, TIRE_RL, TIRE_RR, BRAKE }

/** 상태 맵에 표시되는 개별 부위 */
data class VehiclePart(
    val name: String,
    val value: String,
    val status: PartStatus,
    val source: DataSource,
    val hotspot: HotspotRole = HotspotRole.NONE,
)

/** 밸류 패스포트 행 */
data class PassportRow(val label: String, val value: String)

/** 현재 차량 스냅샷 (상태 + 가치) */
data class VehicleSnapshot(
    val key: String,
    val model: String,
    val year: Int,
    val odometerKm: Int,
    val isEv: Boolean,
    val conditionScore: Int,            // 0..100
    val conditionWord: String,          // 양호 / 우수 …
    val conditionTag: String,           // "상위 18% · 동일 연식 대비"
    val standardPriceManwon: Int,       // 동일 연식 평균 중고가
    val adjustedPriceManwon: Int,       // 내 차 보정가
    val valueNote: String,
    val parts: List<VehiclePart>,
    val passport: List<PassportRow>,
    val voice: String,
    val carImage: String,               // assets/ 내 3/4 뷰 PNG 파일명
) {
    /** 핵심 지표: 동일 연식 평균 대비 차이(+면 평균보다 높음) */
    val valueDiffManwon: Int get() = adjustedPriceManwon - standardPriceManwon
}

/** 운전 종료 리포트 (운전종료 화면에서 사용) */
data class TripReport(
    val todayKm: Double,
    val durationMin: Int,
    val hardBrakes: Int,
    val hardAccels: Int,
    val valueImpact: String,
    val weekKm: Int,
    val eff: String,        // 평균 연비/전비 값
    val effLabel: String,   // "평균 연비" / "평균 전비"
)

/**
 * 데이터 소스 추상화 — Mock(데모 녹화용) 또는 Pleos(실차량)로 교체.
 * UI는 이 인터페이스만 바라보므로, 시연은 Mock으로 그대로 녹화하고
 * 실차량/에뮬레이터 연동 시 PleosVehicleDataSource로만 바꾸면 됩니다.
 */
interface VehicleDataRepository {
    /** 지원 차종 key 목록 (표시 순서) */
    fun models(): List<String>
    fun snapshot(key: String): VehicleSnapshot
    fun trip(key: String): TripReport
}
