package com.autograph.app.data

/**
 * 데모/에뮬레이터용 가상 데이터. 시연 영상은 이 소스로 녹화합니다.
 * 값은 확정된 웹 프로토타입(오토그래프 인카 프로토타입)과 1:1로 일치시킵니다.
 * 실차량 연동 시 PleosVehicleDataSource로 교체하세요.
 */
class MockVehicleDataSource : VehicleDataRepository {

    override fun models(): List<String> = listOf("tucson", "ioniq5", "elantra")

    override fun snapshot(key: String): VehicleSnapshot = when (key) {
        "ioniq5" -> VehicleSnapshot(
            key = "ioniq5", model = "아이오닉 5", year = 2024, odometerKm = 19_120, isEv = true,
            conditionScore = 88, conditionWord = "우수", conditionTag = "상위 10% · 동일 연식 대비",
            standardPriceManwon = 3_950, adjustedPriceManwon = 4_120,
            valueNote = "배터리 SoH가 양호해 표준 시세 대비 +170만원으로 평가됩니다.",
            parts = listOf(
                VehiclePart("구동 배터리 SoH", "96%", PartStatus.GOOD, DataSource.ESTIMATED),
                VehiclePart("전비(평균)", "5.6 km/kWh", PartStatus.GOOD, DataSource.ESTIMATED),
                VehiclePart("와이퍼", "정상", PartStatus.GOOD, DataSource.REALTIME),
                VehiclePart(
                    "타이어(뒤우)", "2.1 bar", PartStatus.WARN, DataSource.ESTIMATED, HotspotRole.TIRE_RR,
                    care = PartCare(
                        reason = "뒤 우측 타이어 저압 경고(TPMS)가 감지됐어요. 정확한 공기압 수치는 추정값입니다.",
                        timing = "이번 주 안에 공기압 보충 · 반복되면 점검",
                        spec = "권장 공기압은 운전석 도어 안쪽 라벨 기준",
                        partNo = "해당 없음 (보충/점검 항목)",
                    ),
                ),
                VehiclePart("브레이크 패드", "65%", PartStatus.GOOD, DataSource.ESTIMATED),
                VehiclePart("엔진/미션오일", "해당 없음", PartStatus.MUTED, DataSource.NONE),
            ),
            passport = listOf(
                PassportRow("차량", "아이오닉 5 · 2024"),
                PassportRow("누적 주행", "19,120 km"),
                PassportRow("배터리 SoH", "96% (추정)"),
                PassportRow("정비 이력", "정규 3회 · 순정"),
                PassportRow("사고 이력", "무사고"),
            ),
            voice = "컨디션 88점, 우수합니다. 뒤 우측 타이어 공기압만 점검하시면 좋아요.",
            carImage = "car_ioniq5.png",
        )
        "elantra" -> VehicleSnapshot(
            key = "elantra", model = "아반떼", year = 2022, odometerKm = 38_900, isEv = false,
            conditionScore = 81, conditionWord = "양호", conditionTag = "상위 29% · 동일 연식 대비",
            standardPriceManwon = 2_050, adjustedPriceManwon = 2_120,
            valueNote = "미션오일 교체 주기 영향으로 표준 시세 대비 +70만원 수준입니다.",
            parts = listOf(
                VehiclePart("와이퍼", "정상", PartStatus.GOOD, DataSource.REALTIME),
                VehiclePart("엔진오일", "정상", PartStatus.GOOD, DataSource.ESTIMATED),
                VehiclePart(
                    "미션오일", "교체 주기", PartStatus.WARN, DataSource.ESTIMATED, HotspotRole.HOOD,
                    care = PartCare(
                        reason = "누적 주행거리 기준으로 변속기 오일 교체 주기에 도달했어요.",
                        timing = "다음 정기 점검 때 함께 교체 권장 (추정)",
                        spec = "변속기 오일 규격은 차량 취급설명서 기준 (예시)",
                        partNo = "VIN 기준 조회 필요 (예시)",
                    ),
                ),
                VehiclePart("타이어(앞)", "2.2 bar", PartStatus.GOOD, DataSource.ESTIMATED),
                VehiclePart("브레이크 패드", "50%", PartStatus.GOOD, DataSource.ESTIMATED),
                VehiclePart("12V 배터리", "양호", PartStatus.GOOD, DataSource.ESTIMATED),
            ),
            passport = listOf(
                PassportRow("차량", "아반떼 · 2022"),
                PassportRow("누적 주행", "38,900 km"),
                PassportRow("미션오일", "교체 주기 도래 (추정)"),
                PassportRow("정비 이력", "정규 6회"),
                PassportRow("사고 이력", "무사고"),
            ),
            voice = "컨디션 81점입니다. 미션오일 교체 주기가 도래했으니 점검을 권장드려요.",
            carImage = "car_elantra.png",
        )
        else -> VehicleSnapshot(
            key = "tucson", model = "투싼", year = 2024, odometerKm = 23_480, isEv = false,
            conditionScore = 84, conditionWord = "양호", conditionTag = "상위 18% · 동일 연식 대비",
            standardPriceManwon = 2_680, adjustedPriceManwon = 2_790,
            valueNote = "표준 시세 대비 관리 상태가 좋아 +110만원으로 평가됩니다.",
            parts = listOf(
                VehiclePart("와이퍼", "정상", PartStatus.GOOD, DataSource.REALTIME),
                VehiclePart(
                    "엔진오일", "교체 임박", PartStatus.WARN, DataSource.ESTIMATED, HotspotRole.HOOD,
                    care = PartCare(
                        reason = "마지막 교체 이후 주행거리와 주행 패턴 기준으로 교체 주기에 가까워졌어요.",
                        timing = "약 1,000 km 이내 교체 권장 (추정)",
                        spec = "엔진오일 0W-20 (예시)",
                        partNo = "오일필터 26300-XXXXX (예시)",
                    ),
                ),
                VehiclePart("미션오일", "정상", PartStatus.GOOD, DataSource.ESTIMATED),
                VehiclePart(
                    "타이어(뒤우)", "2.1 bar", PartStatus.WARN, DataSource.ESTIMATED, HotspotRole.TIRE_RR,
                    care = PartCare(
                        reason = "뒤 우측 타이어 저압 경고(TPMS)가 감지됐어요. 정확한 공기압 수치는 추정값입니다.",
                        timing = "이번 주 안에 공기압 보충 · 반복되면 점검",
                        spec = "권장 공기압은 운전석 도어 안쪽 라벨 기준",
                        partNo = "해당 없음 (보충/점검 항목)",
                    ),
                ),
                VehiclePart("브레이크 패드", "60%", PartStatus.GOOD, DataSource.ESTIMATED),
                VehiclePart("12V 배터리", "양호", PartStatus.GOOD, DataSource.ESTIMATED),
            ),
            passport = listOf(
                PassportRow("차량", "투싼 · 2024"),
                PassportRow("누적 주행", "23,480 km"),
                PassportRow("엔진오일", "교체 임박 (추정)"),
                PassportRow("정비 이력", "정규 4회 · 순정"),
                PassportRow("사고 이력", "무사고"),
            ),
            voice = "컨디션 84점, 양호합니다. 엔진오일 교체 시점이 다가오니 점검하시면 좋아요.",
            carImage = "car_tucson.png",
        )
    }

    override fun trip(key: String): TripReport = when (key) {
        "elantra" -> TripReport(todayKm = 31.2, durationMin = 48, hardBrakes = 1, hardAccels = 2, valueImpact = "소폭 감소", weekKm = 168, eff = "12.4 km/L", effLabel = "평균 연비")
        "ioniq5" -> TripReport(todayKm = 24.6, durationMin = 38, hardBrakes = 0, hardAccels = 0, valueImpact = "거의 없음", weekKm = 121, eff = "5.6 km/kWh", effLabel = "평균 전비")
        else -> TripReport(todayKm = 28.4, durationMin = 42, hardBrakes = 0, hardAccels = 1, valueImpact = "거의 없음", weekKm = 142, eff = "13.8 km/L", effLabel = "평균 연비")
    }
}
