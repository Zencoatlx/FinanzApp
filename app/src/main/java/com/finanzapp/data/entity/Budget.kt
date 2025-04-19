package com.finanzapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,
    val amount: Double,
    val spent: Double? = 0.0,
    val date: Date,
    val months: Int = 1,
    val createdAt: Date = Date()
)