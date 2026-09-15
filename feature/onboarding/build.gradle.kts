plugins {
    alias(libs.plugins.finik.android.feature)
}

android {
    namespace = "dev.bober.finik.feature.onboarding"
}

dependencies {
    implementation(projects.core.pet)
}
