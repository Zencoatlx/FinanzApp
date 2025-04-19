package com.finanzapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.finanzapp.data.entity.Budget
import java.util.Date

@Dao
interface BudgetDao {
    @Insert
    suspend fun insert(budget: Budget): Long

    @Update
    suspend fun update(budget: Budget)

    @Delete
    suspend fun delete(budget: Budget)

    @Query("SELECT * FROM budgets ORDER BY date DESC")
    fun getAllBudgets(): LiveData<List<Budget>>

    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getBudgetById(id: Long): Budget?

    @Query("SELECT * FROM budgets WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getBudgetsByDateRange(startDate: Date, endDate: Date): LiveData<List<Budget>>

    @Query("SELECT * FROM budgets WHERE category = :category ORDER BY date DESC")
    fun getBudgetsByCategory(category: String): LiveData<List<Budget>>
}