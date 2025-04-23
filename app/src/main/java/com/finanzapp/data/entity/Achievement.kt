package com.finanzapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entidad que representa un logro o medalla que puede obtener el usuario.
 * Parte del sistema de gamificación "Finanz Hero".
 */
@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val category: AchievementCategory,
    val tier: AchievementTier,
    val pointsReward: Int,
    val iconName: String,
    val isUnlocked: Boolean = false,
    val unlockedDate: Date? = null,
    val progress: Int = 0,        // Progreso actual
    val targetProgress: Int = 100, // Progreso necesario para desbloquear (porcentaje)
    val conditions: String,       // Descripción de los requisitos para desbloquear
    val createdAt: Date = Date()
)

/**
 * Categorías de logros.
 */
enum class AchievementCategory {
    SAVINGS,     // Relacionados con metas de ahorro
    BUDGET,      // Relacionados con presupuestos
    EXPENSES,    // Relacionados con control de gastos
    INCOME,      // Relacionados con ingresos
    STREAKS,     // Relacionados con rachas de uso/ahorro
    SPECIAL      // Logros especiales o eventos
}

/**
 * Niveles de logros, cada uno más difícil de conseguir.
 */
enum class AchievementTier {
    BRONZE,   // Fácil de obtener
    SILVER,   // Moderadamente difícil
    GOLD,     // Difícil de obtener
    PLATINUM, // Muy difícil
    DIAMOND   // Extremadamente difícil, para los más dedicados
}