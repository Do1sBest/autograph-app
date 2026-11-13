package com.autograph.app.data

import android.car.Car
import android.car.VehicleGear
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
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
            toGear(m.getIntProperty(VehiclePropertyIds.GEAR_SELECTION, 0))
        } catch (t: Throwable) {
            null
        }
    }

    /**
     * 기어가 바뀔 때만 알려 줌 — 11-13 Pleos 성능 기준 '불필요한 폴링 최소화'(전엔 0.35초마다 읽었다). 받는 쪽은 메인 스레드.
     * 반환 = 그만 받기. 등록이 안 되면 null(그때만 부르는 쪽이 예전처럼 읽기로 대신).
     */
    fun listen(onGear: (Gear) -> Unit): (() -> Unit)? {
        val m = manager ?: return null
        val main = android.os.Handler(android.os.Looper.getMainLooper())
        val cb = object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>) {
                val v = value.value as? Int ?: return
                main.post { onGear(toGear(v)) }
            }

            override fun onErrorEvent(propertyId: Int, zone: Int) {}
        }
        val ok = try {
            m.registerCallback(cb, VehiclePropertyIds.GEAR_SELECTION, CarPropertyManager.SENSOR_RATE_ONCHANGE)
        } catch (t: Throwable) {
            false
        }
        if (!ok) return null
        return { try { m.unregisterCallback(cb) } catch (_: Throwable) {} }
    }

    private fun toGear(v: Int): Gear = when (v) {
        VehicleGear.GEAR_PARK -> Gear.PARK
        VehicleGear.GEAR_REVERSE -> Gear.REVERSE
        VehicleGear.GEAR_NEUTRAL -> Gear.NEUTRAL
        VehicleGear.GEAR_UNKNOWN -> Gear.UNKNOWN
        else -> Gear.DRIVE // GEAR_DRIVE 및 전진 단수
    }

    fun close() {
        try { car?.disconnect() } catch (_: Throwable) {}
    }
}
