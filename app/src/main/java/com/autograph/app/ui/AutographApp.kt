package com.autograph.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.autograph.app.data.Gear
import com.autograph.app.data.GearSource
import com.autograph.app.data.VehicleSnapshot
import kotlinx.coroutines.delay

enum class Scene { PARKED, DRIVING, END, PASSPORT }

@Composable
fun AutographApp(vm: VehicleViewModel = viewModel()) {
    val ctx = LocalContext.current
    val gearSource = remember { GearSource(ctx) }
    DisposableEffect(Unit) { onDispose { gearSource.close() } }

    val realConnected = gearSource.connected
    var realGear by remember { mutableStateOf(Gear.PARK) }
    var manualGear by remember { mutableStateOf(Gear.PARK) }
    val gear = if (realConnected) realGear else manualGear

    // 실제 기어 폴링
    LaunchedEffect(realConnected) {
        if (realConnected) while (true) {
            gearSource.readGear()?.let { realGear = it }
            delay(350)
        }
    }

    // 화면 상태(기어 + 트립 종료 + 패스포트)
    var endPending by remember { mutableStateOf(false) }
    var showPassport by remember { mutableStateOf(false) }
    var prevGear by remember { mutableStateOf(gear) }
    LaunchedEffect(gear) {
        val moving = prevGear == Gear.DRIVE || prevGear == Gear.REVERSE || prevGear == Gear.NEUTRAL
        if (gear == Gear.PARK && moving) endPending = true   // 주행 후 P → 운전 종료
        if (gear != Gear.PARK) showPassport = false
        prevGear = gear
    }

    val scene = when {
        gear == Gear.DRIVE || gear == Gear.REVERSE || gear == Gear.NEUTRAL -> Scene.DRIVING
        endPending -> Scene.END
        showPassport -> Scene.PASSPORT
        else -> Scene.PARKED
    }

    val snapshot by vm.snapshot.collectAsStateWithLifecycle()
    val selected by vm.selected.collectAsStateWithLifecycle()

    Surface(color = Palette.Bg) {
        Column(Modifier.fillMaxSize()) {
            TopHeader(snapshot, gear, scene)
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (scene) {
                    Scene.PARKED -> ParkedScreen(
                        snapshot = snapshot,
                        modelKeys = vm.modelKeys,
                        selected = selected,
                        onSelect = vm::select,
                        onOpenPassport = { showPassport = true },
                    )
                    Scene.DRIVING -> DrivingScreen(snapshot)
                    Scene.END -> EndScreen(snapshot, vm.trip()) { endPending = false }
                    Scene.PASSPORT -> PassportScreen(snapshot) { showPassport = false }
                }
            }
            GearBar(gear, realConnected) { manualGear = it }
        }
    }
}

private fun sceneName(scene: Scene): String = when (scene) {
    Scene.PARKED -> "주차 중"
    Scene.DRIVING -> "주행 중"
    Scene.END -> "운전 종료"
    Scene.PASSPORT -> "밸류 패스포트"
}

@Composable
private fun TopHeader(s: VehicleSnapshot, gear: Gear, scene: Scene) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(14.dp).background(
                Brush.sweepGradient(listOf(Palette.Good, Palette.Value, Palette.Accent, Palette.Good)),
                CircleShape
            )
        )
        Spacer(Modifier.width(8.dp))
        Text("AUTOGRAPH", color = Palette.Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(12.dp))
        Text("${s.model} · ${s.year} · ${fmt(s.odometerKm)}km", color = Palette.Ink2, fontSize = 13.sp)
        Spacer(Modifier.weight(1f))
        // 현재 기어 + 상태
        Text(
            "${gear.label} · ${sceneName(scene)}",
            color = if (gear == Gear.PARK) Palette.Good else Palette.Accent,
            fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .border(1.dp, Palette.Line, RoundedCornerShape(999.dp))
                .padding(horizontal = 14.dp, vertical = 5.dp)
        )
    }
}

/**
 * 하단 기어 표시/셀렉터.
 * 실제 기어 연동 시: 현재 단수 표시(읽기 전용).
 * 미연동(권한 없음 등): P/R/N/D 탭하여 데모 전환.
 */
@Composable
private fun GearBar(gear: Gear, realConnected: Boolean, onManual: (Gear) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(Gear.PARK, Gear.REVERSE, Gear.NEUTRAL, Gear.DRIVE).forEach { g ->
                val on = g == gear
                Text(
                    g.label,
                    color = if (on) Color(0xFF06101F) else Palette.Ink2,
                    fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(if (on) Palette.Accent else Palette.Panel, RoundedCornerShape(10.dp))
                        .border(1.dp, if (on) Palette.Accent else Palette.Line, RoundedCornerShape(10.dp))
                        .then(if (realConnected) Modifier else Modifier.clickable { onManual(g) })
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }
        }
        Spacer(Modifier.weight(1f))
        Text(
            if (realConnected) "● 실제 기어 연동됨" else "데모 기어 · 탭하여 변경 (실연동은 Pleos 단계)",
            color = if (realConnected) Palette.Good else Palette.InkMut,
            fontSize = 12.sp
        )
    }
}
