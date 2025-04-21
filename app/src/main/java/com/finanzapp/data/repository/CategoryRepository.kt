package com.finanzapp.data.repository

import androidx.lifecycle.LiveData
import com.finanzapp.data.dao.CategoryDao
import com.finanzapp.data.entity.Category
import com.finanzapp.data.entity.TransactionType

class CategoryRepository(private val categoryDao: CategoryDao) {
    val allCategories: LiveData<List<Category>> = categoryDao.getAllCategories()

    fun getCategoriesByType(type: TransactionType): LiveData<List<Category>> {
        return categoryDao.getCategoriesByType(type.ordinal)
    }

    suspend fun insert(category: Category): Long {
        return categoryDao.insert(category)
    }

    suspend fun update(category: Category) {
        categoryDao.update(category)
    }

    suspend fun delete(category: Category) {
        categoryDao.delete(category)
    }
}