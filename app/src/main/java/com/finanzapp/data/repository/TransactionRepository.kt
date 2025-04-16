package com.finanzapp.data.repository

import androidx.lifecycle.LiveData
import com.finanzapp.data.dao.TransactionDao
import com.finanzapp.data.entity.Transaction
import java.util.Date

class TransactionRepository(private val transactionDao: TransactionDao) {
    val allTransactions: LiveData<List<Transaction>> = transactionDao.getAllTransactions()
    val allIncome: LiveData<List<Transaction>> = transactionDao.getAllIncome()
    val allExpenses: LiveData<List<Transaction>> = transactionDao.getAllExpenses()

    suspend fun insert(transaction: Transaction): Long {
        return transactionDao.insert(transaction)
    }

    suspend fun update(transaction: Transaction) {
        transactionDao.update(transaction)
    }

    suspend fun delete(transaction: Transaction) {
        transactionDao.delete(transaction)
    }

    fun getTransactionsByDateRange(startDate: Date, endDate: Date): LiveData<List<Transaction>> {
        return transactionDao.getTransactionsByDateRange(startDate, endDate)
    }

    fun getTransactionsByCategory(category: String): LiveData<List<Transaction>> {
        return transactionDao.getTransactionsByCategory(category)
    }
}