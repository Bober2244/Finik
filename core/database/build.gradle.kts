plugins {
    alias(libs.plugins.finik.android.library)
    alias(libs.plugins.ksp)
}

android {
    namespace = "dev.bober.finik.core.database"
}

dependencies {
    implementation(projects.core.model)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
}
