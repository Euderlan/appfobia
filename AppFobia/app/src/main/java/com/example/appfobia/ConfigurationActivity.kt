package com.example.appfobia

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.google.android.material.textview.MaterialTextView
import com.example.appfobia.ui.ARActivity

class ConfigurationActivity : AppCompatActivity() {

    private var currentIntensity: Float = 5f

    companion object {
        private const val TAG = "ConfigurationActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate iniciado")

        try {
            setContentView(R.layout.activity_configuration)
            Log.d(TAG, "Layout carregado")

            val exposureTitle = intent.getStringExtra("exposure_title") ?: "Exposicao"
            val exposureType = intent.getStringExtra("exposure_type") ?: ""

            Log.d(TAG, "Parametros: title=$exposureTitle, type=$exposureType")

            val tvTitle = findViewById<MaterialTextView>(R.id.tv_config_title)
            val sliderIntensity = findViewById<Slider>(R.id.slider_intensity)
            val tvIntensityValue = findViewById<MaterialTextView>(R.id.tv_intensity_value)
            val btnStart = findViewById<MaterialButton>(R.id.btn_start_ar)
            val btnBack = findViewById<MaterialButton>(R.id.btn_back)

            Log.d(TAG, "Views encontradas")

            tvTitle.text = "Configurar: $exposureTitle"

            sliderIntensity.apply {
                valueFrom = 1f
                valueTo = 10f
                value = currentIntensity
                stepSize = 1f

                addOnChangeListener { _, value, _ ->
                    currentIntensity = value
                    tvIntensityValue.text = "Intensidade: ${value.toInt()}/10"
                }
            }

            btnStart.setOnClickListener {
                Log.d(TAG, "Iniciando sessao AR com intensidade: ${currentIntensity.toInt()}")
                startARSession(exposureType, currentIntensity.toInt())
            }

            btnBack.setOnClickListener {
                Log.d(TAG, "Clicou em Back")
                finish()
            }

            Log.d(TAG, "ConfigurationActivity inicializada com sucesso")

        } catch (e: Exception) {
            Log.e(TAG, "Erro fatal no onCreate", e)
            e.printStackTrace()
            finish()
        }
    }

    private fun startARSession(exposureType: String, intensity: Int) {
        try {
            Log.d(TAG, "Iniciando sessao RA com ARCore: $exposureType, intensidade: $intensity")
            val intent = Intent(this, ARActivity::class.java).apply {
                putExtra("exposure_type", exposureType)
                putExtra("intensity", intensity)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao iniciar ARActivity", e)
            e.printStackTrace()
        }
    }
}