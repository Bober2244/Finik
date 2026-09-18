package dev.bober.finik.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project

/** Единые SDK/JVM-настройки для всех Android-модулей (app, core, feature). */
internal fun Project.configureKotlinAndroid(commonExtension: CommonExtension) {
    commonExtension.apply {
        compileSdk = 37
        defaultConfig.minSdk = 26

        // AGP 9 использует встроенный Kotlin: jvmTarget выравнивается по targetCompatibility.
        compileOptions.sourceCompatibility = JavaVersion.VERSION_17
        compileOptions.targetCompatibility = JavaVersion.VERSION_17
    }
}
