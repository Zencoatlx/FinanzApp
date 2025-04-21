package com.finanzapp.data.repository

import androidx.lifecycle.LiveData
import com.finanzapp.data.dao.BudgetDao
import com.finanzapp.data.entity.Budget

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

    fun getBudgetById(id: Long): LiveData<Budget> {
        return budgetDao.getBudgetById(id)
    }
}