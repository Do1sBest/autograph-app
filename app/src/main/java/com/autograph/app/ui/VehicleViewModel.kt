package com.autograph.app.ui

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
}
