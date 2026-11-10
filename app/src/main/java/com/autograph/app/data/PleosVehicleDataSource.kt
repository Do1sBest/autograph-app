package com.autograph.app.data

/**
 * === Pleos Connect Vehicle SDK 연동 지점 ===
 *
 * Pleos Playground에서 Vehicle SDK를 추가한 뒤, 각 도메인 객체를 받아
 * `checkXXXCapabilities()`로 지원 여부를 확인하고, `getXxx()` / `registerXxxCallback()`으로
 * 값을 조회해 VehicleSnapshot을 구성합니다. (정확한 패키지·메서드명은 Playground 문서 기준)
 *
 * 도메인 매핑(확정):
 *  - Odometer       → 누적 주행거리            [실시간]
 *  - Tire           → TPMS 공기압(바퀴별)       [실시간]
 *  - Brake          → 브레이크 패드 마모%        [실시간, 지원 차량 한정 → 100-마모%]
 *  - EvBattery      → 구동 배터리 SoH/전비       [EV, 실시간/추정]
 *  - Fuel           → 연료 잔량/연비            [ICE, 실시간]
 *  - Wiper          → 와이퍼 상태              [실시간]
 *  - Steeringwheel  → 조향각(주행중 트레이스)     [실시간]
 *  - DrivingMode    → 주행 모드(ECO/SPORT 등)   [실시간]
 *  - 엔진오일/미션오일 → Pleos 도메인·AAOS 표준 없음 → 정비주기·주행 기반 [추정]
 */
class PleosVehicleDataSource(
    // TODO(Pleos): Vehicle SDK 진입점(VehicleManager 등)과 각 도메인 객체를 주입하세요.
    // private val vehicle: VehicleManager,
    // 외부 시세 API (동일 연식 평균 중고가)
    // private val valuation: ValuationApi,
) : VehicleDataRepository {

    override fun models(): List<String> = listOf("tucson", "ioniq5", "elantra")

    override fun snapshot(key: String): VehicleSnapshot {
        // TODO(Pleos): 아래는 연동 스켈레톤. 각 도메인은 checkXXXCapabilities 후 조회.
        //
        //  val odo    = odometer.getOdometer()                         // 실시간
        //  val tires  = if (tire.checkTireCapabilities()) tire.getTirePressures() else null
        //  val brake  = if (brake.checkBrakeCapabilities())            // BRAKE_PAD_WEAR_PERCENTAGE
        //                   100 - brake.getBrakePadWear(Wheel.REAR_RIGHT) else null  // 남은 수명%
        //  val wiper  = wiper.getCurrentWiperState()                   // 실시간
        //  val soh    = if (isEv) evBattery.getStateOfHealth() else null
        //  val fuel   = if (!isEv) fuel.getFuelLevel() else null
        //  val oil    = OilEstimator.estimate(lastServiceKm, odo, drivingSeverity) // 추정
        //  val std    = valuation.averagePrice(model, year, odo)       // 외부 시세
        //  return VehicleSnapshot( ... adjustedPriceManwon =
        //           ValueEstimator.adjustedPrice(std, condition, soh ?: 100), ... )
        throw NotImplementedError("Pleos Vehicle SDK 연동이 필요합니다. 셋업가이드.md의 '실데이터 연동' 참고.")
    }

    override fun trip(key: String): TripReport {
        // TODO(Pleos): 주행 세션 동안 Odometer 구간거리 + Brake/Steeringwheel 이벤트(급제동/급가속) 집계.
        throw NotImplementedError("Pleos Vehicle SDK 연동이 필요합니다.")
    }
}
