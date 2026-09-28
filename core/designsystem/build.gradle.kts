plugins {
    alias(libs.plugins.finik.android.library.compose)
}

android {
    namespace = "dev.bober.finik.core.designsystem"
    buildFeatures {
        // Шрифты Nunito / Unbounded лежат в res/font.
        androidResources = true
    }
}

dependencies {
    api(project(":core:model"))

    // api: feature-модули получают Compose/Material3 транзитивно через дизайн-систему.
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.graphics)
    api(libs.androidx.compose.animation)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material.icons.extended)
}
