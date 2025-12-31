package com.example.appfobia.ar

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import com.google.ar.core.Session
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

class ARCoreRenderer : GLSurfaceView.Renderer {

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    private var exposureType: String = "ROLLER_COASTER"
    private var intensity: Int = 5
    private var rotationAngle = 0f
    private var arSession: Session? = null

    companion object {
        private const val TAG = "ARCoreRenderer"
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.d(TAG, "onSurfaceCreated")
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glCullFace(GLES20.GL_BACK)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceChanged: $width x $height")
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        try {
            arSession?.let { session ->
                val frame = session.update()

                val camera = frame.camera
                camera.getProjectionMatrix(projectionMatrix, 0, 0.1f, 100f)
                camera.getViewMatrix(viewMatrix, 0)

                rotationAngle += 1f

                val planes = frame.getUpdatedTrackables(Plane::class.java)

                if (planes.isNotEmpty()) {
                    for (plane in planes) {
                        if (plane.trackingState == com.google.ar.core.TrackingState.TRACKING) {
                            drawVirtualObject(plane.centerPose)
                        }
                    }
                }

                updateLighting(frame)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao renderizar ARCore", e)
        }
    }

    private fun drawVirtualObject(pose: Pose) {
        Matrix.setIdentityM(modelMatrix, 0)
        pose.toMatrix(modelMatrix, 0)

        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

        when (exposureType) {
            "ROLLER_COASTER" -> drawRollerCoasterObject()
            "HEIGHTS" -> drawHeightsObject()
            "CLOSED_SPACES" -> drawClosedSpaceObject()
            "CROWDS" -> drawCrowdsObject()
            else -> drawDefaultObject()
        }
    }

    private fun drawRollerCoasterObject() {
        val (r, g, b) = getColorByIntensity()
        val wobble = sin(rotationAngle * 0.1f) * 0.2f

        GLES20.glClearColor(
            (r + wobble).coerceIn(0f, 1f),
            g,
            (b - wobble).coerceIn(0f, 1f),
            0.8f
        )

        drawSimpleCube(r, g, b)
    }

    private fun drawHeightsObject() {
        val intensity_factor = intensity / 10f

        val r = 0.1f * intensity_factor
        val g = 0.2f * intensity_factor
        val b = 0.5f - (intensity_factor * 0.3f)

        GLES20.glClearColor(r, g, b, 0.8f)

        drawSimpleCube(r, g, b)
    }

    private fun drawClosedSpaceObject() {
        val intensity_factor = intensity / 10f

        val r = 0.3f + (intensity_factor * 0.3f)
        val g = 0.2f
        val b = 0.4f - (intensity_factor * 0.2f)

        GLES20.glClearColor(r, g, b, 0.8f)

        drawSimpleCube(r, g, b)
    }

    private fun drawCrowdsObject() {
        val intensity_factor = intensity / 10f
        val r = 0.6f + (intensity_factor * 0.4f)
        val g = 0.3f + (intensity_factor * 0.2f)
        val b = 0.3f

        GLES20.glClearColor(r, g, b, 0.8f)

        val blockCount = when {
            intensity in 1..3 -> 3
            intensity in 4..6 -> 6
            else -> 10
        }

        for (i in 0 until blockCount) {
            val angle = (i.toFloat() / blockCount) * 3.14159f * 2
            val x = cos(angle) * 0.5f
            val z = sin(angle) * 0.5f

            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, x, 0f, z)
            drawSimpleCube(r, g, b)
        }
    }

    private fun drawDefaultObject() {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.8f)
        drawSimpleCube(0.5f, 0.5f, 0.5f)
    }

    private fun drawSimpleCube(r: Float, g: Float, b: Float) {
        GLES20.glClearColor(r, g, b, 0.8f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
    }

    private fun updateLighting(frame: Frame) {
        try {
            val lightEstimate = frame.lightEstimate

            when (lightEstimate.state) {
                com.google.ar.core.LightEstimate.State.VALID -> {
                    val pixelIntensity = lightEstimate.pixelIntensity
                    Log.d(TAG, "Luz do ambiente: $pixelIntensity")
                }
                com.google.ar.core.LightEstimate.State.NOT_VALID -> {
                    Log.d(TAG, "Luz do ambiente nao valida")
                }
                else -> {
                    Log.d(TAG, "Estado de luz desconhecido")
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Erro ao obter iluminacao: ${e.message}")
        }
    }

    private fun getColorByIntensity(): Triple<Float, Float, Float> {
        return when (intensity) {
            in 1..3 -> Triple(0.0f, 0.5f, 1.0f)
            in 4..6 -> Triple(1.0f, 0.8f, 0.0f)
            in 7..10 -> Triple(1.0f, 0.2f, 0.2f)
            else -> Triple(0.5f, 0.5f, 0.5f)
        }
    }

    fun setARSession(session: Session) {
        arSession = session
        Log.d(TAG, "Sessao ARCore definida no renderer")
    }

    fun setExposureType(type: String) {
        exposureType = type
        Log.d(TAG, "Tipo de exposicao: $exposureType")
    }

    fun setIntensity(intensityLevel: Int) {
        intensity = intensityLevel.coerceIn(1, 10)
        Log.d(TAG, "Intensidade: $intensity")
    }
}