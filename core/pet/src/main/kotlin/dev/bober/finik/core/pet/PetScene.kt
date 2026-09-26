package dev.bober.finik.core.pet

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.filament.Engine
import com.google.android.filament.MaterialInstance
import com.google.android.filament.Texture
import com.google.android.filament.TextureSampler
import dev.bober.finik.core.model.PetAccessory
import dev.bober.finik.core.model.PetAppearance
import dev.bober.finik.core.model.PetFurColor
import dev.bober.finik.core.model.PetMood
import dev.bober.finik.core.model.PetSpecies
import io.github.sceneview.FrameRatePolicy
import io.github.sceneview.SceneView
import io.github.sceneview.SurfaceType
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.math.Position
import io.github.sceneview.math.Scale
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.model.model
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberCameraManipulator
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.texture.ImageTexture
import io.github.sceneview.texture.setBitmap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder

@Composable
internal fun PetScene(
    species: PetSpecies,
    stageIndex: Int,
    appearance: PetAppearance,
    mood: PetMood?,
    action: PetAnimation,
    actionEventId: Long,
    animate: Boolean,
    modifier: Modifier = Modifier,
) {
    var retry by remember { mutableIntStateOf(0) }
    key(retry) {
        PetSceneSession(species, stageIndex, appearance, mood, action, actionEventId, animate, modifier) {
            retry++
        }
    }
}

@Composable
private fun PetSceneSession(
    species: PetSpecies,
    stageIndex: Int,
    appearance: PetAppearance,
    mood: PetMood?,
    action: PetAnimation,
    actionEventId: Long,
    animate: Boolean,
    modifier: Modifier,
    onRetry: () -> Unit,
) {
    // The helpers own native resources and destroy them when this scene leaves composition.
    // SceneView suspends its frame loop with the host lifecycle when the app is backgrounded.
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val loaded = rememberPetModel(engine, modelLoader, species.modelAssetPath)
    var frameFailed by remember { mutableStateOf(false) }
    val cameraNode = rememberCameraNode(engine) {
        position = Position(0f, .10f, 3.05f)
        lookAt(Position(0f, .10f, 0f))
    }
    val cameraManipulator = rememberCameraManipulator(
        orbitHomePosition = Position(0f, .10f, 3.05f),
        targetPosition = Position(0f, .10f, 0f),
    )

    Box(modifier) {
        val asset = loaded.asset
        if (asset != null && !frameFailed) {
            val selectedColor = rememberPetColor(appearance.furColor)
            val controller = remember(asset) { PetSceneController(asset, engine) }
            var firstFrameRendered by remember(asset) { mutableStateOf(false) }
            val height = (asset.instance.model.boundingBox.halfExtent[1] * 2f).coerceAtLeast(.01f)
            val modelScale = (1.70f + stageIndex.coerceIn(0, 4) * .075f) / height
            SceneView(
                modifier = Modifier.fillMaxSize(),
                engine = engine,
                modelLoader = modelLoader,
                // Embedded surfaces respect Compose clipping, scrolling and transparent cards.
                surfaceType = SurfaceType.TextureSurface,
                isOpaque = false,
                cameraNode = cameraNode,
                cameraManipulator = cameraManipulator,
                autoCenterContent = false,
                frameRatePolicy = FrameRatePolicy.OnDemand(maxFps = 30),
                onFrame = { frameTimeNanos ->
                    controller.onFrame(frameTimeNanos)
                    if (!firstFrameRendered) firstFrameRendered = true
                },
            ) {
                ModelNode(
                    modelInstance = asset.instance,
                    autoAnimate = false,
                    scale = Scale(modelScale),
                    position = Position(0f, -.91f, 0f),
                    apply = {
                        controller.bind(this)
                        onFrameError = { error ->
                            Log.e("FinikPet", "Pet animation frame failed", error)
                            frameFailed = true
                        }
                    },
                )
                SideEffect { controller.update(appearance, selectedColor, mood, action, actionEventId, animate) }
            }
            if (!firstFrameRendered) {
                PetLoading(Modifier.fillMaxSize())
            }
        } else {
            if (loaded.failed || frameFailed) {
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = .94f),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(6.dp)) {
                        Text("Питомец временно недоступен", style = MaterialTheme.typography.labelSmall)
                        TextButton(onClick = onRetry) { Text("Повторить") }
                    }
                }
            } else {
                PetLoading(Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun PetLoading(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        Text("Загружаем сову…", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 8.dp))
    }
}

