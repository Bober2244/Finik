package dev.bober.finik.core.pet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.google.android.filament.Engine
import io.github.sceneview.loaders.EnvironmentLoader
import io.github.sceneview.loaders.MaterialLoader
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberMaterialLoader

/** Filament resources that must outlive individual navigation destinations. */
internal class PetRuntime(
    val engine: Engine,
    val materialLoader: MaterialLoader,
    val environmentLoader: EnvironmentLoader,
)

internal val LocalPetRuntime = staticCompositionLocalOf<PetRuntime?> { null }

@Composable
fun PetRuntimeProvider(content: @Composable () -> Unit) {
    // EnvironmentLoader.destroy() destroys its IBL prefilter. Doing that for every
    // tab switch can block input for several seconds on the UI thread.
    val engine = rememberEngine()
    val materialLoader = rememberMaterialLoader(engine)
    val environmentLoader = rememberEnvironmentLoader(engine)
    val runtime = remember(engine, materialLoader, environmentLoader) {
        PetRuntime(engine, materialLoader, environmentLoader)
    }
    CompositionLocalProvider(
        LocalPetRuntime provides runtime,
        content = content,
    )
}
