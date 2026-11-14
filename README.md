# 오토그래프 (AUTOGRAPH)

현대자동차그룹 **차량용 앱 개발 공모전** 출품작.
차량의 **상태(컨디션)** 와 **자산가치**를 인포그래픽으로 보여주는 **Pleos Connect** 차량용 앱입니다.

> "내 차가 지금 어떤 상태이고, 동일 연식 평균 대비 얼마나 가치 있는가"를 운전 흐름 속에서 한눈에.

---

## 핵심 아이디어
- 차량 상태를 **3/4 뷰 인포그래픽**으로 보여주고, 문제 부위는 **하이라이트 핀**으로 표시
- **동일 연식 평균 중고가 대비 내 차 가치 차이**를 핵심 지표로 제시 (+/−만원)
- 주행 데이터로 상태를 표시/추정하고, **밸류 패스포트**로 매각 가치 근거까지 연결
- 문제 항목은 **케어 시트**(왜 지금 → 권장 시점 → 사양 → 정품 부품)로 해결까지 연결

## 화면 구성 — 기어 연동
기어(P/R/N/D)에 따라 화면이 **자동 전환**됩니다. (기어 신호 미지원 시 하단 셀렉터로 수동 전환)

| 상태 | 기어 | 내용 |
|---|---|---|
| **주차 중** | P | 자산가치 밴드(평균 대비 차이) · 차량 3/4 상태맵 · 하이라이트 핀 · 컨디션 점수 |
| **주행 중** | D·R·N | 속도·브레이크·스티어링 **라이브 트레이스** · 상태 스트립 |
| **운전 종료** | 주행 후 P | 주행 리포트(거리·시간·급가속/제동·가치 영향) · 경로 맵 |
| **밸류 패스포트** | (주차 중 '자세히') | 정비/사고 이력 · 외관 상태 · 가치 산정 근거 |

추가 구성:
- **가치 산정 플로우(ValueFlow)** — 표준 시세 → 상태 반영 → 보정가로 이어지는 가치 계산 과정을 시각화
- **가치/시세 차트(ValueChart)** — 동일 연식 평균 대비 내 차 가치를 차트로 표시
- **정비 설명 카드(Story)** — 엔진오일·타이어 등 정비 항목을 이미지와 함께 설명

## 기술 스택
- **플랫폼:** Android Automotive OS(AAOS) 기반 **Pleos Connect** · **Jetpack Compose** · Kotlin
  - compileSdk/targetSdk 34 · minSdk 28 · JDK 17
- **기어 연동:** Android Automotive Car API — `android.car` `GEAR_SELECTION`
  - 권한 미승인·미지원 시 화면 하단 기어 셀렉터로 자동 폴백
- **차량 3D:** GLB를 3/4 뷰로 렌더한 투명 PNG + 24프레임 턴테이블 사용
  - 에뮬레이터에서 Filament 네이티브 렌더가 크래시해 이미지 방식으로 대체(Filament 의존성 비활성화)
- **데이터:** `MockVehicleDataSource`(데모) ↔ `PleosVehicleDataSource`(실차량, Pleos Vehicle SDK)

## 실행 방법
1. **Android Studio**로 `autograph-app` 폴더 **Open** → Gradle Sync
2. **Pleos Connect 에뮬레이터** 실행 (아래 테스트 환경 참고)
3. ▶ **Run** → Pleos Connect 런처에서 **"오토그래프"** 실행
4. 하단 **기어(P/R/N/D)** 또는 실제 기어 변경으로 화면 전환 확인

> gradlew 래퍼 없이 **Android Studio 내장 Gradle**로 빌드/실행합니다.
> 데모는 `MockVehicleDataSource`(가상 데이터)라 SDK 연결 없이 에뮬레이터에서 바로 실행됩니다.

## 테스트 환경
- **Pleos Connect Emulator v2.1.0 이상** (Form Factor: Automotive · API 34 · Cold boot · RAM 4GB)
- **화면 사양:** Pleos Connect v2.1.0 **AVD 기본 디스플레이 기준**
- **툴체인:** AGP 8.10.1 · Kotlin 2.0.20 · Gradle 8.11.1 · Compose BOM 2024.06.00
- 실시간 차량 신호 테스트: **Pleos Virtual Console**(ADB `tcp:8088` 포워딩)로 기어·속도 등 주입

## APK
- 위치: **`apk/죽을힘다해_AUTOGRAPH_release.apk`**
- 생성: Android Studio **Build → Build Bundle(s)/APK(s) → Build APK(s)** (설치·실행용 빌드)

## 데이터 정직성 — 실시간 vs 추정
상태 항목에 **출처 태그**(실시간 / 추정)를 붙여, 실제 신호로 받는 값과 알고리즘으로 계산하는 값을 명확히 구분합니다.

| 실시간 (실측·검증) | 추정 (신호 없음 → '추정' 표기) |
|---|---|
| 기어 P/R/N/D (`android.car GEAR_SELECTION`) | 타이어 공기압 수치 |
| 와이퍼 작동 상태 (`Wiper`) | 브레이크 패드 마모 |
| EV 배터리 SoC·주행가능거리 (`EvBattery`) | 엔진/미션 오일 |
| 타이어 저압 경고 TPMS (`Safety` 텔테일) | 12V 배터리 · 전비(평균) · 배터리 SoH |
| 주행거리·운전 부드러움 (속도 기반 계산) | |

> 상세 연동 절차·근거는 [`Getting_Started.md`](./Getting_Started.md) 참고.

## 관리 → 해결까지 (케어 시트)
문제를 알려주는 데서 끝내지 않고, **광고처럼 보이지 않게** 해결로 이어줍니다.
- 메인 화면에는 구매 버튼이 **없습니다.** 상태맵에서 **문제 항목(주의)만 탭** 가능
- 탭하면 케어 시트: **왜 지금인지 → 권장 시점 → 권장 사양 → 정품 부품**
- 액션은 **블루핸즈 예약(1차)**, **정품 부품 보기(2차, 텍스트 링크)** — 주차 중에만 노출
- 부품번호는 모델명만으로 확정되지 않아 **차대번호(VIN) → 모비스 정품 카탈로그(EPC)** 연동이 필요합니다.
  데모의 부품번호·사양은 **예시**이며, 오일 등급 같은 소모품 사양은 엔진/모델 스펙표로 먼저 제시할 수 있습니다.

## 외관 관리 전략
- **서라운드 뷰 카메라는 쓰지 않습니다.** 외부 카메라는 시스템(EVS) 권한 영역이라 일반 앱에 공개되지 않고,
  주차 보조용 광각이라 차체 전체의 스크래치·덴트 평가에도 맞지 않습니다.
- **사용자에게 촬영을 강요하지 않습니다.** 평소에는 충격·주차 이벤트 감지와 알림만.
- **촬영은 선택**: 매각 준비 시 10초 워크어라운드 → 밸류 패스포트의 **'검증 외관'** 항목으로 가치 근거가 됩니다.
- 사진이 들어오면 스크래치/덴트 등급화는 AI 손상 검출로 처리(로드맵).

## 프로젝트 구조
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
apk/                                # 제출용 APK
```

## 로드맵
- 마이현대/블루핸즈 **정비이력 연동**(차주 동의 기반)
- **VIN 기반 정품 부품(모비스 EPC) 연동** · 블루핸즈 예약 연동
- 외관: 충격 이벤트 감지 · 워크어라운드 촬영 · AI 손상 검출
- Pleos 실시간 도메인 연동 · 경로 맵 실데이터

---
*오토그래프 — 차량의 상태와 가치를 운전자의 일상 화면으로.*
