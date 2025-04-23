package com.finanzapp.data.repository

import androidx.lifecycle.LiveData
import com.finanzapp.data.dao.SavingChallengeDao
import com.finanzapp.data.entity.SavingChallenge
import com.finanzapp.data.entity.ChallengeType
import java.util.Date

class SavingChallengeRepository(private val savingChallengeDao: SavingChallengeDao) {

    val allChallenges: LiveData<List<SavingChallenge>> = savingChallengeDao.getAllChallenges()
    val activeChallenges: LiveData<List<SavingChallenge>> = savingChallengeDao.getActiveChallenges()
    val completedChallenges: LiveData<List<SavingChallenge>> = savingChallengeDao.getCompletedChallenges()
    val completedChallengesCount: LiveData<Int> = savingChallengeDao.getCompletedChallengesCount()

    suspend fun insert(challenge: SavingChallenge): Long {
        return savingChallengeDao.insert(challenge)
    }

    suspend fun update(challenge: SavingChallenge) {
        savingChallengeDao.update(challenge)
    }

    suspend fun delete(challenge: SavingChallenge) {
        savingChallengeDao.delete(challenge)
    }

    suspend fun getChallengeById(id: Long): SavingChallenge? {
        return savingChallengeDao.getChallengeById(id)
    }

    fun getChallengesByType(type: ChallengeType): LiveData<List<SavingChallenge>> {
        return savingChallengeDao.getChallengesByType(type)
    }

    fun getAvailableChallenges(limit: Int = 10): LiveData<List<SavingChallenge>> {
        return savingChallengeDao.getAvailableChallenges(limit)
    }

    suspend fun updateChallengeProgress(challengeId: Long, amount: Double) {
        savingChallengeDao.updateChallengeProgress(challengeId, amount)

        // Verificar si se completó el desafío
        val challenge = savingChallengeDao.getChallengeById(challengeId)
        if (challenge != null && challenge.progress >= challenge.targetAmount) {
            savingChallengeDao.markChallengeAsCompleted(challengeId)
        }
    }

    suspend fun markChallengeAsCompleted(challengeId: Long) {
        savingChallengeDao.markChallengeAsCompleted(challengeId)
    }

    suspend fun activateChallenge(challenge: SavingChallenge, durationDays: Int): SavingChallenge {
        val startDate = Date()

        // Calcular fecha de finalización
        val calendar = java.util.Calendar.getInstance()
        calendar.time = startDate
        calendar.add(java.util.Calendar.DAY_OF_YEAR, durationDays)
        val endDate = calendar.time

        val updatedChallenge = challenge.copy(
            isActive = true,
            startDate = startDate,
            endDate = endDate
        )

        savingChallengeDao.update(updatedChallenge)
        return updatedChallenge
    }

    suspend fun abandonChallenge(challengeId: Long) {
        val challenge = savingChallengeDao.getChallengeById(challengeId)
        if (challenge != null) {
            val updatedChallenge = challenge.copy(
                isActive = false,
                progress = 0.0
            )
            savingChallengeDao.update(updatedChallenge)
        }
    }

    suspend fun checkCompletedChallenges(): List<SavingChallenge> {
        val active = savingChallengeDao.getActiveChallenges().value ?: emptyList()
        val completed = mutableListOf<SavingChallenge>()

        active.forEach { challenge ->
            if (challenge.progress >= challenge.targetAmount) {
                savingChallengeDao.markChallengeAsCompleted(challenge.id)

                val updatedChallenge = challenge.copy(isCompleted = true)
                completed.add(updatedChallenge)
            }
        }

        return completed
    }

    suspend fun getActiveChallengesTotalCount(): Int {
        return savingChallengeDao.getActiveChallengesTotalCount()
    }
}