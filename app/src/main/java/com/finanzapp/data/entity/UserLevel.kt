package com.finanzapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entidad que representa el nivel de experiencia del usuario en el sistema de gamificación.
 * A medida que el usuario completa desafíos y logros, gana puntos de experiencia (XP)
 * que le permiten subir de nivel y desbloquear nuevas funcionalidades o recompensas.
 */
@Entity(tableName = "user_levels")
data class UserLevel(
    @PrimaryKey
    val id: Long = 1, // Solo habrá un registro por usuario
    val level: Int = 1,
    val currentXP: Int = 0,
    val xpToNextLevel: Int = 100,
    val totalXPEarned: Int = 0,
    val savingRank: SavingRank = SavingRank.NOVICE,
    val rankPoints: Int = 0,
    val lastLevelUpDate: Date? = null,
    val streakBonus: Double = 1.0, // Multiplicador de XP basado en rachas (1.0 = 100%, 1.5 = 150%)
    val lastUpdated: Date = Date()
)

/**
 * Rangos de "héroe del ahorro" que el usuario puede alcanzar.
 * Cada rango tiene diferentes beneficios y apariencia visual.
 */
enum class SavingRank {
    NOVICE,           // 0 puntos - Principiante
    SAVER,            // 100 puntos - Ahorrador
    MONEY_MASTER,     // 500 puntos - Maestro del Dinero
    BUDGET_NINJA,     // 1000 puntos - Ninja del Presupuesto
    WEALTH_WARRIOR,   // 2500 puntos - Guerrero de la Riqueza
    FINANCE_LEGEND,   // 5000 puntos - Leyenda Financiera
    ECONOMY_TITAN,    // 10000 puntos - Titán Económico
    SAVINGS_SUPERHERO // 25000 puntos - Superhéroe del Ahorro
}

/**
 * Clase de utilidad para cálculos relacionados con los niveles de usuario.
 */
object LevelHelper {

    /**
     * Calcula los puntos de experiencia necesarios para alcanzar un nivel específico.
     * Utiliza una fórmula de progresión geométrica para hacer que los niveles
     * sean cada vez más difíciles de alcanzar.
     *
     * @param level El nivel para el que se calculan los puntos necesarios
     * @return Cantidad de XP necesaria para ese nivel
     */
    fun calculateXpForLevel(level: Int): Int {
        return when {
            level <= 1 -> 0
            level == 2 -> 100
            level <= 5 -> 100 * (level - 1) * (level - 1)
            level <= 10 -> 150 * (level - 1) * (level - 1)
            level <= 20 -> 200 * (level - 1) * (level - 1)
            level <= 50 -> 250 * (level - 1) * (level - 1)
            else -> 300 * (level - 1) * (level - 1)
        }
    }

    /**
     * Determina el rango basado en los puntos acumulados.
     *
     * @param points Puntos totales del usuario
     * @return El rango correspondiente
     */
    fun getRankFromPoints(points: Int): SavingRank {
        return when {
            points < 100 -> SavingRank.NOVICE
            points < 500 -> SavingRank.SAVER
            points < 1000 -> SavingRank.MONEY_MASTER
            points < 2500 -> SavingRank.BUDGET_NINJA
            points < 5000 -> SavingRank.WEALTH_WARRIOR
            points < 10000 -> SavingRank.FINANCE_LEGEND
            points < 25000 -> SavingRank.ECONOMY_TITAN
            else -> SavingRank.SAVINGS_SUPERHERO
        }
    }

    /**
     * Añade experiencia al usuario y actualiza su nivel si corresponde.
     *
     * @param userLevel Estado actual del usuario
     * @param xpToAdd Cantidad de XP a añadir
     * @return UserLevel actualizado
     */
    fun addExperience(userLevel: UserLevel, xpToAdd: Int): UserLevel {
        // Aplicamos el multiplicador de racha si existe
        val adjustedXp = (xpToAdd * userLevel.streakBonus).toInt()

        var newXp = userLevel.currentXP + adjustedXp
        var newLevel = userLevel.level
        var xpForNextLevel = userLevel.xpToNextLevel
        var newTotalXp = userLevel.totalXPEarned + adjustedXp
        var lastLevelUp = userLevel.lastLevelUpDate

        // Verificar si subimos de nivel
        while (newXp >= xpForNextLevel) {
            newXp -= xpForNextLevel
            newLevel++
            xpForNextLevel = calculateXpForLevel(newLevel + 1) - calculateXpForLevel(newLevel)
            lastLevelUp = Date() // Actualizamos la fecha de subida de nivel
        }

        // Actualizar rango si corresponde
        val newRankPoints = userLevel.rankPoints + (adjustedXp / 10) // 10% de XP se convierten en puntos de rango
        val newRank = getRankFromPoints(newRankPoints)

        return userLevel.copy(
            level = newLevel,
            currentXP = newXp,
            xpToNextLevel = xpForNextLevel,
            totalXPEarned = newTotalXp,
            savingRank = newRank,
            rankPoints = newRankPoints,
            lastLevelUpDate = lastLevelUp,
            lastUpdated = Date()
        )
    }
}