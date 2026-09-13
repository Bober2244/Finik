package dev.bober.finik.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/** Включает Compose и подключает BOM + tooling в модуль. */
internal fun Project.configureAndroidCompose(commonExtension: CommonExtension) {
    commonExtension.buildFeatures.compose = true

    dependencies {
        val bom = libs.findLibrary("androidx-compose-bom").get()
        "implementation"(platform(bom))
        "implementation"(libs.findLibrary("androidx-compose-ui-tooling-preview").get())
        "debugImplementation"(libs.findLibrary("androidx-compose-ui-tooling").get())
        "androidTestImplementation"(platform(bom))
    }
}
