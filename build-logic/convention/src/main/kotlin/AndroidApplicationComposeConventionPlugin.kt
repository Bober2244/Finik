import com.android.build.api.dsl.ApplicationExtension
import dev.bober.finik.buildlogic.configureAndroidCompose
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/** `finik.android.application.compose` — приложение + Jetpack Compose. */
class AndroidApplicationComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("finik.android.application")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            extensions.configure<ApplicationExtension> {
                configureAndroidCompose(this)
            }
        }
    }
}
