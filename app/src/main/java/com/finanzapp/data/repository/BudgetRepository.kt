package com.finanzapp.data.repository

import androidx.lifecycle.LiveData
import com.finanzapp.data.dao.BudgetDao
import com.finanzapp.data.entity.Budget
import java.util.Date

class BudgetRepository(private val budgetDao: BudgetDao) {
    val allBudgets: LiveData<List<Budget>> = budgetDao.getAllBudgets()

    suspend fun insert(budget: Budget): Long {
        return budgetDao.insert(budget)
    }

    suspend fun update(budget: Budget) {
        budgetDao.update(budget)
    }

    suspend fun delete(budget: Budget) {
        budgetDao.delete(budget)
    }

    suspend fun getBudgetById(id: Long): Budget? {
        return budgetDao.getBudgetById(id)
    }

    fun getBudgetsByDateRange(startDate: Date, endDate: Date): LiveData<List<Budget>> {
        return budgetDao.getBudgetsByDateRange(startDate, endDate)
    }

    fun getBudgetsByCategory(category: String): LiveData<List<Budget>> {
        return budgetDao.getBudgetsByCategory(category)
    }
}