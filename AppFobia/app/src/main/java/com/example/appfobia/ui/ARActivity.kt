package com.example.appfobia.ui

import android.Manifest
import android.content.pm.PackageManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textview.MaterialTextView
import com.example.appfobia.ExposureType
import com.example.appfobia.R
import com.example.appfobia.ar.ARCoreRenderer
import com.example.appfobia.ar.ARCoreSessionManager
import com.example.appfobia.ar.CameraProvider
import com.example.appfobia.ar.ModelManager
import com.example.appfobia.ar.SceneManager
import com.google.ar.core.Pose

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
    private lateinit var cameraProvider: CameraProvider
    private lateinit var modelManager: ModelManager
    private lateinit var previewView: PreviewView

    private var modelAnchor: com.google.ar.core.Anchor? = null

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

            tvStatus.setText("Inicializando ARCore...")
            tvIntensity.setText("Intensidade: $intensity/10")

            // Inicializar componentes
            arCoreManager = ARCoreSessionManager(this)
            sceneManager = SceneManager(this)
            cameraProvider = CameraProvider(this, this)
            modelManager = ModelManager(this)

            // Criar PreviewView para captura da câmera (primeiro plano - mais importante)
            previewView = PreviewView(this)
            val previewParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            arContainer.addView(previewView, 0, previewParams)

            Log.d(TAG, "PreviewView adicionada (câmera ao vivo)")
            /*
            try {
                val exposureEnum = ExposureType.valueOf(exposureType)
                renderer.setExposureType(exposureEnum.name)
                renderer.setIntensity(intensity)
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao definir tipo de exposicao", e)
            }
            */
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
                    tvStatus.setText("Ativo - ${getExposureTypeName()}")
                } else {
                    stopTimer()
                    tvStatus.setText("Pausado")
                }
                btnPlay.setImageResource(
                    if (isRunning) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
                )
            }

            btnIncrease.setOnClickListener {
                if (intensity < 10) {
                    intensity++
                    tvIntensity.setText("Intensidade: $intensity/10")
                    renderer.setIntensity(intensity)
                }
            }

            btnDecrease.setOnClickListener {
                if (intensity > 1) {
                    intensity--
                    tvIntensity.setText("Intensidade: $intensity/10")
                    renderer.setIntensity(intensity)
                }
            }

            btnEnd.setOnClickListener {
                stopTimer()
                finishSession()
            }

            try {
                val exposureTypeEnum = ExposureType.valueOf(exposureType)
                tvStatus.setText("AR Ativo - ${sceneManager.getSceneDescription(exposureTypeEnum).take(40)}")
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
                    Log.d(TAG, "ARCore inicializado com sucesso")

                    // Pré-carregar modelos
                    modelManager.preloadAllModels { success ->
                        if (success) {
                            Log.d(TAG, "Todos os modelos pré-carregados!")
                            // Carregar o modelo do tipo de exposição atual
                            loadExposureModel(tvStatus)
                        } else {
                            Log.w(TAG, "Alguns modelos falharam ao carregar")
                            loadExposureModel(tvStatus)
                        }
                    }

                    // Iniciar câmera
                    startCameraCapture()
                }
            } else {
                tvStatus.setText("Erro ao inicializar ARCore")
                Log.e(TAG, "Falha ao inicializar sessao ARCore")
            }
        } else {
            tvStatus.setText("ARCore nao suportado - Modo Simulacao")
            Log.w(TAG, "Dispositivo nao suporta ARCore")
        }
    }

    private fun loadExposureModel(tvStatus: MaterialTextView) {
        try {
            val exposureEnum = ExposureType.valueOf(exposureType)
            arCoreManager.getSession()?.let { session ->

                // Criar um anchor para posicionar o modelo
                // Posiciona na frente do usuário (distância -1.5f metros)
                val pose = Pose.makeTranslation(0f, 0f, -1.5f)
                val anchor = session.createAnchor(pose)

                if (anchor != null) {
                    modelAnchor = anchor

                    // Carregar com escala otimizada
                    val scale = when (exposureEnum) {
                        ExposureType.CLOSED_SPACES -> 1.2f  // Elevador um pouco maior
                        ExposureType.HEIGHTS -> 1.5f        // Plataforma bem visível
                        ExposureType.ROLLER_COASTER -> 1.0f // Tamanho normal
                        ExposureType.CROWDS -> 1.3f         // Multidão bem visível
                    }

                    modelManager.loadModelForExposure(
                        exposureType = exposureEnum,
                        anchor = anchor,
                        scale = scale,
                        onSuccess = { anchorNode ->
                            Log.d(TAG, " Modelo renderizado! Você pode agora colocar na frente dos olhos")
                            tvStatus.setText(" Modelo: ${getExposureTypeName()}")
                        },
                        onError = { error ->
                            Log.e(TAG, " Erro ao carregar modelo", error)
                            tvStatus.setText(" Erro ao carregar: ${error.message}")
                        }
                    )
                } else {
                    Log.e(TAG, "Falha ao criar anchor para modelo")
                    tvStatus.setText("Erro: Não foi possível criar anchor")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao carregar modelo de exposição", e)
            tvStatus.setText("Erro: ${e.message}")
        }
    }

    private fun startCameraCapture() {
        try {
            cameraProvider.startCamera(previewView.surfaceProvider)
            Log.d(TAG, " Câmera iniciada com sucesso")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao iniciar câmera", e)
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
                        tvTimer.setText(String.format("Tempo: %02d:%02d", minutes, seconds))
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
        cameraProvider.stopCamera()
        cameraProvider.shutdown()
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
        arCoreManager.resumeSession()
        startCameraCapture()
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        arCoreManager.pauseSession()
        cameraProvider.stopCamera()
        super.onPause()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        stopTimer()
        arCoreManager.closeSession()
        cameraProvider.stopCamera()
        cameraProvider.shutdown()
        modelManager.clearCache()
        super.onDestroy()
    }
}