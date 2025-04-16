package com.finanzapp.ui.viewmodel


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.finanzapp.data.AppDatabase
import com.finanzapp.data.entity.Transaction
import com.finanzapp.data.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TransactionRepository
    val allTransactions: LiveData<List<Transaction>>
    val allIncome: LiveData<List<Transaction>>
    val allExpenses: LiveData<List<Transaction>>

    init {
        val transactionDao = AppDatabase.getDatabase(application).transactionDao()
        repository = TransactionRepository(transactionDao)
        allTransactions = repository.allTransactions
        allIncome = repository.allIncome
        allExpenses = repository.allExpenses
    }

    fun insert(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(transaction)
    }

    fun update(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(transaction)
    }

    fun delete(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(transaction)
    }

    fun getTransactionsByDateRange(startDate: Date, endDate: Date): LiveData<List<Transaction>> {
        return repository.getTransactionsByDateRange(startDate, endDate)
    }

    fun getTransactionsByCategory(category: String): LiveData<List<Transaction>> {
        return repository.getTransactionsByCategory(category)
    }
}