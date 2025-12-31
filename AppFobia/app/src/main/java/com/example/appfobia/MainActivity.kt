package com.example.therapyra

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Referências aos elementos
        val btnRollerCoaster = findViewById<MaterialButton>(R.id.btn_roller_coaster)
        val btnHeights = findViewById<MaterialButton>(R.id.btn_heights)
        val btnClosedSpaces = findViewById<MaterialButton>(R.id.btn_closed_spaces)
        val btnCrowds = findViewById<MaterialButton>(R.id.btn_crowds)

        // Cliques nos botões
        btnRollerCoaster.setOnClickListener {
            startARExposure("Roller Coaster", ExposureType.ROLLER_COASTER)
        }

        btnHeights.setOnClickListener {
            startARExposure("Alturas", ExposureType.HEIGHTS)
        }

        btnClosedSpaces.setOnClickListener {
            startARExposure("Espaços Fechados", ExposureType.CLOSED_SPACES)
        }

        btnCrowds.setOnClickListener {
            startARExposure("Multidões", ExposureType.CROWDS)
        }
    }

    private fun startARExposure(title: String, type: ExposureType) {
        val intent = Intent(this, ConfigurationActivity::class.java).apply {
            putExtra("exposure_title", title)
            putExtra("exposure_type", type.name)
        }
        startActivity(intent)
    }
}

enum class ExposureType {
    ROLLER_COASTER,
    HEIGHTS,
    CLOSED_SPACES,
    CROWDS
}