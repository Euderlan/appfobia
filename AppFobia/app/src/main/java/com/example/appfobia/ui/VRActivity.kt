package com.example.appfobia.ui

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textview.MaterialTextView
import com.example.appfobia.ExposureType
import com.example.appfobia.R
import com.example.appfobia.ar.SceneManager
import com.example.appfobia.ar.SimpleARRenderer

class VRActivity : AppCompatActivity() {

    private var isRunning = false
    private var intensity: Int = 5
    private var exposureType: String = ""
    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var renderer: SimpleARRenderer
    private lateinit var sceneManager: SceneManager
    private var elapsedSeconds = 0
    private var sessionTimer: Thread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar) // Usar o mesmo layout de AR

        try {
            intensity = intent.getIntExtra("intensity", 5)
            exposureType = intent.getStringExtra("exposure_type") ?: ""

            Log.d("VRActivity", "Iniciando VR (modo simulação): $exposureType, intensidade: $intensity")

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
            arContainer.addView(glSurfaceView, 0)

            sceneManager = SceneManager(this)

            tvStatus.text = "🥽 Modo VR (Simulação)"
            tvIntensity.text = "Intensidade: $intensity/10"

            // Configurar renderer
            try {
                val exposureEnum = ExposureType.valueOf(exposureType)
                renderer.setExposureType(exposureEnum.name)
                renderer.setIntensity(intensity)
                tvStatus.text = "🥽 VR - ${getExposureTypeName()}"
            } catch (e: Exception) {
                e.printStackTrace()
            }

            btnPlay.setOnClickListener {
                isRunning = !isRunning
                tvStatus.text = if (isRunning) {
                    startTimer(tvTimer)
                    "▶️ Em andamento - ${getExposureTypeName()}"
                } else {
                    stopTimer()
                    "⏸️ Pausado"
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

            // Mostrar descrição
            val exposureTypeEnum = ExposureType.valueOf(exposureType)
            tvStatus.text = "🥽 VR - ${sceneManager.getSceneDescription(exposureTypeEnum).take(30)}..."

        } catch (e: Exception) {
            Log.e("VRActivity", "Erro ao criar VRActivity", e)
            e.printStackTrace()
            finish()
        }
    }

    private fun getExposureTypeName(): String {
        return when (exposureType) {
            "ROLLER_COASTER" -> "🎢 Roller Coaster"
            "HEIGHTS" -> "⛰️ Alturas"
            "CLOSED_SPACES" -> "🚪 Espaços Fechados"
            "CROWDS" -> "👥 Multidões"
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
        finish()
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
    }
}