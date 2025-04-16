package com.finanzapp.data.repository

import androidx.lifecycle.LiveData
import com.finanzapp.data.dao.SavingGoalDao
import com.finanzapp.data.entity.SavingGoal

class SavingGoalRepository(private val savingGoalDao: SavingGoalDao) {
    val allSavingGoals: LiveData<List<SavingGoal>> = savingGoalDao.getAllSavingGoals()

    suspend fun insert(savingGoal: SavingGoal): Long {
        return savingGoalDao.insert(savingGoal)
    }

    suspend fun update(savingGoal: SavingGoal) {
        savingGoalDao.update(savingGoal)
    }

    suspend fun delete(savingGoal: SavingGoal) {
        savingGoalDao.delete(savingGoal)
    }

    suspend fun getSavingGoalById(id: Long): SavingGoal? {
        return savingGoalDao.getSavingGoalById(id)
    }
}