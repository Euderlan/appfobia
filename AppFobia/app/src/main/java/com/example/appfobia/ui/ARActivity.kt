package com.example.appfobia.ui

import android.Manifest
import android.content.pm.PackageManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textview.MaterialTextView
import com.example.appfobia.ExposureType
import com.example.appfobia.R
import com.example.appfobia.ar.ARCoreManager
import com.example.appfobia.ar.SceneManager
import com.example.appfobia.ar.SimpleARRenderer

class ARActivity : AppCompatActivity() {

    private var isRunning = false
    private var intensity: Int = 5
    private var exposureType: String = ""
    private lateinit var arCoreManager: ARCoreManager
    private lateinit var sceneManager: SceneManager
    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var renderer: SimpleARRenderer
    private val CAMERA_PERMISSION_CODE = 100
    private var elapsedSeconds = 0
    private var sessionTimer: Thread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar)

        try {
            intensity = intent.getIntExtra("intensity", 5)
            exposureType = intent.getStringExtra("exposure_type") ?: ""

            val tvStatus = findViewById<MaterialTextView>(R.id.tv_ar_status)
            val tvIntensity = findViewById<MaterialTextView>(R.id.tv_current_intensity)
            val tvTimer = findViewById<MaterialTextView>(R.id.tv_timer)
            val btnPlay = findViewById<FloatingActionButton>(R.id.fab_play_pause)
            val btnIncrease = findViewById<MaterialButton>(R.id.btn_increase_intensity)
            val btnDecrease = findViewById<MaterialButton>(R.id.btn_decrease_intensity)
            val btnEnd = findViewById<MaterialButton>(R.id.btn_end_session)
            val arContainer = findViewById<android.widget.FrameLayout>(R.id.ar_container)

            // Criar GLSurfaceView para renderização
            glSurfaceView = GLSurfaceView(this)
            renderer = SimpleARRenderer()
            glSurfaceView.setRenderer(renderer)
            glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
            arContainer.addView(glSurfaceView, 0) // Adicionar como primeira view

            // Inicializar managers
            arCoreManager = ARCoreManager(this)
            sceneManager = SceneManager(this)

            tvStatus.text = "Preparando exposição..."
            tvIntensity.text = "Intensidade: $intensity/10"

            // Configurar renderer com tipo de exposição
            try {
                val exposureEnum = ExposureType.valueOf(exposureType)
                renderer.setExposureType(exposureEnum.name)
                renderer.setIntensity(intensity)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Verificar permissão de câmera
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_CODE
                )
            } else {
                initializeAR(tvStatus)
            }

            btnPlay.setOnClickListener {
                isRunning = !isRunning
                tvStatus.text = if (isRunning) {
                    startTimer(tvTimer)
                    "Em andamento - ${getExposureTypeName()}"
                } else {
                    stopTimer()
                    "Pausado"
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

            // Mostrar descrição da cena
            val exposureTypeEnum = ExposureType.valueOf(exposureType)
            tvStatus.text = sceneManager.getSceneDescription(exposureTypeEnum)

        } catch (e: Exception) {
            e.printStackTrace()
            finish()
        }
    }

    private fun initializeAR(tvStatus: MaterialTextView) {
        if (arCoreManager.isARCoreAvailable()) {
            if (arCoreManager.initializeARCore()) {
                tvStatus.text = "ARCore iniciado - ${getExposureTypeName()}"
            } else {
                tvStatus.text = "Aguardando ARCore..."
            }
        } else {
            tvStatus.text = "Dispositivo sem suporte ARCore. Modo simulação ativado."
        }
    }

    private fun getExposureTypeName(): String {
        return when (exposureType) {
            "ROLLER_COASTER" -> "Roller Coaster"
            "HEIGHTS" -> "Alturas"
            "CLOSED_SPACES" -> "Espaços Fechados"
            "CROWDS" -> "Multidões"
            else -> "Exposição"
        }
    }

    private fun startTimer(tvTimer: MaterialTextView) {
        stopTimer()
        elapsedSeconds = 0
        sessionTimer = Thread {
            while (isRunning && !Thread.currentThread().isInterrupted) {
                Thread.sleep(1000)
                elapsedSeconds++
                val minutes = elapsedSeconds / 60
                val seconds = elapsedSeconds % 60
                runOnUiThread {
                    tvTimer.text = String.format("Tempo: %02d:%02d", minutes, seconds)
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
        arCoreManager.close()
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
                initializeAR(findViewById(R.id.tv_ar_status))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
        arCoreManager.resume()
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause()
        arCoreManager.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
        arCoreManager.close()
    }
}