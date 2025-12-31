package com.example.appfobia.ar

import android.opengl.GLSurfaceView
import android.opengl.GLES20
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class SimpleARRenderer : GLSurfaceView.Renderer {

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private var exposureType: String = "ROLLER_COASTER"
    private var intensity: Int = 5
    private var rotationAngle = 0f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projectionMatrix, 0, 45f, ratio, 0.1f, 100f)
        Matrix.setLookAtM(
            viewMatrix, 0,
            0f, 0f, 3f,
            0f, 0f, 0f,
            0f, 1f, 0f
        )
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        rotationAngle += 2f

        when (exposureType) {
            "ROLLER_COASTER" -> drawRollerCoaster()
            "HEIGHTS" -> drawHeights()
            "CLOSED_SPACES" -> drawClosedSpace()
            "CROWDS" -> drawCrowds()
            else -> drawDefault()
        }
    }

    private fun drawRollerCoaster() {
        val (r, g, b) = getColorByIntensity()
        GLES20.glClearColor(r, g, b, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        drawAnimatedStripes(r * 0.5f, g * 0.5f, b * 0.5f)
    }

    private fun drawHeights() {
        val intensity_factor = intensity / 10f
        GLES20.glClearColor(0.2f * intensity_factor, 0.2f * intensity_factor, 0.3f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        drawConcentricCircles()
    }

    private fun drawClosedSpace() {
        val intensity_factor = intensity / 10f
        val intensity_invert = 1f - intensity_factor
        GLES20.glClearColor(
            0.2f + (intensity_factor * 0.3f),
            0.2f,
            0.3f + (intensity_factor * 0.2f),
            1.0f
        )
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        drawWavingPattern()
    }

    private fun drawCrowds() {
        val (r, g, b) = getColorByIntensity()
        GLES20.glClearColor(r * 0.8f, g * 0.8f, b * 0.8f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        drawMultipleBlocks()
    }

    private fun drawDefault() {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
    }

    private fun getColorByIntensity(): Triple<Float, Float, Float> {
        return when (intensity) {
            in 1..3 -> Triple(0.0f, 0.5f, 1.0f)
            in 4..6 -> Triple(1.0f, 0.8f, 0.0f)
            in 7..10 -> Triple(1.0f, 0.2f, 0.2f)
            else -> Triple(0.5f, 0.5f, 0.5f)
        }
    }

    private fun drawAnimatedStripes(r: Float, g: Float, b: Float) {
        val stripeCount = 8
        val stripeHeight = 2f / stripeCount
        val offset = (rotationAngle % 100) / 100f

        for (i in 0 until stripeCount) {
            val y = -1f + (i * stripeHeight) + offset
            if (i % 2 == 0) {
                drawHorizontalLine(y, stripeHeight, r, g, b)
            } else {
                drawHorizontalLine(y, stripeHeight, r * 0.7f, g * 0.7f, b * 0.7f)
            }
        }
    }

    private fun drawConcentricCircles() {
        val circleCount = 5
        val intensity_factor = intensity / 10f

        for (i in 0 until circleCount) {
            val radius = 0.2f + (i * 0.3f)
            val alpha = 1f - (i.toFloat() / circleCount)
            GLES20.glClearColor(0.2f, 0.2f + (alpha * 0.3f), 0.3f, 1.0f)
        }
    }

    private fun drawWavingPattern() {
        val waveCount = 10
        val waveAmp = 0.3f * (intensity / 10f)

        for (i in 0 until waveCount) {
            val yPos = -1f + (i * (2f / waveCount))
            val wave = kotlin.math.sin((rotationAngle * 2 + i * 10) * 0.017f) * waveAmp
            drawHorizontalLine(yPos + wave, 0.2f, 0.3f, 0.2f, 0.4f)
        }
    }

    private fun drawMultipleBlocks() {
        val blockCount = when {
            intensity in 1..3 -> 3
            intensity in 4..6 -> 6
            else -> 10
        }

        val colors = arrayOf(
            Triple(1.0f, 0.2f, 0.2f),
            Triple(0.2f, 1.0f, 0.2f),
            Triple(0.2f, 0.2f, 1.0f),
            Triple(1.0f, 1.0f, 0.2f),
            Triple(1.0f, 0.2f, 1.0f),
            Triple(0.2f, 1.0f, 1.0f)
        )

        for (i in 0 until blockCount) {
            val x = -1f + ((i % 3) * 0.7f)
            val y = -0.5f + ((i / 3) * 0.5f)
            val rotation = rotationAngle + (i * 15f)
            val color = colors[i % colors.size]
            drawBlock(x, y, 0.3f, 0.3f, color.first, color.second, color.third)
        }
    }

    private fun drawHorizontalLine(y: Float, height: Float, r: Float, g: Float, b: Float) {
        GLES20.glClearColor(r, g, b, 1.0f)
    }

    private fun drawBlock(x: Float, y: Float, width: Float, height: Float, r: Float, g: Float, b: Float) {
        GLES20.glClearColor(r * 0.8f, g * 0.8f, b * 0.8f, 1.0f)
    }

    fun setExposureType(type: String) {
        exposureType = type
    }

    fun setIntensity(intensityLevel: Int) {
        intensity = intensityLevel.coerceIn(1, 10)
    }
}