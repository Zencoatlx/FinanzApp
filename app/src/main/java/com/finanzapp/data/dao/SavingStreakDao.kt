package com.finanzapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.finanzapp.data.entity.SavingStreak
import java.util.Date

@Dao
interface SavingStreakDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(streak: SavingStreak): Long

    @Update
    suspend fun update(streak: SavingStreak)

    @Query("SELECT * FROM saving_streaks WHERE id = 1")
    fun getSavingStreak(): LiveData<SavingStreak?>

    @Query("SELECT * FROM saving_streaks WHERE id = 1")
    suspend fun getSavingStreakDirect(): SavingStreak?

    @Query("UPDATE saving_streaks SET currentStreak = :streak WHERE id = 1")
    suspend fun updateCurrentStreak(streak: Int)

    @Query("UPDATE saving_streaks SET isActiveToday = 0 WHERE id = 1")
    suspend fun resetDailyStatus()

    @Query("UPDATE saving_streaks SET lastSavingDate = :date, isActiveToday = 1 WHERE id = 1")
    suspend fun updateLastSavingDate(date: Date)

    @Query("UPDATE saving_streaks SET dailyTarget = :amount WHERE id = 1")
    suspend fun updateDailyTarget(amount: Double)

    @Query("SELECT COUNT(*) FROM saving_streaks WHERE currentStreak > 0")
    suspend fun hasActiveStreak(): Int

    // Verifica si se ha registrado actividad hoy
    @Query("SELECT isActiveToday FROM saving_streaks WHERE id = 1")
    suspend fun isActiveToday(): Boolean?
}