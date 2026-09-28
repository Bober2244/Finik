package dev.bober.finik.core.pet

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.rememberUpdatedState
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
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberSurfaceMirrorer
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
    modelScaleMultiplier: Float,
    onSceneReady: () -> Unit,
    onSceneFailure: () -> Unit,
    onSceneRetry: () -> Unit,
    onInteractionChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var retry by remember { mutableIntStateOf(0) }
    key(retry) {
        PetSceneSession(species, stageIndex, appearance, mood, action, actionEventId, animate, modelScaleMultiplier, onSceneReady, onSceneFailure, onInteractionChange, modifier) {
            onSceneRetry()
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
    modelScaleMultiplier: Float,
    onSceneReady: () -> Unit,
    onSceneFailure: () -> Unit,
    onInteractionChange: (Boolean) -> Unit,
    modifier: Modifier,
    onRetry: () -> Unit,
) {
    // Scene-specific resources leave with this route; shared loaders survive tab switches.
    // SceneView suspends its frame loop with the host lifecycle when backgrounded.
    val runtime = LocalPetRuntime.current
    val engine = runtime?.engine ?: rememberEngine()
    val modelLoader = runtime?.modelLoader ?: rememberModelLoader(engine)
    val materialLoader = runtime?.materialLoader ?: rememberMaterialLoader(engine)
    val environmentLoader = runtime?.environmentLoader ?: rememberEnvironmentLoader(engine)
    val surfaceMirrorer = rememberSurfaceMirrorer()
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
            val currentOnInteractionChange by rememberUpdatedState(onInteractionChange)
            var isInteracting by remember(asset) { mutableStateOf(false) }
            DisposableEffect(asset) {
                onDispose {
                    if (isInteracting) currentOnInteractionChange(false)
                }
            }
            val selectedColor = rememberPetColor(appearance.furColor)
            val controller = remember(asset) { PetSceneController(asset, engine) }
            var firstFrameRendered by remember(asset) { mutableStateOf(false) }
            var sceneHasPixels by remember(asset) { mutableStateOf(false) }
            var probeFailed by remember(asset) { mutableStateOf(false) }
            val currentOnSceneReady by rememberUpdatedState(onSceneReady)
            if (!sceneHasPixels && !probeFailed) {
                DisposableEffect(asset, surfaceMirrorer) {
                    val reader = runCatching {
                        ImageReader.newInstance(64, 64, PixelFormat.RGBA_8888, 2)
                    }.onFailure { error ->
                        Log.w("FinikPet", "Unable to inspect first 3D frame", error)
                        probeFailed = true
                    }.getOrNull()
                    val surface = reader?.surface
                    if (reader != null && surface != null) {
                        reader.setOnImageAvailableListener({ source ->
                            try {
                                source.acquireLatestImage()?.use { image ->
                                    if (!sceneHasPixels && image.hasVisiblePetPixels()) {
                                        sceneHasPixels = true
                                        currentOnSceneReady()
                                    }
                                }
                            } catch (error: Exception) {
                                Log.w("FinikPet", "Unable to inspect 3D frame", error)
                                probeFailed = true
                            }
                        }, Handler(Looper.getMainLooper()))
                        // A tiny second render is active only until the model becomes visible.
                        surfaceMirrorer.startMirroring(surface, width = 64, height = 64)
                    }
                    onDispose {
                        reader?.setOnImageAvailableListener(null, null)
                        if (surface != null) surfaceMirrorer.stopMirroring(surface)
                        reader?.close()
                    }
                }
            }
            val height = (asset.instance.model.boundingBox.halfExtent[1] * 2f).coerceAtLeast(.01f)
            val modelScale = (1.70f + stageIndex.coerceIn(0, 4) * .075f) * modelScaleMultiplier / height
            SceneView(
                modifier = Modifier.fillMaxSize(),
                engine = engine,
                modelLoader = modelLoader,
                materialLoader = materialLoader,
                environmentLoader = environmentLoader,
                // Embedded surfaces respect Compose clipping, scrolling and transparent cards.
                surfaceType = SurfaceType.TextureSurface,
                isOpaque = false,
                cameraNode = cameraNode,
                cameraManipulator = cameraManipulator,
                // TextureView must receive the complete gesture. Let its DOWN disable the
                // surrounding Compose scroll until UP/CANCEL; returning false keeps SceneView's
                // own orbit and pinch detectors in charge of the same MotionEvent stream.
                onTouchEvent = { event, _ ->
                    val active = when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> true
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> false
                        else -> isInteracting
                    }
                    if (active != isInteracting) {
                        isInteracting = active
                        currentOnInteractionChange(active)
                    }
                    false
                },
                autoCenterContent = false,
                frameRatePolicy = FrameRatePolicy.OnDemand(maxFps = 30),
                surfaceMirrorer = surfaceMirrorer,
                onFrame = { frameTimeNanos ->
                    controller.onFrame(frameTimeNanos)
                    if (!firstFrameRendered) {
                        firstFrameRendered = true
                    }
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
                SideEffect(onSceneFailure)
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

/** The mirror is black until the owl contributes pixels to the rendered scene. */
private fun Image.hasVisiblePetPixels(): Boolean {
    val plane = planes.firstOrNull() ?: return false
    val bytes = plane.buffer
    var visible = 0
    for (y in 8 until height - 8 step 2) {
        for (x in 8 until width - 8 step 2) {
            val index = y * plane.rowStride + x * plane.pixelStride
            if (index + 2 >= bytes.limit()) continue
            val red = bytes.get(index).toInt() and 0xff
            val green = bytes.get(index + 1).toInt() and 0xff
            val blue = bytes.get(index + 2).toInt() and 0xff
            if (maxOf(red, green, blue) > 24 && ++visible >= 8) return true
        }
    }
    return false
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
