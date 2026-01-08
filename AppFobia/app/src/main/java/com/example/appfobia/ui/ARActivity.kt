package com.example.appfobia.ui

import android.Manifest
import android.content.pm.PackageManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.filament.Engine
import com.google.android.filament.gltfio.AssetLoader
import com.google.android.filament.gltfio.ResourceLoader
import com.example.appfobia.ExposureType
import com.example.appfobia.R
import java.nio.ByteBuffer
import com.google.android.filament.EntityManager
import com.google.android.filament.gltfio.MaterialProvider

class ARActivity : AppCompatActivity(), GLSurfaceView.Renderer {

    companion object {
        private const val TAG = "ARActivity"
    }

    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var engine: Engine
    private var exposureType: ExposureType = ExposureType.ROLLER_COASTER
    private var intensity: Int = 5

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d(TAG, "Permissão de câmera concedida")
        } else {
            Log.e(TAG, "Permissão de câmera negada")
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar)

        Log.d(TAG, "onCreate ARActivity iniciado")

        // Obter dados da Intent
        @Suppress("DEPRECATION")
        exposureType = intent.getSerializableExtra("exposure_type") as? ExposureType ?: ExposureType.ROLLER_COASTER
        intensity = intent.getIntExtra("intensity", 5)

        // Verificar permissão de câmera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        // Configurar GLSurfaceView
        glSurfaceView = findViewById(R.id.ar_surface_view)
        glSurfaceView.setEGLContextClientVersion(3)
        glSurfaceView.setRenderer(this)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

        // Configurar UI
        setupUI()
    }

    override fun onSurfaceCreated(gl: javax.microedition.khronos.opengles.GL10?, config: javax.microedition.khronos.egl.EGLConfig?) {
        Log.d(TAG, "onSurfaceCreated")
        try {
            engine = Engine.create()
            loadModel()
            Log.d(TAG, "Engine e modelo inicializados")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao inicializar", e)
        }
    }

    override fun onSurfaceChanged(gl: javax.microedition.khronos.opengles.GL10?, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceChanged: $width x $height")
    }

    override fun onDrawFrame(gl: javax.microedition.khronos.opengles.GL10?) {
        // Frame rendering
    }

    private fun loadModel() {
        Thread {
            try {
                val modelPath = "models/roller_coaster.glb"

                // Ler arquivo GLB dos assets
                val inputStream = assets.open(modelPath)
                val buffer = inputStream.readBytes()
                inputStream.close()

                Log.d(TAG, "Arquivo GLB lido: ${buffer.size} bytes")

                val entityManager = EntityManager.get()
                val assetLoader = AssetLoader(engine, null, entityManager)
                val asset = assetLoader.createAsset(ByteBuffer.wrap(buffer))

                if (asset != null) {
                    Log.d(TAG, "Modelo carregado com sucesso!")
                } else {
                    Log.e(TAG, "Falha ao carregar asset")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao carregar modelo", e)
            }
        }.start()
    }

    private fun setupUI() {
        val btnVoltar = findViewById<Button>(R.id.btn_back)
        btnVoltar.setOnClickListener {
            finishSession()
        }
    }

    private fun finishSession() {
        Log.d(TAG, "Finalizando sessão AR")
        try {
            if (::engine.isInitialized) {
                engine.destroy()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao limpar recursos", e)
        }
        finish()
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
    }

    override fun onPause() {
        glSurfaceView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        finishSession()
        super.onDestroy()
    }
}