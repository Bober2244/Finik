import dev.bober.finik.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * `finik.android.feature` — модуль экрана/флоу.
 *
 * Даёт Compose, kotlinx.serialization (type-safe маршруты Navigation Compose)
 * и зависимости на core-модули, чтобы feature-модуль состоял только из UI и навигации.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("finik.android.library.compose")
            pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

            dependencies {
                "implementation"(project(":core:designsystem"))
                "implementation"(project(":core:navigation"))

                "implementation"(libs.findLibrary("androidx-activity-compose").get())
                "implementation"(libs.findLibrary("androidx-navigation-compose").get())
                "implementation"(libs.findLibrary("androidx-lifecycle-runtime-compose").get())
                "implementation"(libs.findLibrary("kotlinx-serialization-json").get())
            }
        }
    }
}
