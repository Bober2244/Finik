plugins {
    alias(libs.plugins.finik.android.library.compose)
}

android {
    namespace = "dev.bober.finik.core.navigation"
}

dependencies {
    api(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.animation)
}
