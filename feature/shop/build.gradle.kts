plugins {
    alias(libs.plugins.finik.android.feature)
}

android {
    namespace = "dev.bober.finik.feature.shop"
}

dependencies {
    implementation(libs.coil.compose)
    implementation(libs.coil.okhttp)
}

