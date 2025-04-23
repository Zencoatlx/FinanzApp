package com.finanzapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.finanzapp.data.entity.Achievement
import com.finanzapp.data.entity.AchievementCategory

@Dao
interface AchievementDao {
    @Insert
    suspend fun insert(achievement: Achievement): Long

    @Update
    suspend fun update(achievement: Achievement)

    @Delete
    suspend fun delete(achievement: Achievement)

    @Query("SELECT * FROM achievements ORDER BY category, tier")
    fun getAllAchievements(): LiveData<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE isUnlocked = 1 ORDER BY unlockedDate DESC")
    fun getUnlockedAchievements(): LiveData<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE isUnlocked = 0")
    fun getLockedAchievements(): LiveData<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE category = :category")
    fun getAchievementsByCategory(category: AchievementCategory): LiveData<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE id = :id")
    suspend fun getAchievementById(id: Long): Achievement?

    @Query("UPDATE achievements SET progress = progress + :amount WHERE id = :achievementId")
    suspend fun updateAchievementProgress(achievementId: Long, amount: Int)

    @Query("UPDATE achievements SET isUnlocked = 1, unlockedDate = :date WHERE id = :achievementId")
    suspend fun unlockAchievement(achievementId: Long, date: Long)

    @Query("SELECT COUNT(*) FROM achievements WHERE isUnlocked = 1")
    fun getUnlockedAchievementsCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM achievements")
    fun getTotalAchievementsCount(): LiveData<Int>

    // Queries específicas para verificar logros
    @Query("SELECT * FROM achievements WHERE category = :category AND progress < targetProgress AND isUnlocked = 0")
    fun getPendingAchievementsByCategory(category: AchievementCategory): LiveData<List<Achievement>>
}