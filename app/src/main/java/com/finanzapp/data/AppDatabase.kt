package com.finanzapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.finanzapp.data.converters.Converters
import com.finanzapp.data.dao.BudgetDao
import com.finanzapp.data.dao.CategoryDao
import com.finanzapp.data.dao.SavingGoalDao
import com.finanzapp.data.dao.TransactionDao
import com.finanzapp.data.entity.Budget
import com.finanzapp.data.entity.Category
import com.finanzapp.data.entity.SavingGoal
import com.finanzapp.data.entity.Transaction

@Database(
    entities = [Transaction::class, SavingGoal::class, Category::class, Budget::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun savingGoalDao(): SavingGoalDao
    abstract fun budgetDao(): BudgetDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finanzapp_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}