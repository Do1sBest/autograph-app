package com.autograph.app.data

import android.content.Context
import org.json.JSONObject

/**
 * 가격·정비 자료 (assets/price_data.json) — 엑셀 '오토그래프_가격자료_1113.xlsx'와 같은 원본에서 생성.
 * 숫자는 공식 출처 값 그대로. 실제 시세 샘플로 맞추는 '차종 보정'은 아직 없음(시세 보정 전).
 */
data class PartPrice(val name: String, val ptno: String, val price: Int)

data class CareInfo(
    val interval: String,
    val spec: String,
    val parts: List<PartPrice>,
    val missing: String,
    val source: String,
    val intervalNormal: String = "",
    val intervalSevere: String = "",
    val specMain: String = "",
    val specSub: String = "",
    val sourceShort: String = "",
) {
    val partsSum: Int get() = parts.sumOf { it.price }
}

data class CarPrice(
    val trim: String,
    val newPrice: Int,
    val priceNote: String,
    val items: Map<String, CareInfo>,
)

data class PriceData(
    val asOf: String,
    val residualLabel: String,
    val residualCaveat: String,
    val rates: List<Double>,          // index 0 = 1년 미만, n = n년
    val cpoYears: Int,
    val cpoKm: Int,
    val cars: Map<String, CarPrice>,
) {
    /** 경과연수 n 의 잔가율 (표 끝을 넘으면 마지막 값) */
    fun rate(age: Int): Double = rates[age.coerceIn(0, rates.lastIndex)]

    companion object {
        fun load(ctx: Context): PriceData? = runCatching {
            val o = JSONObject(ctx.assets.open("price_data.json").bufferedReader(Charsets.UTF_8).use { it.readText() })
            val r = o.getJSONObject("residual")
            val rates = r.getJSONArray("rates").let { a -> List(a.length()) { a.getDouble(it) } }
            val cpo = o.getJSONObject("cpo")
            val carsO = o.getJSONObject("cars")
            val cars = carsO.keys().asSequence().associateWith { k ->
                val c = carsO.getJSONObject(k)
                val itemsO = c.getJSONObject("items")
                CarPrice(
                    trim = c.getString("trim"),
                    newPrice = c.getInt("newPrice"),
                    priceNote = c.getString("priceNote"),
                    items = itemsO.keys().asSequence().associateWith { name ->
                        val i = itemsO.getJSONObject(name)
                        val pa = i.getJSONArray("parts")
                        CareInfo(
                            interval = i.getString("interval"),
                            spec = i.getString("spec"),
                            parts = List(pa.length()) { j ->
                                pa.getJSONObject(j).let { PartPrice(it.getString("name"), it.getString("ptno"), it.getInt("price")) }
                            },
                            missing = i.getString("missing"),
                            source = i.getString("source"),
                            intervalNormal = i.optString("intervalNormal"),
                            intervalSevere = i.optString("intervalSevere"),
                            specMain = i.optString("specMain"),
                            specSub = i.optString("specSub"),
                            sourceShort = i.optString("sourceShort"),
                        )
                    },
                )
            }
            PriceData(
                asOf = o.getString("asOf"),
                residualLabel = r.getString("label"),
                residualCaveat = r.getString("caveat"),
                rates = rates,
                cpoYears = cpo.getInt("years"),
                cpoKm = cpo.getInt("km"),
                cars = cars,
            )
        }.getOrNull()
    }
}
