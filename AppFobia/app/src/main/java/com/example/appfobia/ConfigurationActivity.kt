package com.example.appfobia

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.google.android.material.textview.MaterialTextView

class ConfigurationActivity : AppCompatActivity() {

    private var currentIntensity: Float = 5f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuration)

        try {
            val exposureTitle = intent.getStringExtra("exposure_title") ?: "Exposição"
            val exposureType = intent.getStringExtra("exposure_type") ?: ""

            val tvTitle = findViewById<MaterialTextView>(R.id.tv_config_title)
            val sliderIntensity = findViewById<Slider>(R.id.slider_intensity)
            val tvIntensityValue = findViewById<MaterialTextView>(R.id.tv_intensity_value)
            val btnStart = findViewById<MaterialButton>(R.id.btn_start_ar)
            val btnBack = findViewById<MaterialButton>(R.id.btn_back)

            tvTitle.text = "Configurar: $exposureTitle"

            // Slider de intensidade (1-10)
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
                startARSession(exposureType, currentIntensity.toInt())
            }

            btnBack.setOnClickListener {
                finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            finish()
        }
    }

    private fun startARSession(exposureType: String, intensity: Int) {
        val intent = Intent(this, ARActivity::class.java).apply {
            putExtra("exposure_type", exposureType)
            putExtra("intensity", intensity)
        }
        startActivity(intent)
    }
}