import com.android.build.api.dsl.LibraryExtension
import dev.bober.finik.buildlogic.configureAndroidCompose
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/** `finik.android.library.compose` — библиотека + Jetpack Compose. */
class AndroidLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("finik.android.library")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            extensions.configure<LibraryExtension> {
                configureAndroidCompose(this)
            }
        }
    }
}
