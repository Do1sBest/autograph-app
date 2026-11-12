package com.autograph.app.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.autograph.app.data.MockVehicleDataSource
import com.autograph.app.data.TripReport
import com.autograph.app.data.VehicleDataRepository
import com.autograph.app.data.VehicleSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 화면 전체가 공유하는 차량 상태 홀더.
 * 데모는 MockVehicleDataSource, 실차량은 PleosVehicleDataSource로 교체(생성자 주입).
 */
class VehicleViewModel(
    private val repo: VehicleDataRepository = MockVehicleDataSource(),
) : ViewModel() {

    val modelKeys: List<String> = repo.models()

    private val _selected = MutableStateFlow(modelKeys.first())
    val selected: StateFlow<String> = _selected.asStateFlow()

    private val _snapshot = MutableStateFlow(repo.snapshot(modelKeys.first()))
    val snapshot: StateFlow<VehicleSnapshot> = _snapshot.asStateFlow()

    fun select(key: String) {
        if (key == _selected.value) return
        _selected.value = key
        _snapshot.value = repo.snapshot(key)
    }

    fun trip(): TripReport = repo.trip(_selected.value)

    /**
     * 이번 주행 기록(1초에 한 번) — 주행 중에는 그래프를 그리지 않고 모아 두었다가
     * 운전 종료 화면에서 보여 준다(주행 중 1fps 넘는 움직임 금지 기준).
     */
    val tripSpeed = mutableStateListOf<Float>()
    val tripBrake = mutableStateListOf<Float>()

    fun startTrip() {
        tripSpeed.clear(); tripBrake.clear()
    }

    fun record(speed: Float, brake: Float) {
        tripSpeed.add(speed); tripBrake.add(brake)
    }
}
