/*package com.example.appfobia.ar

import android.view.SurfaceView
import com.google.android.filament.Camera
import com.google.android.filament.Engine
import com.google.android.filament.Renderer
import com.google.android.filament.Scene
import com.google.android.filament.View
import com.google.android.filament.android.UiHelper
import com.google.android.filament.gltfio.FilamentAsset
import kotlin.math.cos
import kotlin.math.sin

class FilamentRenderer(private val surfaceView: SurfaceView) {

    private lateinit var engine: Engine
    private lateinit var renderer: Renderer
    private lateinit var scene: Scene
    private lateinit var view: View
    private lateinit var camera: Camera
    private lateinit var uiHelper: UiHelper

    private var exposureType: String = "ROLLER_COASTER"
    private var intensity: Int = 5
    private var rotationAngle = 0f

    private var currentAsset: FilamentAsset? = null

    fun initialize() {
        engine = Engine.create()
        renderer = engine.createRenderer()
        scene = engine.createScene()
        view = engine.createView()
        camera = engine.createCamera()

        uiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK)
        uiHelper.attachTo(surfaceView)

        view.scene = scene
        view.camera = camera

        setupCamera()
    }

    private fun setupCamera() {
        camera.setProjection(45.0, 1920.0 / 1080.0, 0.1, 100.0)
        val eye = doubleArrayOf(0.0, 0.0, 4.0)
        val center = doubleArrayOf(0.0, 0.0, 0.0)
        val up = doubleArrayOf(0.0, 1.0, 0.0)
        camera.lookAt(eye[0], eye[1], eye[2], center[0], center[1], center[2], up[0], up[1], up[2])
    }

    fun setModel(asset: FilamentAsset) {
        if (currentAsset != null) {
            val root = currentAsset!!.root
            scene.removeEntity(root)
        }

        currentAsset = asset
        val root = asset.root
        scene.addEntity(root)
    }

    fun render() {
        rotationAngle += 1f

        currentAsset?.let { asset ->
            val tm = engine.transformManager
            val instance = tm.getInstance(asset.root)

            val transform = when (exposureType) {
                "ROLLER_COASTER" -> getTransformRollerCoaster()
                "HEIGHTS" -> getTransformHeights()
                "CLOSED_SPACES" -> getTransformClosedSpace()
                "CROWDS" -> getTransformCrowds()
                else -> getTransformDefault()
            }

            tm.setTransform(instance, transform)
        }

        val swapChain = engine.createSwapChain(surfaceView)
        if (renderer.beginFrame(swapChain, view)) {
            renderer.render(view)
            renderer.endFrame()
        }
    }

    private fun getTransformRollerCoaster(): FloatArray {
        val wobble = sin(rotationAngle * 0.1f) * 0.2f
        return floatArrayOf(
            1.0f + wobble, 0f, 0f, 0f,
            0f, 1.0f, 0f, 0f,
            0f, 0f, 1.0f - wobble, 0f,
            0f, 0f, 0f, 1.0f
        )
    }

    private fun getTransformHeights(): FloatArray {
        val scale = 1.0f + (intensity / 10f) * 0.3f
        return floatArrayOf(
            scale, 0f, 0f, 0f,
            0f, scale, 0f, 0f,
            0f, 0f, scale, 0f,
            0f, 0f, 0f, 1.0f
        )
    }

    private fun getTransformClosedSpace(): FloatArray {
        val scale = 0.8f + (intensity / 10f) * 0.4f
        return floatArrayOf(
            scale, 0f, 0f, 0f,
            0f, scale, 0f, 0f,
            0f, 0f, scale, 0f,
            0f, 0f, 0f, 1.0f
        )
    }

    private fun getTransformCrowds(): FloatArray {
        val blockCount = when {
            intensity in 1..3 -> 3
            intensity in 4..6 -> 6
            else -> 10
        }

        val angle = (rotationAngle / blockCount) * 0.02f
        val x = cos(angle) * 0.5f
        val z = sin(angle) * 0.5f

        return floatArrayOf(
            1.0f, 0f, 0f, x.toFloat(),
            0f, 1.0f, 0f, 0f,
            0f, 0f, 1.0f, z.toFloat(),
            0f, 0f, 0f, 1.0f
        )
    }

    private fun getTransformDefault(): FloatArray {
        return floatArrayOf(
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f
        )
    }

    fun setExposureType(type: String) {
        exposureType = type
    }

    fun setIntensity(intensityLevel: Int) {
        intensity = intensityLevel.coerceIn(1, 10)
    }

    fun destroy() {
        uiHelper.detach()
        engine.destroy()
    }
}*/