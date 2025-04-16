package com.finanzapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.finanzapp.data.entity.SavingGoal

@Dao
interface SavingGoalDao {
    @Insert
    suspend fun insert(savingGoal: SavingGoal): Long

    @Update
    suspend fun update(savingGoal: SavingGoal)

    @Delete
    suspend fun delete(savingGoal: SavingGoal)

    @Query("SELECT * FROM saving_goals ORDER BY createdAt DESC")
    fun getAllSavingGoals(): LiveData<List<SavingGoal>>

    @Query("SELECT * FROM saving_goals WHERE id = :id")
    suspend fun getSavingGoalById(id: Long): SavingGoal?
}