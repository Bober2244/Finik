import org.gradle.api.GradleException

plugins {
    alias(libs.plugins.finik.android.application)
    alias(libs.plugins.finik.android.application.compose)
    alias(libs.plugins.kotlin.serialization)
}

val releaseKeystorePath = providers.environmentVariable("FINIK_KEYSTORE_PATH").orNull
val releaseKeystorePassword = providers.environmentVariable("FINIK_KEYSTORE_PASSWORD").orNull
val releaseKeyAlias = providers.environmentVariable("FINIK_KEY_ALIAS").orNull
val releaseKeyPassword = providers.environmentVariable("FINIK_KEY_PASSWORD").orNull
val signingValues = listOf(releaseKeystorePath, releaseKeystorePassword, releaseKeyAlias, releaseKeyPassword)
if (signingValues.any { it != null } && signingValues.any { it.isNullOrBlank() }) {
    throw GradleException("Set all four FINIK_KEYSTORE_* / FINIK_KEY_* variables for a signed release")
}
val hasReleaseSigning = signingValues.all { !it.isNullOrBlank() }
val debugApiBaseUrl = providers.environmentVariable("FINIK_DEBUG_API_BASE_URL").orElse("http://192.168.1.43:8000").get()
val releaseApiBaseUrl = providers.environmentVariable("FINIK_API_BASE_URL").orElse("").get()
if (releaseApiBaseUrl.isNotBlank() && !releaseApiBaseUrl.startsWith("https://")) {
    throw GradleException("FINIK_API_BASE_URL must use HTTPS in release builds")
}
if (listOf(debugApiBaseUrl, releaseApiBaseUrl).any { '"' in it || '\n' in it || '\r' in it }) {
    throw GradleException("API base URL contains an unsupported character")
}

android {
    namespace = "dev.bober.finik"

    defaultConfig {
        applicationId = "dev.bober.finik"
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("finikRelease") {
                val keystore = file(requireNotNull(releaseKeystorePath))
                if (!keystore.isFile) {
                    throw GradleException("FINIK_KEYSTORE_PATH does not point to a file: $keystore")
                }
                storeFile = keystore
                storePassword = releaseKeystorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"$debugApiBaseUrl\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField("String", "API_BASE_URL", "\"$releaseApiBaseUrl\"")
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("finikRelease")
            }
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
    implementation(projects.core.data)
    implementation(projects.core.network)
    implementation(projects.core.pet)

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
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
