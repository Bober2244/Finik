plugins {
    alias(libs.plugins.finik.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.bober.finik.core.network"
}

dependencies {
    implementation(projects.core.model)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.serialization.json)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
}
