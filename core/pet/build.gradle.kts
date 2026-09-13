plugins {
    alias(libs.plugins.finik.android.library.compose)
}

android {
    namespace = "dev.bober.finik.core.pet"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:designsystem"))
    // TODO(3d): когда модель из Blender будет готова, подключить рендерер glTF/GLB
    // (например io.github.sceneview:sceneview или com.google.android.filament) здесь.
}