private class PetAsset(val instance: ModelInstance) {
    var colorTexture: Texture? = null

    fun destroy(modelLoader: ModelLoader, engine: Engine) {
        modelLoader.destroyModel(instance.model)
        colorTexture?.let(engine::destroyTexture)
    }
}

private data class PetLoadState(val asset: PetAsset? = null, val failed: Boolean = false)

@Composable
private fun rememberPetModel(engine: Engine, modelLoader: ModelLoader, path: String): PetLoadState {
    val context = LocalContext.current
    val state by produceState(PetLoadState(), modelLoader, path) {
        try {
            // Only file IO runs off-main. All Filament calls stay on Main.
            val buffer = withContext(Dispatchers.IO) {
                val bytes = context.assets.open(path).use { it.readBytes() }
                val buffer = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.LITTLE_ENDIAN)
                buffer.put(bytes).rewind()
                buffer
            }
            // Each scene receives independent material instances for its selected color.
            value = PetLoadState(PetAsset(modelLoader.createModelInstance(buffer)))
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Log.e("FinikPet", "Unable to load $path", error)
            value = PetLoadState(failed = true)
        }
    }
    // Capture the value, not the delegated state: disposing the previous null-key effect
    // must never read and destroy the asset that has just finished loading.
    val asset = state.asset
    DisposableEffect(asset) {
        onDispose { asset?.destroy(modelLoader, engine) }
    }
    return state
}

private data class LoadedColor(val color: PetFurColor, val bitmap: Bitmap)

@Composable
private fun rememberPetColor(color: PetFurColor): LoadedColor? {
    val context = LocalContext.current
    val loaded by produceState<LoadedColor?>(null, color) {
        try {
            val bitmap = withContext(Dispatchers.IO) {
                context.assets.open(color.textureAssetPath).use { stream ->
                    BitmapFactory.decodeStream(stream, null, BitmapFactory.Options().apply {
                        inScaled = false
                        // The preview is at most a few hundred dp; 1024 px avoids six 2048 px
                        // GPU copies on modest Android devices while retaining crisp feathers.
                        inSampleSize = 2
                    }) ?: error("Unable to decode ${color.textureAssetPath}")
                }
            }
            value = LoadedColor(color, bitmap)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Log.e("FinikPet", "Unable to load owl color ${color.assetId}", error)
        }
    }
    return loaded
}

/** Owns clip transitions and one reusable color texture per visible scene. */
private class PetSceneController(private val asset: PetAsset, private val engine: Engine) {
    private lateinit var node: ModelNode
    private var previousAppearance: PetAppearance? = null
    private var appliedColor: PetFurColor? = null
    private var previousAction: Pair<PetAnimation, Long>? = null
    private var previousAnimate: Boolean? = null
    private var previousMood: PetMood? = null
    private var restingClip = PetAnimation.IDLE
    private var actionEndNanos: Long? = null
    private var isAnimating = true

    fun bind(node: ModelNode) {
        if (::node.isInitialized && this.node === node) return
        this.node = node
        previousAppearance = null
        previousAction = null
        previousMood = null
        appliedColor = null
        // The GLB ships with all four meshes visible. Hide them before its first frame.
        node.renderableNodes.filter { it.name.orEmpty().startsWith("Accessory_") }
            .forEach { it.isVisible = false }
    }

