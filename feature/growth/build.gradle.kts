plugins {
    alias(libs.plugins.finik.android.feature)
}

android {
    namespace = "dev.bober.finik.feature.growth"
}

dependencies {
    implementation(projects.core.pet)
}
