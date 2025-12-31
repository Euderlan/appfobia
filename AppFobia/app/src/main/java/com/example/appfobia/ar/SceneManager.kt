package com.example.appfobia.ar

import android.content.Context
import com.google.ar.core.Anchor
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.example.appfobia.ExposureType

data class ARModel(
    val name: String,
    val type: ExposureType,
    val position: FloatArray = floatArrayOf(0f, -0.5f, -2f),
    val description: String
)

class SceneManager(private val context: Context) {

    fun getModelForExposure(type: ExposureType): ARModel {
        return when (type) {
            ExposureType.ROLLER_COASTER -> ARModel(
                name = "roller_coaster",
                type = ExposureType.ROLLER_COASTER,
                position = floatArrayOf(0f, -0.5f, -2f),
                description = "Roller coaster 3D com movimento"
            )
            ExposureType.HEIGHTS -> ARModel(
                name = "heights",
                type = ExposureType.HEIGHTS,
                position = floatArrayOf(0f, 0f, -2f),
                description = "Plataforma em altura"
            )
            ExposureType.CLOSED_SPACES -> ARModel(
                name = "closed_spaces",
                type = ExposureType.CLOSED_SPACES,
                position = floatArrayOf(0f, 0f, -1.5f),
                description = "Sala fechada/elevador"
            )
            ExposureType.CROWDS -> ARModel(
                name = "crowds",
                type = ExposureType.CROWDS,
                position = floatArrayOf(0f, 0f, -2f),
                description = "Multidão de pessoas"
            )
        }
    }

    fun createAnchor(session: Session?, position: FloatArray = floatArrayOf(0f, 0f, -2f)): Anchor? {
        return try {
            if (session == null) return null

            // Criar uma pose usando o método estático make() do Pose
            // Pose.makeTranslation(x, y, z) cria uma pose com apenas translação
            val pose = Pose.makeTranslation(position[0], position[1], position[2])

            // Cria âncora com a pose
            session.createAnchor(pose)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getSceneDescription(type: ExposureType): String {
        return when (type) {
            ExposureType.ROLLER_COASTER -> "Visualizando um roller coaster em movimento. Você está sentado no carrinho, sentindo a adrenalina das curvas e velocidade."
            ExposureType.HEIGHTS -> "Você está em uma plataforma muito alta. A visão do chão distante pode causar sensação de vertigem."
            ExposureType.CLOSED_SPACES -> "Você está dentro de um elevador em movimento. As paredes estão próximas e o espaço é limitado."
            ExposureType.CROWDS -> "Você está em meio a uma multidão de pessoas. Todos ao seu redor, sons altos, movimento constante."
        }
    }
}