package com.finanzapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.finanzapp.data.AppDatabase
import com.finanzapp.data.entity.SavingGoal
import com.finanzapp.data.repository.SavingGoalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SavingGoalViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SavingGoalRepository
    val allSavingGoals: LiveData<List<SavingGoal>>

    init {
        val savingGoalDao = AppDatabase.getDatabase(application).savingGoalDao()
        repository = SavingGoalRepository(savingGoalDao)
        allSavingGoals = repository.allSavingGoals
    }

    // Modificamos este método para que sea suspend y devuelva el ID
    suspend fun insert(savingGoal: SavingGoal): Long = withContext(Dispatchers.IO) {
        return@withContext repository.insert(savingGoal)
    }

    fun update(savingGoal: SavingGoal) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(savingGoal)
    }

    fun delete(savingGoal: SavingGoal) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(savingGoal)
    }

    suspend fun getSavingGoalById(id: Long): SavingGoal? = withContext(Dispatchers.IO) {
        return@withContext repository.getSavingGoalById(id)
    }
}