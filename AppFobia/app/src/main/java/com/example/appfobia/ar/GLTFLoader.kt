/*package com.example.appfobia.ar

import android.content.Context
import android.util.Log
import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.gltfio.AssetLoader
import com.google.android.filament.gltfio.FilamentAsset
import com.google.android.filament.gltfio.ResourceLoader
import com.google.android.filament.gltfio.UbershaderProvider
import java.nio.ByteBuffer

class GLTFLoader(engine: Engine) {

    companion object {
        private const val TAG = "GLTFLoader"
    }

    private val assetLoader: AssetLoader
    private val resourceLoader: ResourceLoader

    init {
        val entityManager = EntityManager.get()
        val provider = UbershaderProvider(engine)
        assetLoader = AssetLoader(engine, provider, entityManager)
        resourceLoader = ResourceLoader(engine)
    }

    fun loadGLTFAsset(context: Context, modelPath: String): FilamentAsset? {
        return try {
            val assetManager = context.assets
            val inputStream = assetManager.open(modelPath)
            val bytes = inputStream.readBytes()
            inputStream.close()

            val buffer = ByteBuffer.allocateDirect(bytes.size)
            buffer.put(bytes)
            buffer.flip()

            val asset = assetLoader.createAsset(buffer)

            if (asset != null) {
                resourceLoader.loadResources(asset)
                Log.d(TAG, "Modelo carregado com sucesso: $modelPath")
            } else {
                Log.e(TAG, "Falha ao carregar modelo: $modelPath")
            }

            asset
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao carregar GLTF: $modelPath", e)
            e.printStackTrace()
            null
        }
    }

    fun release() {
        assetLoader.destroy()
        resourceLoader.destroy()
    }
}*/