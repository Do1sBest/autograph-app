package com.autograph.app.data

import android.car.Car
import android.car.VehicleGear
import android.car.VehiclePropertyIds
import android.car.hardware.property.CarPropertyManager
import android.content.Context

/** 변속 단수. */
enum class Gear(val label: String) {
    PARK("P"), REVERSE("R"), NEUTRAL("N"), DRIVE("D"), UNKNOWN("–")
}

/**
 * 실제 차량 기어(GEAR_SELECTION)를 Android Automotive Car API로 읽는다.
 *
 * - 권한(CAR_POWERTRAIN)이 없거나 Car 서비스가 없으면 connected=false → 앱은 화면 기어 셀렉터로 폴백.
 * - 모든 호출을 try/catch(Throwable)로 감싸 앱이 절대 죽지 않게 한다.
 *   (일반 앱은 스톡 에뮬레이터에서 이 권한을 못 받을 수 있어 폴백이 기본 동작일 수 있음)
 */
class GearSource(context: Context) {
    private var car: Car? = null
    private var manager: CarPropertyManager? = null

    /** 실제 기어를 읽을 수 있으면 true. false면 UI가 수동 셀렉터를 노출. */
    var connected: Boolean = false
        private set

    init {
        try {
            val c = Car.createCar(context.applicationContext)
            car = c
            val m = c.getCarManager(Car.PROPERTY_SERVICE) as? CarPropertyManager
            manager = m
            connected = m != null &&
                runCatching { m.getIntProperty(VehiclePropertyIds.GEAR_SELECTION, 0) }.isSuccess
        } catch (t: Throwable) {
            connected = false
        }
    }

    /** 현재 기어. 읽기 실패 시 null. */
    fun readGear(): Gear? {
        val m = manager ?: return null
        return try {
            when (m.getIntProperty(VehiclePropertyIds.GEAR_SELECTION, 0)) {
                VehicleGear.GEAR_PARK -> Gear.PARK
                VehicleGear.GEAR_REVERSE -> Gear.REVERSE
                VehicleGear.GEAR_NEUTRAL -> Gear.NEUTRAL
                VehicleGear.GEAR_UNKNOWN -> Gear.UNKNOWN
                else -> Gear.DRIVE // GEAR_DRIVE 및 전진 단수
            }
        } catch (t: Throwable) {
            null
        }
    }

    fun close() {
        try { car?.disconnect() } catch (_: Throwable) {}
    }
}
