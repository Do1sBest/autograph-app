# 오토그래프 (AUTOGRAPH) — AAOS 앱

현대자동차그룹 차량용 앱 개발 공모전 출품작. 차량의 **상태 + 자산가치**를
인포그래픽으로 보여주는 Android Automotive(AAOS) 앱입니다.

- 플랫폼: Android Automotive OS / Pleos Connect, Jetpack Compose, minSdk 28
- 3D 차량: **Filament**(네이티브) — `assets/*.glb`
- 데이터: `MockVehicleDataSource`(데모) ↔ `PleosVehicleDataSource`(실차량)

## 현재 구현 상태
- [x] **01 주차 중** — 자산가치 밴드(동일 연식 평균 대비 차이) · 차량 상태맵(3/4 3D) · 하이라이트 핀 · 차종 전환 · 밸류 패스포트 이동
- [ ] 02 주행 중 — 라이브 트레이스 (다음 단계)
- [ ] 03 운전 종료 — 주행 리포트/경로 맵 (다음 단계)
- [x] 04 밸류 패스포트 — 기본 목록 + 뒤로가기

빌드·실행·튜닝·실데이터 연동은 **`셋업가이드.md`** 참고.
