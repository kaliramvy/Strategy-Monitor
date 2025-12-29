package com.strategytracker.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TradeResult {
    WIN,
    LOSS
}

@Entity(
    tableName = "trades",
    foreignKeys = [
        ForeignKey(
            entity = Strategy::class,
            parentColumns = ["id"],
            childColumns = ["strategyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["strategyId"])]
)
data class Trade(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val strategyId: Long,
    val entryDateTime: Long,
    val exitDateTime: Long? = null,
    val result: TradeResult? = null,
    val pnlAmount: Double? = null,
    val isActive: Boolean = true
)

// Data class for statistics
data class TradeStatistics(
    val totalTrades: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val winRate: Double = 0.0,
    val totalPnL: Double = 0.0
)

// Data class for daily P&L
data class DailyPnL(
    val date: String,
    val pnl: Double
)

// Data class for monthly P&L
data class MonthlyPnL(
    val month: Int,
    val year: Int,
    val pnl: Double
)
