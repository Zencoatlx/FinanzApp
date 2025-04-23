package com.finanzapp.data.repository

import androidx.lifecycle.LiveData
import com.finanzapp.data.dao.AchievementDao
import com.finanzapp.data.entity.Achievement
import com.finanzapp.data.entity.AchievementCategory
import java.util.Date

class AchievementRepository(private val achievementDao: AchievementDao) {

    val allAchievements: LiveData<List<Achievement>> = achievementDao.getAllAchievements()
    val unlockedAchievements: LiveData<List<Achievement>> = achievementDao.getUnlockedAchievements()
    val lockedAchievements: LiveData<List<Achievement>> = achievementDao.getLockedAchievements()
    val unlockedCount: LiveData<Int> = achievementDao.getUnlockedAchievementsCount()
    val totalCount: LiveData<Int> = achievementDao.getTotalAchievementsCount()

    suspend fun insert(achievement: Achievement): Long {
        return achievementDao.insert(achievement)
    }

    suspend fun update(achievement: Achievement) {
        achievementDao.update(achievement)
    }

    suspend fun delete(achievement: Achievement) {
        achievementDao.delete(achievement)
    }

    suspend fun getAchievementById(id: Long): Achievement? {
        return achievementDao.getAchievementById(id)
    }

    fun getAchievementsByCategory(category: AchievementCategory): LiveData<List<Achievement>> {
        return achievementDao.getAchievementsByCategory(category)
    }

    suspend fun updateAchievementProgress(achievementId: Long, progressToAdd: Int): Boolean {
        achievementDao.updateAchievementProgress(achievementId, progressToAdd)

        // Verificar si se desbloqueó el logro
        val achievement = achievementDao.getAchievementById(achievementId)
        if (achievement != null && !achievement.isUnlocked && achievement.progress >= achievement.targetProgress) {
            unlockAchievement(achievementId)
            return true
        }
        return false
    }

    suspend fun unlockAchievement(achievementId: Long) {
        val now = Date().time
        achievementDao.unlockAchievement(achievementId, now)
    }

    fun getPendingAchievementsByCategory(category: AchievementCategory): LiveData<List<Achievement>> {
        return achievementDao.getPendingAchievementsByCategory(category)
    }

    /**
     * Verifica el progreso de los logros de una categoría específica
     * y actualiza su progreso según el valor proporcionado.
     */
    suspend fun checkAndUpdateCategoryAchievements(category: AchievementCategory, value: Int) {
        val achievements = achievementDao.getPendingAchievementsByCategory(category).value ?: return

        for (achievement in achievements) {
            // Actualizar el progreso basado en el contexto del logro
            when (achievement.category) {
                AchievementCategory.SAVINGS -> {
                    // Para logros de ahorro, value podría ser la cantidad ahorrada
                    updateAchievementProgress(achievement.id, value)
                }
                AchievementCategory.STREAKS -> {
                    // Para logros de rachas, value podría ser la racha actual
                    if (value > achievement.progress) {
                        // Sólo actualizamos si es mayor que el progreso anterior
                        achievementDao.update(achievement.copy(progress = value))

                        // Si alcanzó el objetivo, desbloqueamos
                        if (value >= achievement.targetProgress && !achievement.isUnlocked) {
                            unlockAchievement(achievement.id)
                        }
                    }
                }
                else -> {
                    // Para otros tipos de logros, simplemente incrementamos el progreso
                    updateAchievementProgress(achievement.id, 1)
                }
            }
        }
    }

    /**
     * Verifica y actualiza todos los logros pendientes
     * basados en valores agregados por el usuario.
     */
    suspend fun checkAllAchievements(savingsAmount: Double, streakDays: Int, budgetCompliance: Int) {
        // Verificar logros de ahorro
        checkAndUpdateCategoryAchievements(AchievementCategory.SAVINGS, savingsAmount.toInt())

        // Verificar logros de rachas
        checkAndUpdateCategoryAchievements(AchievementCategory.STREAKS, streakDays)

        // Verificar logros de presupuesto
        checkAndUpdateCategoryAchievements(AchievementCategory.BUDGET, budgetCompliance)
    }
}