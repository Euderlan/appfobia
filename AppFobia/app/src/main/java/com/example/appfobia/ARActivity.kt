package com.example.appfobia

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textview.MaterialTextView

class ARActivity : AppCompatActivity() {

    private var isRunning = false
    private var intensity: Int = 5
    private var exposureType: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar)

        intensity = intent.getIntExtra("intensity", 5)
        exposureType = intent.getStringExtra("exposure_type") ?: ""

        val tvStatus = findViewById<MaterialTextView>(R.id.tv_ar_status)
        val tvIntensity = findViewById<MaterialTextView>(R.id.tv_current_intensity)
        val tvTimer = findViewById<MaterialTextView>(R.id.tv_timer)
        val btnPlay = findViewById<FloatingActionButton>(R.id.fab_play_pause)
        val btnIncrease = findViewById<MaterialButton>(R.id.btn_increase_intensity)
        val btnDecrease = findViewById<MaterialButton>(R.id.btn_decrease_intensity)
        val btnEnd = findViewById<MaterialButton>(R.id.btn_end_session)

        tvStatus.text = "Preparando exposição..."
        tvIntensity.text = "Intensidade: $intensity/10"

        // TODO: Inicializar ARCore aqui

        btnPlay.setOnClickListener {
            isRunning = !isRunning
            tvStatus.text = if (isRunning) "Em andamento..." else "Pausado"
            btnPlay.setImageResource(
                if (isRunning) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
            )
        }

        btnIncrease.setOnClickListener {
            if (intensity < 10) {
                intensity++
                tvIntensity.text = "Intensidade: $intensity/10"
            }
        }

        btnDecrease.setOnClickListener {
            if (intensity > 1) {
                intensity--
                tvIntensity.text = "Intensidade: $intensity/10"
            }
        }

        btnEnd.setOnClickListener {
            // Salvar dados de sessão
            finishSession()
        }
    }

    private fun finishSession() {
        // TODO: Salvar progresso
        finish()
    }

    override fun onResume() {
        super.onResume()
        // TODO: Retomar ARCore session
    }

    override fun onPause() {
        super.onPause()
        // TODO: Pausar ARCore session
    }
}