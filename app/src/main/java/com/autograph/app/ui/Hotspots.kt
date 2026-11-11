package com.autograph.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.json.JSONObject

/** 한 프레임에서의 핀 위치(프레임 비율 0~1) + 가시성(앞을 향할 때만 true). */
data class PinPos(val x: Float, val y: Float, val visible: Boolean)

/**
 * assets/hotspots.json 로드:
 *   { "tucson": { "hood": [{x,y,v} | null, ...24], "tire": [...] }, "ioniq5": {...}, "elantra": {...} }
 * 반환: carKey -> (role -> 프레임별 PinPos?)  (role: "hood" | "tire")
 */
@Composable
fun rememberHotspots(): Map<String, Map<String, List<PinPos?>>> {
    val ctx = LocalContext.current
    return remember {
        runCatching {
            val txt = ctx.assets.open("hotspots.json").bufferedReader().use { it.readText() }
            val root = JSONObject(txt)
            val cars = HashMap<String, Map<String, List<PinPos?>>>()
            for (car in root.keys()) {
                val co = root.getJSONObject(car)
                val roles = HashMap<String, List<PinPos?>>()
                for (role in co.keys()) {
                    val arr = co.getJSONArray(role)
                    val list = ArrayList<PinPos?>(arr.length())
                    for (i in 0 until arr.length()) {
                        if (arr.isNull(i)) {
                            list.add(null)
                        } else {
                            val o = arr.getJSONObject(i)
                            list.add(PinPos(o.getDouble("x").toFloat(), o.getDouble("y").toFloat(), o.getBoolean("v")))
                        }
                    }
                    roles[role] = list
                }
                cars[car] = roles
            }
            cars as Map<String, Map<String, List<PinPos?>>>
        }.getOrDefault(emptyMap())
    }
}

/** HotspotRole → JSON role 키. */
fun hotspotRoleKey(role: com.autograph.app.data.HotspotRole): String? = when (role) {
    com.autograph.app.data.HotspotRole.HOOD -> "hood"
    com.autograph.app.data.HotspotRole.TIRE_RR -> "tire"
    else -> null
}
