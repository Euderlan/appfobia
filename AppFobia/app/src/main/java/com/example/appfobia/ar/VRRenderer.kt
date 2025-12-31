package com.example.appfobia.vr

import android.opengl.GLSurfaceView
import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class VRRenderer360 : GLSurfaceView.Renderer {

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    private var exposureType: String = "ROLLER_COASTER"
    private var intensity: Int = 5
    private var rotationAngle = 0f
    private var screenWidth = 0
    private var screenHeight = 0

    private var skyboxVertices: FloatArray? = null
    private var skyboxIndices: ShortArray? = null

    companion object {
        private const val TAG = "VRRenderer360"
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.d(TAG, "onSurfaceCreated")
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glCullFace(GLES20.GL_BACK)

        initializeSkybox()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceChanged: $width x $height")
        screenWidth = width
        screenHeight = height

        val ratio: Float = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projectionMatrix, 0, 45f, ratio, 0.1f, 100f)
        Matrix.setLookAtM(
            viewMatrix, 0,
            0f, 0f, 0f,
            0f, 0f, -1f,
            0f, 1f, 0f
        )
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        rotationAngle += 1f

        val halfWidth = screenWidth / 2

        GLES20.glViewport(0, 0, halfWidth, screenHeight)
        drawLeftEyeVR360()

        GLES20.glViewport(halfWidth, 0, halfWidth, screenHeight)
        drawRightEyeVR360()
    }

    private fun drawLeftEyeVR360() {
        val eyeDistance = 0.032f
        val leftViewMatrix = FloatArray(16)
        Matrix.setLookAtM(
            leftViewMatrix, 0,
            -eyeDistance, 0f, 0f,
            -eyeDistance, 0f, -1f,
            0f, 1f, 0f
        )

        when (exposureType) {
            "ROLLER_COASTER" -> drawRollerCoasterVR360(leftViewMatrix)
            "HEIGHTS" -> drawHeightsVR360(leftViewMatrix)
            "CLOSED_SPACES" -> drawClosedSpaceVR360(leftViewMatrix)
            "CROWDS" -> drawCrowdsVR360(leftViewMatrix)
            else -> drawDefaultVR360()
        }
    }

    private fun drawRightEyeVR360() {
        val eyeDistance = 0.032f
        val rightViewMatrix = FloatArray(16)
        Matrix.setLookAtM(
            rightViewMatrix, 0,
            eyeDistance, 0f, 0f,
            eyeDistance, 0f, -1f,
            0f, 1f, 0f
        )

        when (exposureType) {
            "ROLLER_COASTER" -> drawRollerCoasterVR360(rightViewMatrix)
            "HEIGHTS" -> drawHeightsVR360(rightViewMatrix)
            "CLOSED_SPACES" -> drawClosedSpaceVR360(rightViewMatrix)
            "CROWDS" -> drawCrowdsVR360(rightViewMatrix)
            else -> drawDefaultVR360()
        }
    }

    private fun drawRollerCoasterVR360(viewMatrix: FloatArray) {
        val (r, g, b) = getColorByIntensity()
        val wobble = sin(rotationAngle * 0.1f) * 0.2f

        GLES20.glClearColor(
            (r + wobble).coerceIn(0f, 1f),
            g,
            (b - wobble).coerceIn(0f, 1f),
            1.0f
        )
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        drawSkyboxWithGradient(r, g, b, viewMatrix)
    }

    private fun drawHeightsVR360(viewMatrix: FloatArray) {
        val intensity_factor = intensity / 10f

        GLES20.glClearColor(
            0.1f * intensity_factor,
            0.2f * intensity_factor,
            0.5f - (intensity_factor * 0.3f),
            1.0f
        )
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        drawHeightSkybox(intensity_factor, viewMatrix)
    }

    private fun drawClosedSpaceVR360(viewMatrix: FloatArray) {
        val intensity_factor = intensity / 10f

        GLES20.glClearColor(
            0.3f + (intensity_factor * 0.3f),
            0.2f,
            0.4f - (intensity_factor * 0.2f),
            1.0f
        )
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        drawClosedSkybox(intensity_factor, viewMatrix)
    }

    private fun drawCrowdsVR360(viewMatrix: FloatArray) {
        val intensity_factor = intensity / 10f
        val r = 0.6f + (intensity_factor * 0.4f)
        val g = 0.3f + (intensity_factor * 0.2f)
        val b = 0.3f

        GLES20.glClearColor(r, g, b, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        drawCrowdSkybox(r, g, b, viewMatrix)
    }

    private fun drawDefaultVR360() {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
    }

    private fun drawSkyboxWithGradient(r: Float, g: Float, b: Float, viewMatrix: FloatArray) {
        val top = rotationAngle * 0.01f
        val bands = 10

        for (i in 0 until bands) {
            val progress = i.toFloat() / bands
            val colorR = r + (progress * 0.3f).coerceIn(0f, 1f)
            val colorG = g + (progress * 0.2f).coerceIn(0f, 1f)
            val colorB = b - (progress * 0.2f).coerceIn(0f, 1f)

            GLES20.glClearColor(colorR, colorG, colorB, 1.0f)
        }
    }

    private fun drawHeightSkybox(intensity: Float, viewMatrix: FloatArray) {
        val pulse = sin(rotationAngle * 0.05f) * 0.3f
        val bands = 12

        for (i in 0 until bands) {
            val angle = (i.toFloat() / bands) * PI.toFloat() * 2
            val radius = 0.5f + sin(angle + rotationAngle * 0.02f) * 0.2f

            val r = 0.1f + pulse
            val g = 0.2f + (radius * 0.3f)
            val b = 0.5f

            GLES20.glClearColor(r, g, b, 1.0f)
        }
    }

    private fun drawClosedSkybox(intensity: Float, viewMatrix: FloatArray) {
        val wave = sin(rotationAngle * 0.08f) * 0.2f
        val bands = 16

        for (i in 0 until bands) {
            val waveMotion = sin((i.toFloat() / bands) * PI.toFloat() + rotationAngle * 0.05f) * 0.15f

            val r = 0.3f + wave + waveMotion
            val g = 0.2f
            val b = 0.4f - wave

            GLES20.glClearColor(r, g, b, 1.0f)
        }
    }

    private fun drawCrowdSkybox(r: Float, g: Float, b: Float, viewMatrix: FloatArray) {
        val movement = sin(rotationAngle * 0.02f) * 0.3f
        val blockCount = when {
            intensity in 1..3 -> 3
            intensity in 4..6 -> 6
            else -> 10
        }

        for (i in 0 until blockCount) {
            val angle = (i.toFloat() / blockCount) * PI.toFloat() * 2
            val x = cos(angle + rotationAngle * 0.01f)
            val z = sin(angle + rotationAngle * 0.01f)

            val colorR = (r + movement).coerceIn(0f, 1f)
            val colorG = (g + movement).coerceIn(0f, 1f)
            val colorB = b

            GLES20.glClearColor(colorR, colorG, colorB, 1.0f)
        }
    }

    private fun initializeSkybox() {
        val latBands = 20
        val lonBands = 30
        val radius = 50f

        val vertices = mutableListOf<Float>()
        val indices = mutableListOf<Short>()

        for (lat in 0..latBands) {
            val theta = (lat.toFloat() / latBands) * PI.toFloat()
            val sinTheta = sin(theta)
            val cosTheta = cos(theta)

            for (lon in 0..lonBands) {
                val phi = (lon.toFloat() / lonBands) * PI.toFloat() * 2
                val sinPhi = sin(phi)
                val cosPhi = cos(phi)

                val x = cosPhi * sinTheta
                val y = cosTheta
                val z = sinPhi * sinTheta

                vertices.add(x * radius)
                vertices.add(y * radius)
                vertices.add(z * radius)
            }
        }

        for (lat in 0 until latBands) {
            for (lon in 0 until lonBands) {
                val first = (lat * (lonBands + 1) + lon).toShort()
                val second = (first + lonBands + 1).toShort()

                indices.add(first)
                indices.add(second)
                indices.add((first + 1).toShort())

                indices.add((first + 1).toShort())
                indices.add(second)
                indices.add((second + 1).toShort())
            }
        }

        skyboxVertices = vertices.toFloatArray()
        skyboxIndices = indices.toShortArray()
    }

    private fun getColorByIntensity(): Triple<Float, Float, Float> {
        return when (intensity) {
            in 1..3 -> Triple(0.0f, 0.5f, 1.0f)
            in 4..6 -> Triple(1.0f, 0.8f, 0.0f)
            in 7..10 -> Triple(1.0f, 0.2f, 0.2f)
            else -> Triple(0.5f, 0.5f, 0.5f)
        }
    }

    fun setExposureType(type: String) {
        exposureType = type
        Log.d(TAG, "Exposicao: $exposureType")
    }

    fun setIntensity(intensityLevel: Int) {
        intensity = intensityLevel.coerceIn(1, 10)
        Log.d(TAG, "Intensidade: $intensity")
    }
}