package com.strategytracker.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ButtonType {
    BLUE,
    GREEN,
    RED
}

@Entity(
    tableName = "overlay_positions",
    foreignKeys = [
        ForeignKey(
            entity = Strategy::class,
            parentColumns = ["id"],
            childColumns = ["strategyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["strategyId", "buttonType"], unique = true)]
)
data class OverlayPosition(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val strategyId: Long,
    val buttonType: ButtonType,
    val positionX: Int,
    val positionY: Int
)
