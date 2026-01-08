package com.example.appfobia

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.appfobia.ui.ARActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate iniciado")

        try {
            setContentView(R.layout.activity_main)
            Log.d(TAG, "Layout carregado com sucesso")

            val btnRollerCoaster = findViewById<MaterialButton>(R.id.btn_roller_coaster)
            val btnHeights = findViewById<MaterialButton>(R.id.btn_heights)
            val btnClosedSpaces = findViewById<MaterialButton>(R.id.btn_closed_spaces)
            val btnCrowds = findViewById<MaterialButton>(R.id.btn_crowds)

            Log.d(TAG, "Todos os botoes encontrados")

            btnRollerCoaster.setOnClickListener {
                Log.d(TAG, "Clicou em Roller Coaster")
                startARExposure("Roller Coaster", ExposureType.ROLLER_COASTER)
            }

            btnHeights.setOnClickListener {
                Log.d(TAG, "Clicou em Heights")
                startARExposure("Alturas", ExposureType.HEIGHTS)
            }

            btnClosedSpaces.setOnClickListener {
                Log.d(TAG, "Clicou em Closed Spaces")
                startARExposure("Espacos Fechados", ExposureType.CLOSED_SPACES)
            }

            btnCrowds.setOnClickListener {
                Log.d(TAG, "Clicou em Crowds")
                startARExposure("Multidoes", ExposureType.CROWDS)
            }

            Log.d(TAG, "MainActivity inicializada com sucesso")

        } catch (e: Exception) {
            Log.e(TAG, "Erro fatal no onCreate", e)
            e.printStackTrace()
            finish()
        }
    }

    private fun startARExposure(title: String, type: ExposureType) {
        try {
            val intent = Intent(this, ARActivity::class.java).apply {
                putExtra("exposure_type", type.name)
                putExtra("intensity", 5)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao iniciar ARActivity", e)
            e.printStackTrace()
        }
    }
}