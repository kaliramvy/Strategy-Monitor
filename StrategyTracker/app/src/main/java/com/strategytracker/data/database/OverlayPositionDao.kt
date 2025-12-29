package com.strategytracker.data.database

import androidx.room.*
import com.strategytracker.data.models.ButtonType
import com.strategytracker.data.models.OverlayPosition

@Dao
interface OverlayPositionDao {
    
    @Query("SELECT * FROM overlay_positions WHERE strategyId = :strategyId")
    suspend fun getPositionsForStrategy(strategyId: Long): List<OverlayPosition>
    
    @Query("SELECT * FROM overlay_positions WHERE strategyId = :strategyId AND buttonType = :buttonType")
    suspend fun getPositionForButton(strategyId: Long, buttonType: ButtonType): OverlayPosition?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosition(position: OverlayPosition): Long
    
    @Update
    suspend fun updatePosition(position: OverlayPosition)
    
    @Query("UPDATE overlay_positions SET positionX = :x, positionY = :y WHERE strategyId = :strategyId AND buttonType = :buttonType")
    suspend fun updateButtonPosition(strategyId: Long, buttonType: ButtonType, x: Int, y: Int)
    
    @Query("DELETE FROM overlay_positions WHERE strategyId = :strategyId")
    suspend fun deletePositionsForStrategy(strategyId: Long)
    
    @Transaction
    suspend fun saveOrUpdatePosition(strategyId: Long, buttonType: ButtonType, x: Int, y: Int) {
        val existing = getPositionForButton(strategyId, buttonType)
        if (existing != null) {
            updateButtonPosition(strategyId, buttonType, x, y)
        } else {
            insertPosition(OverlayPosition(
                strategyId = strategyId,
                buttonType = buttonType,
                positionX = x,
                positionY = y
            ))
        }
    }
}
