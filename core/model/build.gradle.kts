plugins {
    alias(libs.plugins.finik.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.bober.finik.core.model"
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
}
