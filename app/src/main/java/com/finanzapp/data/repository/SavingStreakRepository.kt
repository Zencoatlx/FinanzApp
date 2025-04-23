package com.finanzapp.data.repository

import androidx.lifecycle.LiveData
import com.finanzapp.data.dao.SavingStreakDao
import com.finanzapp.data.entity.SavingStreak
import com.finanzapp.data.entity.StreakHelper
import java.util.Date
import java.util.Calendar

class SavingStreakRepository(private val savingStreakDao: SavingStreakDao) {

    val savingStreak: LiveData<SavingStreak?> = savingStreakDao.getSavingStreak()

    suspend fun initialize() {
        // Verificar si ya existe
        val existingStreak = savingStreakDao.getSavingStreakDirect()
        if (existingStreak == null) {
            // Crear nuevo registro de racha
            val newStreak = SavingStreak(id = 1)
            savingStreakDao.insert(newStreak)
        }
    }

    suspend fun update(streak: SavingStreak) {
        savingStreakDao.update(streak)
    }

    /**
     * Registra una nueva actividad de ahorro y actualiza la racha.
     * @param savingAmount Cantidad ahorrada
     * @return La racha actualizada
     */
    suspend fun registerSavingActivity(savingAmount: Double): SavingStreak {
        // Obtener la racha actual
        var streak = savingStreakDao.getSavingStreakDirect() ?: SavingStreak(id = 1)

        // Verificar si el ahorro cumple con el mínimo diario
        if (savingAmount >= streak.dailyTarget) {
            // Actualizar la racha
            streak = StreakHelper.checkAndUpdateStreak(streak, savingAmount)
            savingStreakDao.update(streak)
        }

        return streak
    }

    /**
     * Verifica si la racha debe romperse por falta de actividad.
     * Esta función debe llamarse una vez al día, preferiblemente a medianoche.
     */
    suspend fun checkAndResetStreak() {
        val streak = savingStreakDao.getSavingStreakDirect() ?: return

        // Verificar si la última actividad fue ayer o antes
        val lastDate = streak.lastSavingDate ?: return

        val today = Calendar.getInstance()
        val lastActivityDay = Calendar.getInstance().apply { time = lastDate }

        // Borrar la hora para comparar solo fechas
        clearTimeFromCalendar(today)
        clearTimeFromCalendar(lastActivityDay)

        // Si la última actividad no fue ayer, rompemos la racha
        val oneDayInMillis = 24 * 60 * 60 * 1000
        if (today.timeInMillis - lastActivityDay.timeInMillis > oneDayInMillis) {
            // Romper la racha si ha pasado más de un día
            val updatedStreak = streak.copy(
                currentStreak = 0,
                streakStartDate = null,
                isActiveToday = false
            )
            savingStreakDao.update(updatedStreak)
        }

        // Siempre reseteamos el estado diario a medianoche
        savingStreakDao.resetDailyStatus()
    }

    /**
     * Actualiza el objetivo diario mínimo para mantener la racha.
     */
    suspend fun updateDailyTarget(amount: Double) {
        savingStreakDao.updateDailyTarget(amount)
    }

    /**
     * Verifica si ya se ha registrado actividad de ahorro hoy.
     */
    suspend fun isActiveToday(): Boolean {
        return savingStreakDao.isActiveToday() ?: false
    }

    /**
     * Obtiene la racha actual directamente (no como LiveData).
     */
    suspend fun getCurrentStreakDirect(): Int {
        val streak = savingStreakDao.getSavingStreakDirect()
        return streak?.currentStreak ?: 0
    }

    /**
     * Obtiene el récord personal (racha más larga).
     */
    suspend fun getLongestStreak(): Int {
        val streak = savingStreakDao.getSavingStreakDirect()
        return streak?.longestStreak ?: 0
    }

    /**
     * Retorna los días totales con actividad de ahorro.
     */
    suspend fun getTotalSavingDays(): Int {
        val streak = savingStreakDao.getSavingStreakDirect()
        return streak?.totalSavingDays ?: 0
    }

    /**
     * Calcula el multiplicador de experiencia basado en la racha actual.
     * Una racha más larga proporciona más XP por actividades.
     */
    suspend fun calculateStreakBonus(): Double {
        val streak = savingStreakDao.getSavingStreakDirect() ?: return 1.0

        return when {
            streak.currentStreak >= 30 -> 2.0   // 100% bonus después de 30 días
            streak.currentStreak >= 14 -> 1.5   // 50% bonus después de 14 días
            streak.currentStreak >= 7 -> 1.25  // 25% bonus después de 7 días
            streak.currentStreak >= 3 -> 1.1   // 10% bonus después de 3 días
            else -> 1.0
        }
    }

    private fun clearTimeFromCalendar(calendar: Calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
    }
}