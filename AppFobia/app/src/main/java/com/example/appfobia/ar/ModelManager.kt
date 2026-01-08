/*package com.example.appfobia.ar

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.example.appfobia.ExposureType

class ModelManager(private val context: Context) {

    companion object {
        private const val TAG = "ModelManager"
    }

    private val modelMap = mapOf(
        ExposureType.CLOSED_SPACES to "models/closed_room.glb",
        ExposureType.ROLLER_COASTER to "models/roller_coaster.glb",
        ExposureType.HEIGHTS to "models/roller_coaster.glb",
        ExposureType.CROWDS to "models/roller_coaster.glb"
    )

    private val loadedModels = mutableMapOf<String, ModelRenderable>()

    fun preloadModel(modelPath: String, callback: (Boolean) -> Unit) {
        if (loadedModels.containsKey(modelPath)) {
            callback(true)
            return
        }

        ModelRenderable.builder()
            .setSource(context, Uri.parse("file:///android_asset/$modelPath"))
            .build()
            .thenAccept { renderable ->
                loadedModels[modelPath] = renderable
                callback(true)
                Log.d(TAG, "Modelo pre-carregado: $modelPath")
            }
            .exceptionally { exception ->
                Log.e(TAG, "Erro ao pre-carregar modelo: $modelPath", exception)
                callback(false)
                null
            }
    }

    fun preloadAllModels(callback: (Boolean) -> Unit) {
        val paths = modelMap.values.distinct()
        var loadedCount = 0

        if (paths.isEmpty()) {
            callback(true)
            return
        }

        paths.forEach { path ->
            preloadModel(path) { success ->
                if (success) loadedCount++
                if (loadedCount == paths.size) {
                    callback(true)
                    Log.d(TAG, "Todos os modelos pre-carregados!")
                }
            }
        }
    }

    fun loadModelForExposure(
        exposureType: ExposureType,
        anchor: Anchor,
        scale: Float = 1f,
        onSuccess: (AnchorNode) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val modelPath = modelMap[exposureType]

        if (modelPath == null) {
            onError(Exception("Modelo nao encontrado para: $exposureType"))
            return
        }

        loadModel(
            modelPath = modelPath,
            anchor = anchor,
            scale = scale,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun loadModel(
        modelPath: String,
        anchor: Anchor,
        scale: Float = 1f,
        onSuccess: (AnchorNode) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val renderable = loadedModels[modelPath]

        if (renderable != null) {
            val anchorNode = createAnchorNode(anchor, renderable, scale)
            onSuccess(anchorNode)
            Log.d(TAG, "Modelo carregado (cache): $modelPath")
        } else {
            ModelRenderable.builder()
                .setSource(context, Uri.parse("file:///android_asset/$modelPath"))
                .build()
                .thenAccept { renderable ->
                    loadedModels[modelPath] = renderable
                    val anchorNode = createAnchorNode(anchor, renderable, scale)
                    onSuccess(anchorNode)
                    Log.d(TAG, "Modelo carregado (novo): $modelPath")
                }
                .exceptionally { exception ->
                    Log.e(TAG, "Erro ao carregar modelo: $modelPath", exception)
                    onError(exception as Exception)
                    null
                }
        }
    }

    private fun createAnchorNode(
        anchor: Anchor,
        renderable: ModelRenderable,
        scale: Float
    ): AnchorNode {
        return AnchorNode(anchor).apply {
            this.renderable = renderable
            localScale = Vector3(scale, scale, scale)
        }
    }

    fun clearCache() {
        loadedModels.clear()
        Log.d(TAG, "Cache de modelos limpo")
    }

    fun removeFromCache(modelPath: String) {
        loadedModels.remove(modelPath)
        Log.d(TAG, "Modelo removido do cache: $modelPath")
    }

    fun getModelPath(exposureType: ExposureType): String? {
        return modelMap[exposureType]
    }
}*/