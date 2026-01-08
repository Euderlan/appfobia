package com.example.appfobia.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.appfobia.ExposureType
import com.example.appfobia.R
import com.example.appfobia.ar.ModelManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textview.MaterialTextView
import com.google.ar.core.Pose
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

class ARActivity : AppCompatActivity() {

    private var isRunning = false
    private var intensity: Int = 5
    private var exposureType: String = ""
    private var elapsedSeconds = 0
    private var sessionTimer: Thread? = null

    private val CAMERA_PERMISSION_CODE = 100

    private lateinit var arFragment: ArFragment
    private lateinit var modelManager: ModelManager

    private var placedNode: TransformableNode? = null
    private var placedAnchorNode: AnchorNode? = null

    companion object {
        private const val TAG = "ARActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate ARActivity iniciado")
        setContentView(R.layout.activity_ar)

        // Fragmento AR (precisa existir no activity_ar.xml como R.id.ux_fragment)
        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment

        intensity = intent.getIntExtra("intensity", 5)
        exposureType = intent.getStringExtra("exposure_type") ?: "ROLLER_COASTER"

        val tvStatus = findViewById<MaterialTextView>(R.id.tv_ar_status)
        val tvIntensity = findViewById<MaterialTextView>(R.id.tv_current_intensity)
        val tvTimer = findViewById<MaterialTextView>(R.id.tv_timer)

        val btnPlay = findViewById<FloatingActionButton>(R.id.fab_play_pause)
        val btnIncrease = findViewById<MaterialButton>(R.id.btn_increase_intensity)
        val btnDecrease = findViewById<MaterialButton>(R.id.btn_decrease_intensity)
        val btnEnd = findViewById<MaterialButton>(R.id.btn_end_session)

        tvStatus.text = "Inicializando AR..."
        tvIntensity.text = "Intensidade: $intensity/10"

        modelManager = ModelManager(this)

        // Permissão de câmera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
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
            if (isRunning) {
                startTimer(tvTimer)
                tvStatus.text = "Ativo - ${getExposureTypeName()}"
            } else {
                stopTimer()
                tvStatus.text = "Pausado"
            }
            btnPlay.setImageResource(
                if (isRunning) android.R.drawable.ic_media_pause
                else android.R.drawable.ic_media_play
            )
        }

        btnIncrease.setOnClickListener {
            if (intensity < 10) {
                intensity++
                tvIntensity.text = "Intensidade: $intensity/10"
                // Se você quiser, dá para usar intensidade para alterar algo no app (efeitos, UI, etc.)
            }
        }

        btnDecrease.setOnClickListener {
            if (intensity > 1) {
                intensity--
                tvIntensity.text = "Intensidade: $intensity/10"
            }
        }

        btnEnd.setOnClickListener {
            stopTimer()
            finishSession()
        }
    }

    private fun initializeAR(tvStatus: MaterialTextView) {
        Log.d(TAG, "initializeAR")

        // Pré-carrega modelos (opcional, mas bom)
        modelManager.preloadAllModels { success ->
            if (success) Log.d(TAG, "Modelos pré-carregados com sucesso")
            else Log.w(TAG, "Alguns modelos falharam no preload (vai tentar carregar mesmo assim)")

            // Coloca o modelo automaticamente na frente da câmera
            placeModelInFrontOfCamera(tvStatus)
        }
    }

    /**
     * Cria um anchor ~1m à frente da câmera e adiciona o modelo na Scene do Sceneform.
     * Isso é o que faz o .glb/.gltf aparecer para o usuário.
     */
    private fun placeModelInFrontOfCamera(tvStatus: MaterialTextView) {
        val exposureEnum = runCatching { ExposureType.valueOf(exposureType) }
            .getOrElse {
                tvStatus.text = "Tipo de exposição inválido: $exposureType"
                return
            }

        val session = arFragment.arSceneView.session
        val frame = arFragment.arSceneView.arFrame

        if (session == null || frame == null) {
            tvStatus.text = "Aguardando câmera/AR iniciar..."
            Log.w(TAG, "Session ou Frame nulos (ainda não pronto)")
            // Tenta de novo no próximo frame
            arFragment.arSceneView.scene.addOnUpdateListener {
                if (arFragment.arSceneView.session != null && arFragment.arSceneView.arFrame != null) {
                    arFragment.arSceneView.scene.removeOnUpdateListener { }
                    placeModelInFrontOfCamera(tvStatus)
                }
            }
            return
        }

        // Remove modelo anterior se existir
        placedNode?.setParent(null)
        placedNode = null
        placedAnchorNode?.let { old ->
            try {
                old.anchor?.detach()
            } catch (_: Exception) {}
            old.setParent(null)
        }
        placedAnchorNode = null

        // Anchor 1 metro à frente da câmera
        val cameraPose = frame.camera.pose
        val forwardPose: Pose = cameraPose.compose(Pose.makeTranslation(0f, 0f, -1.0f))
        val anchor = session.createAnchor(forwardPose)

        // Escala por tipo
        val scale = when (exposureEnum) {
            ExposureType.CLOSED_SPACES -> 1.2f
            ExposureType.HEIGHTS -> 1.5f
            ExposureType.ROLLER_COASTER -> 1.0f
            ExposureType.CROWDS -> 1.3f
        }

        tvStatus.text = "Carregando modelo: ${getExposureTypeName()}..."

        modelManager.loadModelForExposure(
            exposureType = exposureEnum,
            anchor = anchor,
            scale = scale,
            onSuccess = { anchorNode ->
                Log.d(TAG, "Modelo carregado! Adicionando na cena...")

                // ESSENCIAL: adicionar o AnchorNode na cena
                arFragment.arSceneView.scene.addChild(anchorNode)

                // Torna o modelo "mexível" para o usuário (pinch/rotate)
                val node = TransformableNode(arFragment.transformationSystem).apply {
                    setParent(anchorNode)
                    select()
                }

                placedAnchorNode = anchorNode
                placedNode = node

                tvStatus.text = "Modelo: ${getExposureTypeName()} (pronto)"
            },
            onError = { error ->
                Log.e(TAG, "Erro ao carregar modelo", error)
                tvStatus.text = "Erro ao carregar: ${error.message}"
            }
        )
    }

    private fun getExposureTypeName(): String {
        return when (exposureType) {
            "ROLLER_COASTER" -> "Roller Coaster"
            "HEIGHTS" -> "Alturas"
            "CLOSED_SPACES" -> "Espaços Fechados"
            "CROWDS" -> "Multidões"
            else -> "Exposição Padrão"
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

        // Limpa nós/anchors
        placedNode?.setParent(null)
        placedNode = null

        placedAnchorNode?.let { node ->
            try {
                node.anchor?.detach()
            } catch (_: Exception) {}
            node.setParent(null)
        }
        placedAnchorNode = null

        modelManager.clearCache()
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
            } else {
                Log.w(TAG, "Permissão de câmera negada")
                findViewById<MaterialTextView>(R.id.tv_ar_status).text =
                    "Permissão de câmera negada"
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopTimer()
        // ArFragment gerencia sessão internamente
    }

    override fun onDestroy() {
        stopTimer()
        modelManager.clearCache()
        super.onDestroy()
    }
}
