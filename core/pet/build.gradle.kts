plugins {
    alias(libs.plugins.finik.android.library.compose)
}

android {
    namespace = "dev.bober.finik.core.pet"
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.designsystem)
    // 4.38.0 is the stable 2026-09-20 release, compatible with our Kotlin 2.4 / Compose
    // 2026.09 baseline. It includes lifecycle-safe model disposal, embedded transparent
    // surfaces and frame-rate limiting. Keep Filament at the version supplied by SceneView.
    // https://github.com/sceneview/sceneview/releases/tag/v4.38.0
    implementation(libs.sceneview)
    implementation(libs.kotlinx.coroutines.android)
}
