plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.autograph.app"
    compileSdk = 34

    // Android Automotive Car API (GEAR_SELECTION 등) 컴파일 지원
    useLibrary("android.car")

    defaultConfig {
        applicationId = "com.autograph.app"
        minSdk = 28           // 공모전 요건: Android API 28 이상
        targetSdk = 34
        versionCode = 1
        versionName = "0.2.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    // GLB는 압축하지 않고 그대로 패키징 (assets)
    androidResources {
        noCompress += listOf("glb", "ktx")
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")

    // ===== Filament (네이티브 3D) — 에뮬레이터에서 SIGSEGV 크래시로 비활성화 =====
    // 데모/에뮬레이터: assets의 3/4 렌더 PNG 사용(CarImageView).
    // 실제 차량 헤드유닛에서 실시간 3D를 쓰려면 아래 주석 해제 +
    // docs/filament-reference/Car3DView.kt.txt 를 ui/ 로 복원.
    // val filament = "1.51.0"
    // implementation("com.google.android.filament:filament-android:$filament")
    // implementation("com.google.android.filament:gltfio-android:$filament")
    // implementation("com.google.android.filament:filament-utils-android:$filament")

    // ===== Pleos Connect SDK (실데이터 연동 시 주석 해제) =====
    // implementation("ai.pleos.connect:vehicle-sdk:<version>")        // 차량 상태 데이터
    // implementation("ai.pleos.connect:fusedlocation-sdk:<version>")  // 위치
    // implementation("ai.pleos.connect:navihelper-sdk:<version>")     // 서비스센터/충전소 연계
    // implementation("ai.pleos.connect:tts-sdk:<version>")            // 음성 출력
    // implementation("ai.pleos.connect:stt-sdk:<version>")            // 음성 입력
}
