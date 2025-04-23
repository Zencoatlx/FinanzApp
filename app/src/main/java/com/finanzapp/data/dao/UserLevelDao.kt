package com.finanzapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.finanzapp.data.entity.UserLevel

@Dao
interface UserLevelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userLevel: UserLevel): Long

    @Update
    suspend fun update(userLevel: UserLevel)

    @Query("SELECT * FROM user_levels WHERE id = 1")
    fun getUserLevel(): LiveData<UserLevel?>

    @Query("SELECT * FROM user_levels WHERE id = 1")
    suspend fun getUserLevelDirect(): UserLevel?

    @Query("UPDATE user_levels SET currentXP = currentXP + :amount WHERE id = 1")
    suspend fun addExperience(amount: Int)

    @Query("UPDATE user_levels SET level = :level, currentXP = :xp, xpToNextLevel = :nextLevelXp WHERE id = 1")
    suspend fun updateLevel(level: Int, xp: Int, nextLevelXp: Int)

    @Query("UPDATE user_levels SET rankPoints = rankPoints + :points WHERE id = 1")
    suspend fun addRankPoints(points: Int)

    @Query("UPDATE user_levels SET streakBonus = :bonus WHERE id = 1")
    suspend fun updateStreakBonus(bonus: Double)

    // Para obtener información rápida
    @Query("SELECT level FROM user_levels WHERE id = 1")
    suspend fun getCurrentLevel(): Int?

    @Query("SELECT currentXP FROM user_levels WHERE id = 1")
    suspend fun getCurrentXP(): Int?

    @Query("SELECT xpToNextLevel FROM user_levels WHERE id = 1")
    suspend fun getXPToNextLevel(): Int?
}