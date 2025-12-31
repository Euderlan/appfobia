package com.example.appfobia.ar

import android.content.Context
import android.util.Log
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableException

class ARCoreSessionManager(private val context: Context) {

    private var session: Session? = null
    private var cameraPermissionGranted = false

    companion object {
        private const val TAG = "ARCoreSessionManager"
    }

    fun checkARCoreSupport(): Boolean {
        return try {
            val availability = ArCoreApk.getInstance().checkAvailability(context)
            when {
                availability.isTransient -> {
                    Log.w(TAG, "ARCore ainda nao pronto")
                    false
                }
                availability.isSupported -> {
                    Log.d(TAG, "ARCore suportado")
                    true
                }
                availability.isUnsupported -> {
                    Log.w(TAG, "Dispositivo nao suporta ARCore")
                    false
                }
                else -> false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar ARCore", e)
            false
        }
    }

    fun initializeSession(): Boolean {
        return try {
            if (session == null) {
                session = Session(context)
                configureSession()
                Log.d(TAG, "Sessao ARCore inicializada com sucesso")
            }
            true
        } catch (e: UnavailableException) {
            Log.e(TAG, "ARCore nao disponivel", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao inicializar sessao ARCore", e)
            false
        }
    }

    private fun configureSession() {
        session?.let { session ->
            val config = Config(session)
            config.focusMode = Config.FocusMode.AUTO
            config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
            config.depthMode = Config.DepthMode.AUTOMATIC
            session.configure(config)
            Log.d(TAG, "Configuracao ARCore aplicada")
        }
    }

    fun resumeSession(): Boolean {
        return try {
            session?.resume()
            Log.d(TAG, "Sessao ARCore resumida")
            true
        } catch (e: CameraNotAvailableException) {
            Log.e(TAG, "Camera nao disponivel", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao resumir sessao", e)
            false
        }
    }

    fun pauseSession() {
        try {
            session?.pause()
            Log.d(TAG, "Sessao ARCore pausada")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao pausar sessao", e)
        }
    }

    fun closeSession() {
        try {
            session?.close()
            session = null
            Log.d(TAG, "Sessao ARCore fechada")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao fechar sessao", e)
        }
    }

    fun getSession(): Session? = session

    fun setCameraPermissionGranted(granted: Boolean) {
        cameraPermissionGranted = granted
    }

    fun hasCameraPermission(): Boolean = cameraPermissionGranted

    fun isSessionActive(): Boolean = session != null
}