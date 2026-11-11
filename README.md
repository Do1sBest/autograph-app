# 오토그래프 (AUTOGRAPH)

현대자동차그룹 **차량용 앱 개발 공모전** 출품작.
차량의 **상태(컨디션)** 와 **자산가치**를 인포그래픽으로 보여주는 Android Automotive(AAOS) 앱입니다.

> "내 차가 지금 어떤 상태이고, 동일 연식 평균 대비 얼마나 가치 있는가"를 운전 흐름 속에서 한눈에.

---

## 핵심 아이디어
- 차량 상태를 **3/4 뷰 인포그래픽**으로 보여주고, 문제 부위는 **하이라이트 핀**으로 표시
- **동일 연식 평균 중고가 대비 내 차 가치 차이**를 핵심 지표로 제시 (+/−만원)
- 주행 데이터로 상태를 표시/추정하고, **밸류 패스포트**로 매각 가치 근거까지 연결

## 화면 구성 — 기어 연동
기어(P/R/N/D)에 따라 화면이 **자동 전환**됩니다. (실제 기어 미연동 시 하단 셀렉터로 전환)

| 상태 | 기어 | 내용 |
|---|---|---|
| **주차 중** | P | 자산가치 밴드(평균 대비 차이) · 차량 3/4 상태맵 · 하이라이트 핀 · 컨디션 점수 |
| **주행 중** | D·R·N | 속도·브레이크·스티어링 **라이브 트레이스** · 상태 스트립 |
| **운전 종료** | 주행 후 P | 주행 리포트(거리·시간·급가속/제동·가치 영향) · 경로 맵 |
| **밸류 패스포트** | (주차 중 '자세히') | 정비/사고 이력 · 가치 산정 근거 |

## 기술 스택
- **Android Automotive OS** / **Jetpack Compose** / Kotlin, minSdk 28, compileSdk 34
- **차량 3D**: GLB를 3/4 뷰로 렌더한 투명 PNG 사용
  - 에뮬레이터에서 Filament 네이티브 렌더가 크래시해 이미지 방식으로 대체
  - 실기기 헤드유닛용 Filament 구현은 `docs/filament-reference/` 에 보존
- **기어 연동**: `android.car` CarPropertyManager(`GEAR_SELECTION`)
  - 권한(`CAR_POWERTRAIN`) 미승인 시 화면 기어 셀렉터로 자동 폴백
- **데이터**: `MockVehicleDataSource`(데모) ↔ `PleosVehicleDataSource`(실차량, Pleos Vehicle SDK)

## 실행 방법
1. **Android Studio**로 `autograph-app` 폴더 열기 → Gradle Sync
2. **Automotive 에뮬레이터** 생성
   - Device Manager → Create Device → **Automotive**
   - 시스템 이미지: **Automotive with Google APIs** (API 34, x86_64)
   - Graphics: **Hardware** 권장
3. ▶ **Run**
4. 하단 **기어(P/R/N/D)** 또는 실제 기어 변경으로 화면 전환 확인

> 데모는 `MockVehicleDataSource`(가상 데이터)라 Pleos SDK 없이 바로 실행됩니다.

## 실차량 데이터 연동
`data/PleosVehicleDataSource.kt`의 TODO를 Pleos Vehicle SDK 도메인 호출로 교체:
`Odometer · Tire · Brake · EvBattery · Fuel · Wiper · Steeringwheel · DrivingMode`.
엔진/미션오일은 Pleos·AAOS 표준 속성이 없어 **정비주기·주행 기반 추정**으로 처리.

## 프로젝트 구조
```
app/src/main/
  assets/car_*.png                 # 차량 3/4 렌더 이미지
  java/com/autograph/app/
    MainActivity.kt
    data/  Vehicle · Mock… · Pleos… · GearSource · ValueEstimator
    ui/    AutographApp(셸·기어전환) · ParkedScreen · DrivingScreen
           EndScreen · PlaceholderScreens(패스포트) · Car3DView(이미지)
           Components · VehicleViewModel · Theme
docs/filament-reference/           # 실기기용 Filament 3D 코드(참고)
```

## 로드맵
- 마이현대/블루핸즈 **정비이력 연동**(차주 동의 기반)
- Pleos 실시간 도메인 연동 · 경로 맵 실데이터
- 차량 3D 인터랙션(회전 등) 고도화

---
*오토그래프 — 차량의 상태와 가치를 운전자의 일상 화면으로.*
