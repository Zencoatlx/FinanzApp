package com.finanzapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: TransactionType,
    val iconName: String? = null,
    val colorCode: String? = null
)

enum class TransactionType {
    INCOME, EXPENSE
}