package com.finanzapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.finanzapp.data.AppDatabase
import com.finanzapp.data.entity.Budget
import com.finanzapp.data.repository.BudgetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class BudgetViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: BudgetRepository
    val allBudgets: LiveData<List<Budget>>

    init {
        val budgetDao = AppDatabase.getDatabase(application).budgetDao()
        repository = BudgetRepository(budgetDao)
        allBudgets = repository.allBudgets
    }

    fun insert(budget: Budget) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(budget)
    }

    fun update(budget: Budget) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(budget)
    }

    fun delete(budget: Budget) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(budget)
    }

    suspend fun getBudgetById(id: Long): Budget? = withContext(Dispatchers.IO) {
        return@withContext repository.getBudgetById(id)
    }

    fun getBudgetsByDateRange(startDate: Date, endDate: Date): LiveData<List<Budget>> {
        return repository.getBudgetsByDateRange(startDate, endDate)
    }

    fun getBudgetsByCategory(category: String): LiveData<List<Budget>> {
        return repository.getBudgetsByCategory(category)
    }
}