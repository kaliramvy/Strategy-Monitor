package com.strategytracker.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "strategies")
data class Strategy(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val profitAmount: Double = 0.0,
    val lossAmount: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
