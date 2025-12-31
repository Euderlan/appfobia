package com.example.appfobia.ar

import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import com.google.cardboard.sdk.CardboardView
import com.google.cardboard.sdk.HeadTransform
import com.google.cardboard.sdk.Eye
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class VRRenderer : CardboardView.StereoRenderer {

    private val leftProjectionMatrix = FloatArray(16)
    private val rightProjectionMatrix = FloatArray(16)
    private val leftViewMatrix = FloatArray(16)
    private val rightViewMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    private var exposureType: String = "ROLLER_COASTER"
    private var intensity: Int = 5
    private var rotationAngle = 0f
    private var headTransformMatrix = FloatArray(16)

    override fun onNewFrame(headTransform: HeadTransform) {
        // Obter matriz de transformação da cabeça (head tracking)
        headTransform.getHeadMatrix(headTransformMatrix, 0)

        // Atualizar rotação para animação
        rotationAngle += 0.5f

        // Log para debug (opcional)
        if (rotationAngle.toInt() % 60 == 0) {
            Log.d("VRRenderer", "Renderizando: $exposureType, intensidade: $intensity")
        }
    }

    override fun onDrawEye(eye: Eye) {
        // Limpar buffer
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Obter matrizes do eye (projeção e view)
        eye.projectionMatrix.let { projection ->
            projection.copyInto(0, leftProjectionMatrix, 0, 16)
        }

        // Renderizar baseado no tipo de exposição
        when (exposureType) {
            "ROLLER_COASTER" -> drawRollerCoasterVR()
            "HEIGHTS" -> drawHeightsVR()
            "CLOSED_SPACES" -> drawClosedSpaceVR()
            "CROWDS" -> drawCrowdsVR()
            else -> drawDefaultVR()
        }
    }

    override fun onFinishFrame() {
        // Finalizar frame - já feito automaticamente
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Cor de fundo preta
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glCullFace(GLES20.GL_BACK)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        // Viewport é automaticamente ajustado pelo CardboardView
        GLES20.glViewport(0, 0, width, height)
    }

    private fun drawRollerCoasterVR() {
        // Roller coaster: gradiente de cores que muda com intensidade
        val (r, g, b) = getColorByIntensity()

        // Aplicar efeito de movimento (cambaleio)
        val wobble = kotlin.math.sin(rotationAngle * 0.1f) * 0.2f

        GLES20.glClearColor(
            (r + wobble).coerceIn(0f, 1f),
            g,
            (b - wobble).coerceIn(0f, 1f),
            1.0f
        )
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Desenhar linhas horizontais para simular movimento
        drawAnimatedRollerCoasterLines()
    }

    private fun drawHeightsVR() {
        // Alturas: tons azuis escuros para simular céu/profundidade
        val intensity_factor = intensity / 10f

        // Quanto maior intensidade, mais escuro fica
        GLES20.glClearColor(
            0.1f * intensity_factor,
            0.2f * intensity_factor,
            0.5f - (intensity_factor * 0.3f),
            1.0f
        )
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Efeito de profundidade com padrões
        drawDepthPattern()
    }

    private fun drawClosedSpaceVR() {
        // Espaços fechados: paredes próximas, cores quentes
        val intensity_factor = intensity / 10f

        // Quanto maior intensidade, mais fechado parece
        GLES20.glClearColor(
            0.3f + (intensity_factor * 0.3f),
            0.2f,
            0.4f - (intensity_factor * 0.2f),
            1.0f
        )
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Efeito de paredes se aproximando
        drawMovingWalls()
    }

    private fun drawCrowdsVR() {
        // Multidões: cores vibrantes, mais ativas conforme intensidade
        val intensity_factor = intensity / 10f

        // Cores mais saturadas em intensidades altas
        val r = 0.6f + (intensity_factor * 0.4f)
        val g = 0.3f + (intensity_factor * 0.2f)
        val b = 0.3f

        GLES20.glClearColor(r, g, b, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Efeito de movimento de pessoas ao redor
        drawMovingCrowd()
    }

    private fun drawDefaultVR() {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
    }

    private fun drawAnimatedRollerCoasterLines() {
        // Simular movimento horizontal com padrões
        val speed = (rotationAngle * 0.5f) % 2f

        // Cores alternadas que se movem
        for (i in 0 until 10) {
            val yPos = -1f + (i * 0.2f) + (speed * 0.1f)
            if (i % 2 == 0) {
                GLES20.glClearColor(0.2f, 0.4f, 0.8f, 1.0f)
            } else {
                GLES20.glClearColor(0.1f, 0.2f, 0.5f, 1.0f)
            }
        }
    }

    private fun drawDepthPattern() {
        // Padrão que cria sensação de profundidade/altura
        val pulse = kotlin.math.sin(rotationAngle * 0.05f) * 0.3f

        GLES20.glClearColor(
            0.1f,
            0.2f + pulse,
            0.5f,
            1.0f
        )
    }

    private fun drawMovingWalls() {
        // Efeito de paredes se aproximando/afastando
        val wave = kotlin.math.sin(rotationAngle * 0.08f) * 0.2f

        GLES20.glClearColor(
            0.3f + wave,
            0.2f,
            0.4f - wave,
            1.0f
        )
    }

    private fun drawMovingCrowd() {
        // Simular movimento de múltiplas pessoas ao redor
        val rotation = (rotationAngle * 0.02f) % (3.14159f * 2f)
        val movement = kotlin.math.sin(rotation) * 0.3f

        GLES20.glClearColor(
            0.6f + movement,
            0.3f + movement,
            0.3f,
            1.0f
        )
    }

    private fun getColorByIntensity(): Triple<Float, Float, Float> {
        return when (intensity) {
            in 1..3 -> Triple(0.0f, 0.5f, 1.0f)    // Azul claro (calmo)
            in 4..6 -> Triple(1.0f, 0.8f, 0.0f)    // Amarelo (médio)
            in 7..10 -> Triple(1.0f, 0.2f, 0.2f)   // Vermelho (intenso)
            else -> Triple(0.5f, 0.5f, 0.5f)
        }
    }

    fun setExposureType(type: String) {
        exposureType = type
        Log.d("VRRenderer", "Tipo de exposição: $exposureType")
    }

    fun setIntensity(intensityLevel: Int) {
        intensity = intensityLevel.coerceIn(1, 10)
        Log.d("VRRenderer", "Intensidade: $intensity")
    }

    // Extensão para FloatArray (copiar valores de um array para outro)
    private fun FloatArray.copyInto(srcPos: Int, dest: FloatArray, destPos: Int, length: Int) {
        for (i in 0 until length) {
            if (srcPos + i < size && destPos + i < dest.size) {
                dest[destPos + i] = this[srcPos + i]
            }
        }
    }
}