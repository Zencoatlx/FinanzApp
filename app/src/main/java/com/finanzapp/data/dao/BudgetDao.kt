package com.finanzapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.finanzapp.data.entity.Budget

@Dao
interface BudgetDao {
    @Insert
    suspend fun insert(budget: Budget): Long

    @Update
    suspend fun update(budget: Budget)

    @Delete
    suspend fun delete(budget: Budget)

    @Query("SELECT * FROM budgets ORDER BY createdAt DESC")
    fun getAllBudgets(): LiveData<List<Budget>>

    @Query("SELECT * FROM budgets WHERE id = :id")
    fun getBudgetById(id: Long): LiveData<Budget>
}