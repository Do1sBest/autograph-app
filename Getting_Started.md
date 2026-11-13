# 오토그래프(AUTOGRAPH) — PleOS 앱 셋업 가이드

> 확정된 웹 프로토타입(오토그래프 인카 프로토타입)을 **PleOS 네이티브 앱**으로 옮기는 중입니다.
> **이번 단계: 주차중(01) 화면 — 3/4 뷰 상태맵 + 자산가치 밴드 + 하이라이트 핀.**
> 주행중(02)·운전종료(03)는 다음 단계에서 구현(현재는 안내 화면).

## 1. 요구 환경
- Android Studio (Koala 이상 권장), JDK 17
- Android SDK Platform 34, AGP 8.5.2 / Kotlin 1.9.24 / Gradle 8.9 (프로젝트에 고정)
- **PleOS 시뮬레이터(Pleos Simulator)** — 본 프로젝트는 **PleOS** 기준입니다(기존 AAOS 아님)
- GPU 하드웨어 가속 ON 권장(Filament 렌더링 품질/성능)

> **플랫폼 참고:** 기존 Android Automotive OS(AAOS) 기반에서 **PleOS 기반으로 전환**되었습니다.
> 표준 안드로이드 에뮬레이터가 아닌 **PleOS 시뮬레이터/에뮬레이터**로 실행하세요.

## 2. 열기 & 실행
1. `autograph-app` 폴더를 Android Studio로 **Open**
2. Gradle Sync (최초 1회 Filament 의존성 다운로드)
3. **PleOS 시뮬레이터(Pleos Simulator)** 실행
   - Pleos Playground 안내대로 PleOS 시스템 이미지/시뮬레이터를 띄웁니다.
   - GPU 하드웨어 가속 ON 권장
4. 실행 대상으로 PleOS 시뮬레이터 선택 후 **Run ▶**
5. PleOS 차량 런처에서 **"오토그래프"** 실행
6. 하단 **기어(P/R/N/D)** 또는 실제 기어 변경으로 화면 전환 확인

## 3. 데이터 소스
- 기본값은 **MockVehicleDataSource** — 시연 영상은 이 소스 그대로 녹화합니다.
- 값은 웹 프로토타입과 1:1 일치(투싼/아이오닉5/아반떼, 상태·가치·패스포트).
- 출처 태그(**실시간/추정**)도 동일하게 표기 → 5번 항목 참고.

## 4. 3D 차량(Filament → 이미지 폴백)
- 릴리스 빌드는 GLB를 3/4 뷰로 렌더한 투명 **PNG**(`assets/car_*.png`)를 사용합니다.
  시뮬레이터에서 Filament 네이티브 렌더가 크래시해 이미지 방식으로 대체했습니다.
- 실기기 헤드유닛용 Filament 구현은 `docs/filament-reference/` 에 보존되어 있습니다.
- 하이라이트 핀 좌표는 `ParkedScreen.kt`의 `HotspotPin` 정렬에서 차종별로 미세조정합니다.

## 5. 실데이터(PleOS) 연동 — 실시간 vs 추정
각 상태 항목에는 기술적 정직성을 위해 **출처 태그**를 붙입니다. 데이터는 두 종류이며,
가이드에서 반드시 구분해 표기합니다.

### 실시간(Real-time) — PleOS 시뮬레이터 API로 수신
**Pleos Vehicle SDK** 도메인 조회로 받아오는 실제 신호값입니다.

| 항목 | 도메인 |
|---|---|
| 주행거리 | `Odometer` |
| 타이어 | `Tire` |
| 브레이크 | `Brake` |
| EV 배터리 | `EvBattery` |
| 연료 | `Fuel` |
| 와이퍼(작동 상태) | `Wiper` |
| 스티어링 | `Steeringwheel` |
| 주행모드/기어 | `DrivingMode` |
| **브레이크 패드 잔량**(지원 차량 한정) | `BRAKE_PAD_WEAR_PERCENTAGE` → `100 − 마모%` |

### 추정(Estimated) — 실제 신호가 없어 알고리즘으로 계산
PleOS·표준 속성에 신호가 없어 **정비주기·주행거리 기반**으로 계산합니다.
실시간 측정값이 아니므로 반드시 **추정**으로 태그합니다.

| 항목 | 사유 |
|---|---|
| 엔진오일 | 전용 PleOS 도메인 없음. AAOS 표준 `ENGINE_OIL_*`는 모델 의존·보장 안 됨 → 추정. |
| 미션(변속기)오일 | PleOS 도메인·표준 속성 모두 없음 → 직접 취득 불가, 추정만. |
| 와이퍼 블레이드 수명 | `Wiper`는 작동 상태만 제공, 블레이드 마모는 아님 → 사용패턴 추정. |

> '실시간'과 '추정'을 명확히 구분하는 것이 심사의 **기술 활용성/정직성** 가점 포인트입니다.

### 실데이터 연동 절차
1. `settings.gradle.kts`에 Pleos maven 저장소 추가
2. `app/build.gradle.kts`의 Pleos SDK 의존성 주석 해제
3. `data/PleosVehicleDataSource.kt`의 TODO를 실제 도메인 호출로 교체
   - 각 도메인의 `checkXXXCapabilities`로 지원 여부 확인 후 조회(모델명 분기 금지)
4. `MainActivity` → `VehicleViewModel(PleosVehicleDataSource(...))` 로 주입 교체
5. 매니페스트에 필요한 차량 데이터 권한 선언

## 6. 구조
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

> ⚠️ 이 코드는 Android Studio에서 **PleOS 시뮬레이터**로 빌드/실행해야 합니다(이 환경에선 컴파일 검증 불가).
> 최초 Sync 시 버전만 맞으면 바로 동작하며, 핀/각도는 실행 후 눈으로 보정하세요.
