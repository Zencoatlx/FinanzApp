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

    fun getBudgetById(id: Long): LiveData<Budget> {
        return repository.getBudgetById(id)
    }
}