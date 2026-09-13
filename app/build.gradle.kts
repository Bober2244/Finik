plugins {
    alias(libs.plugins.finik.android.application)
    alias(libs.plugins.finik.android.application.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.bober.finik"

    defaultConfig {
        applicationId = "dev.bober.finik"
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:navigation"))

    // Feature-модули: по одному на экран/флоу. Новые добавлять сюда и в settings.gradle.kts.
    implementation(project(":feature:onboarding"))
    implementation(project(":feature:home"))
    implementation(project(":feature:plan"))
    implementation(project(":feature:tasks"))
    implementation(project(":feature:shop"))
    implementation(project(":feature:goal"))
    implementation(project(":feature:growth"))
    implementation(project(":feature:profile"))
    implementation(project(":feature:report"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