    fun update(
        appearance: PetAppearance,
        loadedColor: LoadedColor?,
        mood: PetMood?,
        action: PetAnimation,
        eventId: Long,
        animate: Boolean,
    ) {
        if (appearance.accessories != previousAppearance?.accessories) {
            applyAccessories(appearance.accessories)
        }
        if (loadedColor?.color == appearance.furColor && appliedColor != appearance.furColor) {
            // The GLB already embeds blue. Keep its original sampler until another color is chosen.
            if (appearance.furColor != PetFurColor.BLUE || asset.colorTexture != null) {
                applyColor(loadedColor)
            }
            appliedColor = appearance.furColor
        }
        previousAppearance = appearance
        val rest = if (mood == PetMood.SAD || mood == PetMood.BORED) PetAnimation.CALM else PetAnimation.IDLE
        val restChanged = rest != restingClip
        val enteringSad = mood == PetMood.SAD && previousMood != PetMood.SAD
        restingClip = rest
        isAnimating = animate
        val requested = action to eventId
        when {
            !animate && previousAnimate != false -> play(PetAnimation.IDLE, loop = false, frozen = true)
            animate && (requested != previousAction || previousAnimate != true) -> {
                if (action == PetAnimation.IDLE && enteringSad) {
                    play(PetAnimation.HELP, loop = false)
                } else if (action == PetAnimation.IDLE) play(restingClip, loop = true)
                else play(action, loop = false)
            }
            animate && (restChanged || enteringSad) && actionEndNanos == null -> {
                if (enteringSad) play(PetAnimation.HELP, loop = false)
                else play(restingClip, loop = true)
            }
        }
        previousAction = requested
        previousAnimate = animate
        previousMood = mood
    }

    fun onFrame(frameTimeNanos: Long) {
        if (isAnimating && actionEndNanos?.let { frameTimeNanos >= it } == true) {
            play(restingClip, loop = true)
        }
    }

    private fun play(request: PetAnimation, loop: Boolean, frozen: Boolean = false) {
        val animator = node.animator
        val indices = 0 until animator.animationCount
        val requestedIndex = indices.firstOrNull { animator.getAnimationName(it).equals(request.clipName, ignoreCase = true) }
        val index = requestedIndex
            ?: indices.firstOrNull { animator.getAnimationName(it).equals(PetAnimation.IDLE.clipName, ignoreCase = true) }
            ?: indices.firstOrNull()
            ?: return
        node.playingAnimations.keys.toList().forEach(node::stopAnimation)
        // Apply the first pose immediately so interrupted clips never linger in the next action.
        animator.applyAnimation(index, 0f)
        animator.updateBoneMatrices()
        val effectiveLoop = loop || requestedIndex == null
        if (!frozen) node.playAnimation(index, loop = effectiveLoop)
        actionEndNanos = if (!frozen && !effectiveLoop) {
            System.nanoTime() + (animator.getAnimationDuration(index).coerceAtLeast(.01f) * 1_000_000_000).toLong()
        } else null
        node.requestRender()
    }

    private fun applyAccessories(accessories: Set<PetAccessory>) {
        val names = accessories.mapTo(mutableSetOf()) { "Accessory_${it.id}" }
        node.renderableNodes.forEach { renderable ->
            val name = renderable.name.orEmpty()
            if (name.startsWith("Accessory_")) {
                renderable.isVisible = name in names
            }
        }
        node.requestRender()
    }

    private fun applyColor(color: LoadedColor) {
        val bodyMaterial: MaterialInstance = node.renderableNodes.asSequence()
            .flatMap { it.materialInstances.asSequence() }
            .firstOrNull { it.name == "BakedMaterial" && it.material.hasParameter("baseColorMap") }
            ?: run {
                Log.e("FinikPet", "Owl body material BakedMaterial/baseColorMap is missing")
                return
            }
        val texture = asset.colorTexture?.also {
            it.setBitmap(engine, color.bitmap)
            it.generateMipmaps(engine)
        } ?: ImageTexture.Builder().bitmap(color.bitmap).build(engine).also {
            asset.colorTexture = it
        }
        bodyMaterial.setParameter("baseColorMap", texture, TextureSampler())
        node.requestRender()
    }
}
