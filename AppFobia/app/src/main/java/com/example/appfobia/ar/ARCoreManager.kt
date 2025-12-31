package com.example.appfobia.ar

import android.content.Context
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Session

class ARCoreManager(private val context: Context) {

    private var session: Session? = null

    fun initializeARCore(): Boolean {
        return try {
            val availability = ArCoreApk.getInstance().checkAvailability(context)
            if (availability.isTransient) {
                // Re-query at 5Hz while we wait for ArCore to be ready.
                return false
            }
            if (availability.isUnsupported) {
                // This device is not supported.
                return false
            }

            if (session == null) {
                session = Session(context)
                session?.resume()
                return true
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getSession(): Session? = session

    fun pause() {
        session?.pause()
    }

    fun resume() {
        try {
            session?.resume()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun close() {
        session?.close()
        session = null
    }

    fun isARCoreAvailable(): Boolean {
        return try {
            ArCoreApk.getInstance().checkAvailability(context).isSupported
        } catch (e: Exception) {
            false
        }
    }
}