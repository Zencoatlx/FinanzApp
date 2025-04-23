package com.finanzapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entidad que registra las rachas de ahorro del usuario.
 * Las rachas motivan al usuario a mantener constancia en sus hábitos de ahorro.
 */
@Entity(tableName = "saving_streaks")
data class SavingStreak(
    @PrimaryKey
    val id: Long = 1, // Solo habrá un registro por usuario
    val currentStreak: Int = 0, // Días consecutivos actuales
    val longestStreak: Int = 0, // Récord personal
    val totalSavingDays: Int = 0, // Total de días con actividad de ahorro
    val lastSavingDate: Date? = null, // Última fecha de registro de ahorro
    val streakStartDate: Date? = null, // Fecha de inicio de la racha actual
    val dailyTarget: Double = 0.0, // Objetivo diario mínimo para mantener la racha
    val weeklyStreak: Int = 0, // Semanas consecutivas
    val monthlyStreak: Int = 0, // Meses consecutivos
    val isActiveToday: Boolean = false, // Si ya se registró ahorro hoy
    val lastUpdated: Date = Date()
)

/**
 * Clase de utilidad para trabajar con SavingStreak
 */
object StreakHelper {

    /**
     * Verifica si se debe incrementar la racha actual
     * @param streak Objeto SavingStreak actual
     * @param savingAmount Cantidad ahorrada
     * @return SavingStreak actualizado
     */
    fun checkAndUpdateStreak(streak: SavingStreak, savingAmount: Double): SavingStreak {
        val today = Date()

        // Si no hay fecha previa, iniciamos la racha
        if (streak.lastSavingDate == null) {
            return streak.copy(
                currentStreak = 1,
                longestStreak = 1,
                totalSavingDays = 1,
                lastSavingDate = today,
                streakStartDate = today,
                isActiveToday = true
            )
        }

        // Si ya se registró ahorro hoy, solo actualizamos el estado
        if (isDateToday(streak.lastSavingDate) && streak.isActiveToday) {
            return streak
        }

        // Si se ahorra hoy y ayer fue el último día de ahorro, incrementamos la racha
        if (isConsecutiveDay(streak.lastSavingDate, today)) {
            val newStreak = streak.currentStreak + 1
            return streak.copy(
                currentStreak = newStreak,
                longestStreak = maxOf(newStreak, streak.longestStreak),
                totalSavingDays = streak.totalSavingDays + 1,
                lastSavingDate = today,
                isActiveToday = true,
                lastUpdated = today,
                weeklyStreak = calculateWeeklyStreak(streak, today),
                monthlyStreak = calculateMonthlyStreak(streak, today)
            )
        }

        // Si se rompe la racha, iniciamos una nueva
        return streak.copy(
            currentStreak = 1,
            totalSavingDays = streak.totalSavingDays + 1,
            lastSavingDate = today,
            streakStartDate = today,
            isActiveToday = true,
            lastUpdated = today
        )
    }

    /**
     * Verifica si dos fechas son consecutivas (un día después)
     */
    private fun isConsecutiveDay(lastDate: Date, currentDate: Date): Boolean {
        // Implementación simplificada para verificar días consecutivos
        val diff = currentDate.time - lastDate.time
        val dayInMillis = 24 * 60 * 60 * 1000L
        return diff in 1..dayInMillis
    }

    /**
     * Verifica si una fecha es hoy
     */
    private fun isDateToday(date: Date): Boolean {
        val today = Date()
        return date.year == today.year &&
                date.month == today.month &&
                date.date == today.date
    }

    /**
     * Calcula la racha semanal
     */
    private fun calculateWeeklyStreak(streak: SavingStreak, today: Date): Int {
        // TODO: Implementar lógica para racha semanal
        return streak.weeklyStreak
    }

    /**
     * Calcula la racha mensual
     */
    private fun calculateMonthlyStreak(streak: SavingStreak, today: Date): Int {
        // TODO: Implementar lógica para racha mensual
        return streak.monthlyStreak
    }
}