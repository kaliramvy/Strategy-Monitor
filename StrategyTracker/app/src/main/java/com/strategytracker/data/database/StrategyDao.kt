package com.strategytracker.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.strategytracker.data.models.Strategy
import kotlinx.coroutines.flow.Flow

@Dao
interface StrategyDao {
    
    @Query("SELECT * FROM strategies ORDER BY createdAt DESC")
    fun getAllStrategies(): Flow<List<Strategy>>
    
    @Query("SELECT * FROM strategies ORDER BY createdAt DESC")
    fun getAllStrategiesLiveData(): LiveData<List<Strategy>>
    
    @Query("SELECT * FROM strategies WHERE id = :strategyId")
    suspend fun getStrategyById(strategyId: Long): Strategy?
    
    @Query("SELECT * FROM strategies WHERE id = :strategyId")
    fun getStrategyByIdLiveData(strategyId: Long): LiveData<Strategy?>
    
    @Query("SELECT * FROM strategies WHERE id = :strategyId")
    fun getStrategyByIdFlow(strategyId: Long): Flow<Strategy?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStrategy(strategy: Strategy): Long
    
    @Update
    suspend fun updateStrategy(strategy: Strategy)
    
    @Delete
    suspend fun deleteStrategy(strategy: Strategy)
    
    @Query("DELETE FROM strategies WHERE id = :strategyId")
    suspend fun deleteStrategyById(strategyId: Long)
    
    @Query("UPDATE strategies SET description = :description, updatedAt = :updatedAt WHERE id = :strategyId")
    suspend fun updateDescription(strategyId: Long, description: String, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE strategies SET profitAmount = :profitAmount, updatedAt = :updatedAt WHERE id = :strategyId")
    suspend fun updateProfitAmount(strategyId: Long, profitAmount: Double, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE strategies SET lossAmount = :lossAmount, updatedAt = :updatedAt WHERE id = :strategyId")
    suspend fun updateLossAmount(strategyId: Long, lossAmount: Double, updatedAt: Long = System.currentTimeMillis())
}
