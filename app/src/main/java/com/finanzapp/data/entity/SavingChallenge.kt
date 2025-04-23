package com.finanzapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entidad que representa un desafío de ahorro para el usuario.
 * Parte del sistema de gamificación "Finanz Hero".
 */
@Entity(tableName = "saving_challenges")
data class SavingChallenge(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val targetAmount: Double,
    val rewardPoints: Int,
    val difficulty: ChallengeDifficulty,
    val type: ChallengeType,
    val duration: Int, // En días
    val iconName: String? = null,
    val colorCode: String? = null,
    val isCompleted: Boolean = false,
    val isActive: Boolean = false,
    val startDate: Date? = null,
    val endDate: Date? = null,
    val progress: Double = 0.0,
    val createdAt: Date = Date()
)

/**
 * Diferentes niveles de dificultad para los desafíos.
 */
enum class ChallengeDifficulty {
    BEGINNER, // Para usuarios nuevos
    EASY,     // Desafíos simples
    MEDIUM,   // Moderadamente difíciles
    HARD,     // Difíciles de lograr
    EXPERT    // Para los más valientes
}

/**
 * Tipos de desafíos de ahorro.
 */
enum class ChallengeType {
    DAILY,        // Desafíos diarios (guardar una pequeña cantidad cada día)
    WEEKLY,       // Desafíos semanales (hacer una acción una vez por semana)
    NO_SPEND,     // No gastar en cierta categoría por un tiempo
    PERCENTAGE,   // Ahorrar un porcentaje de los ingresos
    ONE_TIME,     // Una acción única para ahorrar
    STREAK        // Mantener una racha de ahorro
}