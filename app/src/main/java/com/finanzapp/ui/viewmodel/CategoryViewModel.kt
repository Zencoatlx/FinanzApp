package com.finanzapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.finanzapp.data.AppDatabase
import com.finanzapp.data.entity.Category
import com.finanzapp.data.entity.TransactionType
import com.finanzapp.data.repository.CategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CategoryRepository
    val allCategories: LiveData<List<Category>>

    init {
        val categoryDao = AppDatabase.getDatabase(application).categoryDao()
        repository = CategoryRepository(categoryDao)
        allCategories = repository.allCategories
    }

    fun getCategoriesByType(type: TransactionType): LiveData<List<Category>> {
        return repository.getCategoriesByType(type)
    }

    fun insert(category: Category) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(category)
    }

    fun update(category: Category) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(category)
    }

    fun delete(category: Category) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(category)
    }
}