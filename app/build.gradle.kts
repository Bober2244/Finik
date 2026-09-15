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
    implementation(projects.core.model)
    implementation(projects.core.designsystem)
    implementation(projects.core.navigation)

    // Feature-модули: по одному на экран/флоу. Новые добавлять сюда и в settings.gradle.kts.
    implementation(projects.feature.onboarding)
    implementation(projects.feature.home)
    implementation(projects.feature.plan)
    implementation(projects.feature.tasks)
    implementation(projects.feature.shop)
    implementation(projects.feature.goal)
    implementation(projects.feature.growth)
    implementation(projects.feature.profile)
    implementation(projects.feature.report)

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
