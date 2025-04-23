package com.finanzapp.data.repository

import androidx.lifecycle.LiveData
import com.finanzapp.data.dao.UserLevelDao
import com.finanzapp.data.entity.UserLevel
import com.finanzapp.data.entity.LevelHelper

class UserLevelRepository(private val userLevelDao: UserLevelDao) {

    val userLevel: LiveData<UserLevel?> = userLevelDao.getUserLevel()

    suspend fun initialize() {
        // Verificar si ya existe
        val existingLevel = userLevelDao.getUserLevelDirect()
        if (existingLevel == null) {
            // Crear nuevo nivel de usuario
            val newLevel = UserLevel(id = 1)
            userLevelDao.insert(newLevel)
        }
    }

    suspend fun update(userLevel: UserLevel) {
        userLevelDao.update(userLevel)
    }

    /**
     * Añade experiencia al usuario, actualizando su nivel si corresponde.
     * @param xpAmount Cantidad de XP a añadir
     * @param streakBonus Multiplicador de XP por racha de ahorro
     * @return true si el usuario subió de nivel, false en caso contrario
     */
    suspend fun addExperience(xpAmount: Int, streakBonus: Double = 1.0): Boolean {
        // Obtener nivel actual
        var level = userLevelDao.getUserLevelDirect() ?: UserLevel(id = 1)

        // Actualizar multiplicador de racha
        level = level.copy(streakBonus = streakBonus)

        // Nivel antes de actualizar
        val previousLevel = level.level

        // Actualizar nivel
        level = LevelHelper.addExperience(level, xpAmount)

        // Guardar cambios
        userLevelDao.update(level)

        // Devolver true si el nivel cambió
        return level.level > previousLevel
    }

    /**
     * Añade puntos de rango al usuario, actualizando su rango si corresponde.
     */
    suspend fun addRankPoints(points: Int): Boolean {
        var level = userLevelDao.getUserLevelDirect() ?: UserLevel(id = 1)

        // Guardar rango actual
        val previousRank = level.savingRank

        // Actualizar puntos
        val newRankPoints = level.rankPoints + points
        val newRank = LevelHelper.getRankFromPoints(newRankPoints)

        // Actualizar nivel
        level = level.copy(rankPoints = newRankPoints, savingRank = newRank)
        userLevelDao.update(level)

        // Devolver true si el rango cambió
        return newRank != previousRank
    }

    /**
     * Actualiza el multiplicador de experiencia basado en la racha.
     */
    suspend fun updateStreakBonus(bonus: Double) {
        userLevelDao.updateStreakBonus(bonus)
    }

    /**
     * Obtiene el nivel actual del usuario directamente.
     */
    suspend fun getCurrentLevel(): Int {
        return userLevelDao.getCurrentLevel() ?: 1
    }

    /**
     * Obtiene la experiencia actual del usuario.
     */
    suspend fun getCurrentXP(): Int {
        return userLevelDao.getCurrentXP() ?: 0
    }

    /**
     * Obtiene la experiencia necesaria para el próximo nivel.
     */
    suspend fun getXPToNextLevel(): Int {
        return userLevelDao.getXPToNextLevel() ?: 100
    }

    /**
     * Verifica si el usuario ha subido de nivel y obtiene el nivel actual.
     * @return Par con (subióDeNivel, nivelActual)
     */
    suspend fun checkLevelUp(addedXP: Int): Pair<Boolean, Int> {
        val level = userLevelDao.getUserLevelDirect() ?: return Pair(false, 1)

        // Calcular nuevo nivel
        val updatedLevel = LevelHelper.addExperience(level, addedXP)

        // Verificar si subió de nivel
        val leveledUp = updatedLevel.level > level.level

        return Pair(leveledUp, updatedLevel.level)
    }

    /**
     * Calcula el progreso actual del usuario hacia el siguiente nivel (0-100%).
     */
    suspend fun calculateLevelProgress(): Int {
        val level = userLevelDao.getUserLevelDirect() ?: return 0

        if (level.xpToNextLevel <= 0) return 100

        return ((level.currentXP.toFloat() / level.xpToNextLevel.toFloat()) * 100).toInt()
    }
}