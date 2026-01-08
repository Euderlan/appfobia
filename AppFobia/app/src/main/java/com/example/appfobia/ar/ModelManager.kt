package com.example.appfobia.ar

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
        ExposureType.ROLLER_COASTER to "models/closed_room.glb",
        ExposureType.HEIGHTS to "models/closed_room.glb",
        ExposureType.CROWDS to "models/closed_room.glb"
    )

    private val loadedModels = mutableMapOf<String, ModelRenderable>()

    /**
     * Pré-carrega um modelo 3D para uso rápido depois
     */
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
                Log.d(TAG, "Modelo pré-carregado: $modelPath")
            }
            .exceptionally { exception ->
                Log.e(TAG, "Erro ao pré-carregar modelo: $modelPath", exception)
                callback(false)
                null
            }
    }

    /**
     * Pré-carrega todos os modelos de exposição
     */
    fun preloadAllModels(callback: (Boolean) -> Unit) {
        val paths = modelMap.values.toList()
        var loadedCount = 0

        paths.forEach { path ->
            preloadModel(path) { success ->
                if (success) loadedCount++
                if (loadedCount == paths.size) {
                    callback(true)
                    Log.d(TAG, "Todos os modelos pré-carregados!")
                }
            }
        }
    }

    /**
     * Carrega o modelo correspondente ao tipo de exposição
     */
    fun loadModelForExposure(
        exposureType: ExposureType,
        anchor: Anchor,
        scale: Float = 1f,
        onSuccess: (AnchorNode) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val modelPath = modelMap[exposureType]

        if (modelPath == null) {
            onError(Exception("Modelo não encontrado para: $exposureType"))
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

    /**
     * Carrega qualquer modelo 3D manualmente
     */
    fun loadModel(
        modelPath: String,
        anchor: Anchor,
        scale: Float = 1f,
        onSuccess: (AnchorNode) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val renderable = loadedModels[modelPath]

        if (renderable != null) {
            // Modelo já está pré-carregado
            val anchorNode = createAnchorNode(anchor, renderable, scale)
            onSuccess(anchorNode)
            Log.d(TAG, "Modelo carregado (cache): $modelPath")
        } else {
            // Carregar modelo pela primeira vez
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

    /**
     * Cria um AnchorNode com o modelo renderizado
     */
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

    /**
     * Limpa o cache de modelos carregados
     */
    fun clearCache() {
        loadedModels.clear()
        Log.d(TAG, "Cache de modelos limpo")
    }

    /**
     * Remove um modelo específico do cache
     */
    fun removeFromCache(modelPath: String) {
        loadedModels.remove(modelPath)
        Log.d(TAG, "Modelo removido do cache: $modelPath")
    }

    /**
     * Retorna o caminho do modelo para um tipo de exposição
     */
    fun getModelPath(exposureType: ExposureType): String? {
        return modelMap[exposureType]
    }
}