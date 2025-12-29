package com.strategytracker.data.repository

import androidx.lifecycle.LiveData
import com.strategytracker.data.database.AppDatabase
import com.strategytracker.data.database.DailyPnLResult
import com.strategytracker.data.database.MonthlyPnLResult
import com.strategytracker.data.models.*
import kotlinx.coroutines.flow.Flow

class TradeRepository(private val database: AppDatabase) {
    
    private val strategyDao = database.strategyDao()
    private val tradeDao = database.tradeDao()
    private val overlayPositionDao = database.overlayPositionDao()
    
    // Strategy operations
    fun getAllStrategies(): Flow<List<Strategy>> = strategyDao.getAllStrategies()
    
    fun getAllStrategiesLiveData(): LiveData<List<Strategy>> = strategyDao.getAllStrategiesLiveData()
    
    suspend fun getStrategyById(strategyId: Long): Strategy? = strategyDao.getStrategyById(strategyId)
    
    fun getStrategyByIdLiveData(strategyId: Long): LiveData<Strategy?> = strategyDao.getStrategyByIdLiveData(strategyId)
    
    fun getStrategyByIdFlow(strategyId: Long): Flow<Strategy?> = strategyDao.getStrategyByIdFlow(strategyId)
    
    suspend fun insertStrategy(strategy: Strategy): Long = strategyDao.insertStrategy(strategy)
    
    suspend fun updateStrategy(strategy: Strategy) = strategyDao.updateStrategy(strategy)
    
    suspend fun deleteStrategy(strategy: Strategy) = strategyDao.deleteStrategy(strategy)
    
    suspend fun deleteStrategyById(strategyId: Long) = strategyDao.deleteStrategyById(strategyId)
    
    suspend fun updateDescription(strategyId: Long, description: String) = 
        strategyDao.updateDescription(strategyId, description)
    
    suspend fun updateProfitAmount(strategyId: Long, profitAmount: Double) = 
        strategyDao.updateProfitAmount(strategyId, profitAmount)
    
    suspend fun updateLossAmount(strategyId: Long, lossAmount: Double) = 
        strategyDao.updateLossAmount(strategyId, lossAmount)
    
    // Trade operations
    fun getTradesByStrategy(strategyId: Long): Flow<List<Trade>> = tradeDao.getTradesByStrategy(strategyId)
    
    fun getTradesByStrategyLiveData(strategyId: Long): LiveData<List<Trade>> = 
        tradeDao.getTradesByStrategyLiveData(strategyId)
    
    fun getCompletedTradesByStrategy(strategyId: Long): Flow<List<Trade>> = 
        tradeDao.getCompletedTradesByStrategy(strategyId)
    
    suspend fun getActiveTrade(strategyId: Long): Trade? = tradeDao.getActiveTrade(strategyId)
    
    suspend fun getAnyActiveTrade(): Trade? = tradeDao.getAnyActiveTrade()
    
    suspend fun getTradeById(tradeId: Long): Trade? = tradeDao.getTradeById(tradeId)
    
    suspend fun insertTrade(trade: Trade): Long = tradeDao.insertTrade(trade)
    
    suspend fun updateTrade(trade: Trade) = tradeDao.updateTrade(trade)
    
    suspend fun deleteTrade(trade: Trade) = tradeDao.deleteTrade(trade)
    
    suspend fun deleteTradeById(tradeId: Long) = tradeDao.deleteTradeById(tradeId)
    
    suspend fun closeTrade(tradeId: Long, exitDateTime: Long, result: TradeResult, pnlAmount: Double) = 
        tradeDao.closeTrade(tradeId, exitDateTime, result, pnlAmount)
    
    // Start a new trade
    suspend fun startTrade(strategyId: Long): Long {
        val trade = Trade(
            strategyId = strategyId,
            entryDateTime = System.currentTimeMillis(),
            isActive = true
        )
        return tradeDao.insertTrade(trade)
    }
    
    // End trade with result
    suspend fun endTrade(tradeId: Long, isWin: Boolean, strategy: Strategy) {
        val exitDateTime = System.currentTimeMillis()
        val result = if (isWin) TradeResult.WIN else TradeResult.LOSS
        val pnlAmount = if (isWin) strategy.profitAmount else -strategy.lossAmount
        tradeDao.closeTrade(tradeId, exitDateTime, result, pnlAmount)
    }
    
    // Statistics
    suspend fun getTradeStatistics(strategyId: Long): TradeStatistics {
        val totalTrades = tradeDao.getTotalTradesCount(strategyId)
        val wins = tradeDao.getWinsCount(strategyId)
        val losses = tradeDao.getLossesCount(strategyId)
        val totalPnL = tradeDao.getTotalPnL(strategyId)
        val winRate = if (totalTrades > 0) (wins.toDouble() / totalTrades) * 100 else 0.0
        
        return TradeStatistics(
            totalTrades = totalTrades,
            wins = wins,
            losses = losses,
            winRate = winRate,
            totalPnL = totalPnL
        )
    }
    
    suspend fun getDailyPnLForMonth(strategyId: Long): List<DailyPnLResult> = 
        tradeDao.getDailyPnLForMonth(strategyId)
    
    suspend fun getMonthlyPnLForYear(strategyId: Long): List<MonthlyPnLResult> = 
        tradeDao.getMonthlyPnLForYear(strategyId)
    
    suspend fun getAllCompletedTradesForExport(strategyId: Long): List<Trade> = 
        tradeDao.getAllCompletedTradesForExport(strategyId)
    
    // Overlay position operations
    suspend fun getPositionsForStrategy(strategyId: Long): List<OverlayPosition> = 
        overlayPositionDao.getPositionsForStrategy(strategyId)
    
    suspend fun getPositionForButton(strategyId: Long, buttonType: ButtonType): OverlayPosition? = 
        overlayPositionDao.getPositionForButton(strategyId, buttonType)
    
    suspend fun saveButtonPosition(strategyId: Long, buttonType: ButtonType, x: Int, y: Int) = 
        overlayPositionDao.saveOrUpdatePosition(strategyId, buttonType, x, y)
    
    suspend fun deletePositionsForStrategy(strategyId: Long) = 
        overlayPositionDao.deletePositionsForStrategy(strategyId)
}
