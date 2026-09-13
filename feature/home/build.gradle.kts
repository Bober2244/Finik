plugins {
    alias(libs.plugins.finik.android.feature)
}

android {
    namespace = "dev.bober.finik.feature.home"
}

dependencies {
    implementation(project(":core:pet"))
}
