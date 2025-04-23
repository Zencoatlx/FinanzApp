package com.finanzapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.finanzapp.data.entity.SavingChallenge
import com.finanzapp.data.entity.ChallengeType

@Dao
interface SavingChallengeDao {
    @Insert
    suspend fun insert(challenge: SavingChallenge): Long

    @Update
    suspend fun update(challenge: SavingChallenge)

    @Delete
    suspend fun delete(challenge: SavingChallenge)

    @Query("SELECT * FROM saving_challenges ORDER BY createdAt DESC")
    fun getAllChallenges(): LiveData<List<SavingChallenge>>

    @Query("SELECT * FROM saving_challenges WHERE id = :id")
    suspend fun getChallengeById(id: Long): SavingChallenge?

    @Query("SELECT * FROM saving_challenges WHERE isActive = 1 ORDER BY endDate ASC")
    fun getActiveChallenges(): LiveData<List<SavingChallenge>>

    @Query("SELECT * FROM saving_challenges WHERE isCompleted = 1 ORDER BY endDate DESC")
    fun getCompletedChallenges(): LiveData<List<SavingChallenge>>

    @Query("SELECT * FROM saving_challenges WHERE type = :type AND isActive = 1")
    fun getChallengesByType(type: ChallengeType): LiveData<List<SavingChallenge>>

    @Query("SELECT * FROM saving_challenges WHERE isActive = 0 AND isCompleted = 0 ORDER BY difficulty ASC LIMIT :limit")
    fun getAvailableChallenges(limit: Int = 10): LiveData<List<SavingChallenge>>

    @Query("UPDATE saving_challenges SET progress = progress + :amount WHERE id = :challengeId")
    suspend fun updateChallengeProgress(challengeId: Long, amount: Double)

    @Query("UPDATE saving_challenges SET isCompleted = 1 WHERE id = :challengeId")
    suspend fun markChallengeAsCompleted(challengeId: Long)

    @Query("SELECT COUNT(*) FROM saving_challenges WHERE isActive = 1")
    suspend fun getActiveChallengesTotalCount(): Int

    @Query("SELECT COUNT(*) FROM saving_challenges WHERE isCompleted = 1")
    fun getCompletedChallengesCount(): LiveData<Int>
}