package com.example.appfobia.ar

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraProvider(private val context: Context, private val lifecycleOwner: LifecycleOwner) {

    private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    companion object {
        private const val TAG = "CameraProvider"
    }

    fun startCamera(previewSurfaceProvider: Preview.SurfaceProvider) {
        cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture?.addListener({
            try {
                val cameraProvider = cameraProviderFuture?.get()

                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewSurfaceProvider)
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview
                )

                Log.d(TAG, "Camera iniciada com sucesso")
            } catch (exc: Exception) {
                Log.e(TAG, "Erro ao iniciar câmera", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun stopCamera() {
        try {
            ProcessCameraProvider.getInstance(context).get().unbindAll()
            Log.d(TAG, "Camera parada")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao parar câmera", e)
        }
    }

    fun shutdown() {
        cameraExecutor.shutdown()
    }
}