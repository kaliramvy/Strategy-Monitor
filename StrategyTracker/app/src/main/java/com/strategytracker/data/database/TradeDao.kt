package com.strategytracker.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.strategytracker.data.models.Trade
import com.strategytracker.data.models.TradeResult
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeDao {
    
    @Query("SELECT * FROM trades WHERE strategyId = :strategyId ORDER BY entryDateTime DESC")
    fun getTradesByStrategy(strategyId: Long): Flow<List<Trade>>
    
    @Query("SELECT * FROM trades WHERE strategyId = :strategyId ORDER BY entryDateTime DESC")
    fun getTradesByStrategyLiveData(strategyId: Long): LiveData<List<Trade>>
    
    @Query("SELECT * FROM trades WHERE strategyId = :strategyId AND isActive = 0 ORDER BY entryDateTime DESC")
    fun getCompletedTradesByStrategy(strategyId: Long): Flow<List<Trade>>
    
    @Query("SELECT * FROM trades WHERE strategyId = :strategyId AND isActive = 1 LIMIT 1")
    suspend fun getActiveTrade(strategyId: Long): Trade?
    
    @Query("SELECT * FROM trades WHERE isActive = 1 LIMIT 1")
    suspend fun getAnyActiveTrade(): Trade?
    
    @Query("SELECT * FROM trades WHERE id = :tradeId")
    suspend fun getTradeById(tradeId: Long): Trade?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: Trade): Long
    
    @Update
    suspend fun updateTrade(trade: Trade)
    
    @Delete
    suspend fun deleteTrade(trade: Trade)
    
    @Query("DELETE FROM trades WHERE id = :tradeId")
    suspend fun deleteTradeById(tradeId: Long)
    
    // Close trade with result
    @Query("UPDATE trades SET exitDateTime = :exitDateTime, result = :result, pnlAmount = :pnlAmount, isActive = 0 WHERE id = :tradeId")
    suspend fun closeTrade(tradeId: Long, exitDateTime: Long, result: TradeResult, pnlAmount: Double)
    
    // Statistics queries
    @Query("SELECT COUNT(*) FROM trades WHERE strategyId = :strategyId AND isActive = 0")
    suspend fun getTotalTradesCount(strategyId: Long): Int
    
    @Query("SELECT COUNT(*) FROM trades WHERE strategyId = :strategyId AND result = 'WIN' AND isActive = 0")
    suspend fun getWinsCount(strategyId: Long): Int
    
    @Query("SELECT COUNT(*) FROM trades WHERE strategyId = :strategyId AND result = 'LOSS' AND isActive = 0")
    suspend fun getLossesCount(strategyId: Long): Int
    
    @Query("SELECT COALESCE(SUM(pnlAmount), 0.0) FROM trades WHERE strategyId = :strategyId AND isActive = 0")
    suspend fun getTotalPnL(strategyId: Long): Double
    
    // Daily P&L for current month
    @Query("""
        SELECT date(entryDateTime / 1000, 'unixepoch', 'localtime') as date, 
               SUM(pnlAmount) as pnl 
        FROM trades 
        WHERE strategyId = :strategyId 
          AND isActive = 0 
          AND strftime('%Y-%m', entryDateTime / 1000, 'unixepoch', 'localtime') = strftime('%Y-%m', :currentTime / 1000, 'unixepoch', 'localtime')
        GROUP BY date(entryDateTime / 1000, 'unixepoch', 'localtime')
        ORDER BY date
    """)
    suspend fun getDailyPnLForMonth(strategyId: Long, currentTime: Long = System.currentTimeMillis()): List<DailyPnLResult>
    
    // Monthly P&L for current year
    @Query("""
        SELECT CAST(strftime('%m', entryDateTime / 1000, 'unixepoch', 'localtime') AS INTEGER) as month,
               CAST(strftime('%Y', entryDateTime / 1000, 'unixepoch', 'localtime') AS INTEGER) as year,
               SUM(pnlAmount) as pnl 
        FROM trades 
        WHERE strategyId = :strategyId 
          AND isActive = 0 
          AND strftime('%Y', entryDateTime / 1000, 'unixepoch', 'localtime') = strftime('%Y', :currentTime / 1000, 'unixepoch', 'localtime')
        GROUP BY month, year
        ORDER BY month
    """)
    suspend fun getMonthlyPnLForYear(strategyId: Long, currentTime: Long = System.currentTimeMillis()): List<MonthlyPnLResult>
    
    // All trades for CSV export
    @Query("SELECT * FROM trades WHERE strategyId = :strategyId AND isActive = 0 ORDER BY entryDateTime ASC")
    suspend fun getAllCompletedTradesForExport(strategyId: Long): List<Trade>
}

// Helper data classes for query results
data class DailyPnLResult(
    val date: String,
    val pnl: Double
)

data class MonthlyPnLResult(
    val month: Int,
    val year: Int,
    val pnl: Double
)
