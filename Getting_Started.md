# 오토그래프(AUTOGRAPH) — Pleos Connect 앱 셋업 가이드

> 확정된 웹 프로토타입(오토그래프 인카 프로토타입)을 **Pleos Connect 네이티브 앱**으로 구현했습니다.
> **화면: 주차 중 · 주행 중 · 운전 종료 · 밸류 패스포트 구현(데모 데이터 기준).**

## 1. 요구 환경
- Android Studio (AGP 8.10.x 지원 버전 — Meerkat 이상), JDK 17
- Android SDK Platform 34 · **AGP 8.10.1 / Kotlin 2.0.20 / Gradle 8.11.1** (프로젝트에 고정)
- **Pleos Connect Emulator v2.1.0** (Form Factor: Automotive · API 34 · Cold boot · RAM 4GB)
- GPU 하드웨어 가속 ON 권장

> **플랫폼 참고:** 본 앱은 **Android Automotive OS(AAOS) 기반 Pleos Connect** 플랫폼에서 동작합니다.
> 표준 Automotive 에뮬레이터가 아니라 **Pleos Connect 전용 에뮬레이터(v2.1.0)** 로 실행하세요.
> 설치: SDK Manager → **SDK Update Sites**에 Pleos 시스템 이미지 URL 추가 → **API 34** Pleos 이미지 설치 → **Automotive AVD** 생성(Cold boot · RAM 4GB).

## 2. 열기 & 실행
1. `autograph-app` 폴더를 Android Studio로 **Open**
2. Gradle Sync
3. **Pleos Connect v2.1.0 에뮬레이터** 실행 (반드시 **Cold boot** — Quick boot는 VHAL 사용 불가)
4. 실행 대상으로 해당 에뮬레이터 선택 후 **Run ▶**
5. Pleos Connect 런처에서 **"오토그래프"** 실행
6. 하단 **기어(P/R/N/D)** 또는 실제 기어 변경으로 화면 전환 확인

> gradlew 래퍼 없이 **Android Studio 내장 Gradle**로 빌드/실행합니다.

## 3. 데이터 소스
- 기본값은 **MockVehicleDataSource** — 시연 영상은 이 소스 그대로 녹화합니다.
- 값은 웹 프로토타입과 1:1 일치(투싼/아이오닉5/아반떼, 상태·가치·패스포트).
- 출처 태그(**실시간/추정**)도 동일하게 표기 → 5번 항목 참고.

## 4. 3D 차량(Filament → 이미지 폴백)
- GLB를 3/4 뷰로 렌더한 투명 PNG + **24프레임 턴테이블**(`assets/car_*.webp`)을 사용합니다.
  에뮬레이터에서 Filament 네이티브 렌더가 크래시해 이미지 방식으로 대체했습니다(Filament 의존성 비활성화).
- 하이라이트 핀 좌표는 `ParkedScreen.kt`의 `HotspotPin` 정렬에서 차종별로 미세조정합니다.

## 5. 실데이터(Pleos Connect) 연동 — 실시간 vs 추정
각 상태 항목에는 기술적 정직성을 위해 **출처 태그**를 붙입니다. 데이터는 두 종류이며 반드시 구분해 표기합니다.

### 실시간(Real-time) — 실제 신호 수신 (Virtual Console로 검증)
| 항목 | 소스 |
|---|---|
| 기어 P/R/N/D | `android.car` `GEAR_SELECTION` (AAOS Car API) |
| 와이퍼 작동 상태 | Pleos Vehicle SDK `Wiper` |
| EV 배터리 SoC·주행가능거리 | Pleos Vehicle SDK `EvBattery` |
| 타이어 저압 경고(TPMS) | Pleos Vehicle SDK `Safety` 텔테일 |
| 주행거리 · 운전 부드러움 | 속도(`PERF_VEHICLE_SPEED` / Virtual Console) 기반 계산 |

> 기어는 Pleos Vehicle SDK에 전용 도메인이 없어 **AAOS 표준 `GEAR_SELECTION`** 으로 읽습니다.

### 추정(Estimated) — 실제 신호가 없어 알고리즘으로 계산 (반드시 '추정' 표기)
| 항목 | 사유 |
|---|---|
| 타이어 공기압 수치 | `Tire` 도메인은 저압 임계치(`getLowTirePressure`)만 제공, 현재 공기압 getter·콜백 없음 → 수치는 추정 |
| 브레이크 패드 마모 | Pleos Vehicle SDK·Virtual Console에 해당 신호 없음 → 추정 |
| 엔진오일 · 미션(변속기)오일 | 전용 도메인 없음 → 정비주기·주행거리 기반 계산 |
| 12V 배터리 · 전비(평균) · 배터리 SoH | 실측 신호 없음 → 추정 |

> '실시간'과 '추정'을 명확히 구분하는 것이 심사의 **기술 정직성** 포인트입니다.

### 실데이터 연동 절차
1. `settings.gradle.kts`에 Pleos maven 저장소 추가
   `https://nexus-playground.pleos.ai/repository/maven-releases/`
2. `app/build.gradle.kts`에 의존성 추가
   `implementation("ai.pleos.playground:Vehicle:2.3.2")`
3. `data/PleosVehicleDataSource.kt`의 TODO를 실제 도메인 호출로 교체
   - 각 도메인의 `checkXxxCapabilities`로 지원 여부 확인 후 조회(**모델명 분기 금지**)
   - 사용 패턴: `Vehicle(context).initialize()` → `vehicle.getXxx()` → Get/Callback → `release()`
4. `MainActivity` → `VehicleViewModel(PleosVehicleDataSource(...))` 로 데이터 소스 주입 교체
5. 매니페스트 권한 선언
   - Pleos: `CAR_ENERGY` · `CAR_SAFETY` · `CAR_DRIVE` · `CAR_WINDOWS`
   - AAOS: `android.car.permission.CAR_POWERTRAIN`(기어) · `CAR_SPEED`(속도)

## 6. 구조
```
app/src/main/
  assets/car_*.png, car_*_NN.webp   # 3/4 렌더 이미지 + 24프레임 턴테이블
  assets/story/*.webp               # 정비 설명 카드 이미지
  assets/hotspots.json, price_data.json
  java/com/autograph/app/
    MainActivity.kt
    data/  Vehicle · MockVehicleDataSource · PleosVehicleDataSource
           GearSource · ValueEstimator · PriceData
    ui/    AutographApp(셸·기어전환) · ParkedScreen · DrivingScreen · EndScreen
           PlaceholderScreens(패스포트) · CareSheet · Story · ValueFlow · ValueChart
           CarTurntable · Hotspots · Car3DView · Components · Theme · VehicleViewModel
```

> ⚠️ 이 코드는 Android Studio에서 **Pleos Connect v2.1.0 에뮬레이터**로 빌드/실행합니다.
> 최초 Sync 시 버전만 맞으면 바로 동작하며, 핀/각도는 실행 후 눈으로 보정하세요.
