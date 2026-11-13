package com.autograph.app.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.autograph.app.data.Gear
import com.autograph.app.data.GearSource
import com.autograph.app.data.VehicleSnapshot
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.awaitCancellation
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

    // 실제 기어 — 바뀔 때만 받는다(11-13 Pleos 성능 기준 '불필요한 폴링 최소화', 전엔 0.35초마다 읽음).
    // 콜백 등록이 안 되는 차에서만 예전처럼 0.35초 읽기, 그것도 화면이 보일 때만
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(realConnected) {
        if (!realConnected) return@LaunchedEffect
        gearSource.readGear()?.let { realGear = it }
        val stop = gearSource.listen { realGear = it }
        if (stop != null) {
            try { awaitCancellation() } finally { stop() }
        } else {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) {
                    gearSource.readGear()?.let { realGear = it }
                    delay(350)
                }
            }
        }
    }

    // 화면 상태(기어 + 트립 종료 + 패스포트)
    var endPending by remember { mutableStateOf(false) }
    var showPassport by remember { mutableStateOf(false) }
    var prevGear by remember { mutableStateOf(gear) }
    LaunchedEffect(gear) {
        val moving = prevGear == Gear.DRIVE || prevGear == Gear.REVERSE || prevGear == Gear.NEUTRAL
        val nowMoving = gear == Gear.DRIVE || gear == Gear.REVERSE || gear == Gear.NEUTRAL
        if (nowMoving && !moving) vm.startTrip()             // 새 주행 기록 시작
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
                // 기어 전환 시 화면이 급하게 바뀌지 않도록 부드럽게 크로스페이드
                Crossfade(targetState = scene, animationSpec = tween(400), label = "scene") { sc ->
                    when (sc) {
                        Scene.PARKED -> ParkedScreen(
                            snapshot = snapshot,
                            onOpenPassport = { showPassport = true },
                        )
                        Scene.DRIVING -> DrivingScreen(snapshot, vm::record)
                        Scene.END -> EndScreen(snapshot, vm.trip(), vm.tripSpeed, vm.tripBrake) { endPending = false }
                        Scene.PASSPORT -> PassportScreen(snapshot) { showPassport = false }
                    }
                }
            }
            DemoBar(gear, realConnected, scene, { manualGear = it }, vm.modelKeys, selected, vm::select)
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
        Modifier.fillMaxWidth().padding(horizontal = Dim.Pad, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(18.dp).background(
                Brush.sweepGradient(listOf(Palette.Good, Palette.Value, Palette.Accent, Palette.Good)),
                CircleShape
            )
        )
        Spacer(Modifier.width(12.dp))
        Text("AUTOGRAPH", color = Palette.Ink, fontSize = T.Body, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(18.dp))
        Text("${s.model} · ${s.year} · ${fmt(s.odometerKm)}km", color = Palette.Ink2, fontSize = T.Label)
        Spacer(Modifier.weight(1f))
        // 현재 기어 + 상태
        Text(
            "${gear.label} · ${sceneName(scene)}",
            color = if (gear == Gear.PARK) Palette.Good else Palette.Accent,
            fontSize = T.Label, fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(Palette.Panel, RoundedCornerShape(999.dp))
                .padding(horizontal = 20.dp, vertical = 8.dp)
        )
    }
}

/**
 * 하단 데모 바.
 * 실제 기어 연동 시: 기어 버튼은 숨김(상단에 현재 기어 표시) — 차종 데모 칩만.
 * 미연동(권한 없음 등): P/R/N/D 탭하여 데모 전환 + 차종 칩.
 * 주행 중에는 차종 칩도 숨김(주행 중 터치 요소 없음).
 */
@Composable
private fun DemoBar(
    gear: Gear, realConnected: Boolean, scene: Scene, onManual: (Gear) -> Unit,
    modelKeys: List<String>, selected: String, onSelect: (String) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = Dim.Pad, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!realConnected) {
            Text("데모 기어", color = Palette.InkMut, fontSize = T.Caption)
            Spacer(Modifier.width(14.dp))
            Segmented(
                listOf(Gear.PARK, Gear.REVERSE, Gear.NEUTRAL, Gear.DRIVE).map { it to it.label },
                gear, onManual, fontSize = T.Body,
            )
        } else {
            Text("● 실제 기어 연동됨", color = Palette.Good, fontSize = T.Caption)
        }
        Spacer(Modifier.weight(1f))
        if (scene != Scene.DRIVING) {
            // 화면 모드(자동 = 차의 낮/밤 신호) — 비교·시연용
            Text("화면", color = Palette.InkMut, fontSize = T.Caption)
            Spacer(Modifier.width(14.dp))
            Segmented(ThemeMode.values().map { it to it.label }, Palette.mode, { Palette.mode = it }, minWidth = 96.dp)
            Spacer(Modifier.width(28.dp))
            Text("데모 차종", color = Palette.InkMut, fontSize = T.Caption)
            Spacer(Modifier.width(14.dp))
            ModelChipRow(modelKeys, selected, onSelect)
        }
    }
}
