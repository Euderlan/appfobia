package com.example.appfobia.ui

import android.Manifest
import android.content.pm.PackageManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textview.MaterialTextView
import com.example.appfobia.ExposureType
import com.example.appfobia.R
import com.example.appfobia.ar.ARCoreRenderer
import com.example.appfobia.ar.ARCoreSessionManager
import com.example.appfobia.ar.SceneManager

class ARActivity : AppCompatActivity() {

    private var isRunning = false
    private var intensity: Int = 5
    private var exposureType: String = ""
    private var elapsedSeconds = 0
    private var sessionTimer: Thread? = null

    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var renderer: ARCoreRenderer
    private lateinit var arCoreManager: ARCoreSessionManager
    private lateinit var sceneManager: SceneManager

    private val CAMERA_PERMISSION_CODE = 100

    companion object {
        private const val TAG = "ARActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate ARActivity iniciado")

        try {
            setContentView(R.layout.activity_ar)
            Log.d(TAG, "Layout carregado")

            intensity = intent.getIntExtra("intensity", 5)
            exposureType = intent.getStringExtra("exposure_type") ?: "ROLLER_COASTER"

            Log.d(TAG, "Parametros: exposure=$exposureType, intensity=$intensity")

            val tvStatus = findViewById<MaterialTextView>(R.id.tv_ar_status)
            val tvIntensity = findViewById<MaterialTextView>(R.id.tv_current_intensity)
            val tvTimer = findViewById<MaterialTextView>(R.id.tv_timer)
            val btnPlay = findViewById<FloatingActionButton>(R.id.fab_play_pause)
            val btnIncrease = findViewById<MaterialButton>(R.id.btn_increase_intensity)
            val btnDecrease = findViewById<MaterialButton>(R.id.btn_decrease_intensity)
            val btnEnd = findViewById<MaterialButton>(R.id.btn_end_session)
            val arContainer = findViewById<FrameLayout>(R.id.ar_container)

            tvStatus.text = "Inicializando ARCore..."
            tvIntensity.text = "Intensidade: $intensity/10"

            arCoreManager = ARCoreSessionManager(this)
            sceneManager = SceneManager(this)

            glSurfaceView = GLSurfaceView(this)
            renderer = ARCoreRenderer()
            glSurfaceView.setRenderer(renderer)
            glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
            arContainer.addView(glSurfaceView, 0)

            Log.d(TAG, "GLSurfaceView adicionada")

            try {
                val exposureEnum = ExposureType.valueOf(exposureType)
                renderer.setExposureType(exposureEnum.name)
                renderer.setIntensity(intensity)
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao definir tipo de exposicao", e)
            }

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "Pedindo permissao de camera")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_CODE
                )
            } else {
                initializeARCore(tvStatus)
            }

            btnPlay.setOnClickListener {
                isRunning = !isRunning
                if (isRunning) {
                    startTimer(tvTimer)
                    tvStatus.text = "Ativo - ${getExposureTypeName()}"
                } else {
                    stopTimer()
                    tvStatus.text = "Pausado"
                }
                btnPlay.setImageResource(
                    if (isRunning) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
                )
            }

            btnIncrease.setOnClickListener {
                if (intensity < 10) {
                    intensity++
                    tvIntensity.text = "Intensidade: $intensity/10"
                    renderer.setIntensity(intensity)
                }
            }

            btnDecrease.setOnClickListener {
                if (intensity > 1) {
                    intensity--
                    tvIntensity.text = "Intensidade: $intensity/10"
                    renderer.setIntensity(intensity)
                }
            }

            btnEnd.setOnClickListener {
                stopTimer()
                finishSession()
            }

            try {
                val exposureTypeEnum = ExposureType.valueOf(exposureType)
                tvStatus.text = "ARCore - ${sceneManager.getSceneDescription(exposureTypeEnum).take(40)}"
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao carregar descricao", e)
            }

            Log.d(TAG, "ARActivity inicializada com sucesso")

        } catch (e: Exception) {
            Log.e(TAG, "Erro fatal no onCreate", e)
            e.printStackTrace()
            finish()
        }
    }

    private fun initializeARCore(tvStatus: MaterialTextView) {
        Log.d(TAG, "Inicializando ARCore")

        if (arCoreManager.checkARCoreSupport()) {
            if (arCoreManager.initializeSession()) {
                arCoreManager.getSession()?.let { session ->
                    renderer.setARSession(session)
                    tvStatus.text = "ARCore Ativo - ${getExposureTypeName()}"
                    Log.d(TAG, "ARCore inicializado com sucesso")
                }
            } else {
                tvStatus.text = "Erro ao inicializar ARCore"
                Log.e(TAG, "Falha ao inicializar sessao ARCore")
            }
        } else {
            tvStatus.text = "ARCore nao suportado - Modo Simulacao"
            Log.w(TAG, "Dispositivo nao suporta ARCore")
        }
    }

    private fun getExposureTypeName(): String {
        return when (exposureType) {
            "ROLLER_COASTER" -> "Roller Coaster"
            "HEIGHTS" -> "Alturas"
            "CLOSED_SPACES" -> "Espacos Fechados"
            "CROWDS" -> "Multidoes"
            else -> "Exposicao Padrao"
        }
    }

    private fun startTimer(tvTimer: MaterialTextView) {
        stopTimer()
        elapsedSeconds = 0
        sessionTimer = Thread {
            while (isRunning && !Thread.currentThread().isInterrupted) {
                try {
                    Thread.sleep(1000)
                    elapsedSeconds++
                    val minutes = elapsedSeconds / 60
                    val seconds = elapsedSeconds % 60
                    runOnUiThread {
                        tvTimer.text = String.format("Tempo: %02d:%02d", minutes, seconds)
                    }
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    break
                }
            }
        }
        sessionTimer?.start()
    }

    private fun stopTimer() {
        sessionTimer?.interrupt()
        sessionTimer = null
    }

    private fun finishSession() {
        stopTimer()
        arCoreManager.pauseSession()
        arCoreManager.closeSession()
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                arCoreManager.setCameraPermissionGranted(true)
                initializeARCore(findViewById(R.id.tv_ar_status))
            } else {
                Log.w(TAG, "Permissao de camera negada")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        glSurfaceView.onResume()
        arCoreManager.resumeSession()
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        glSurfaceView.onPause()
        arCoreManager.pauseSession()
        super.onPause()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        stopTimer()
        arCoreManager.closeSession()
        super.onDestroy()
    }
}