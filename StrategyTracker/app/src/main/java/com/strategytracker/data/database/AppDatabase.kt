package com.strategytracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.strategytracker.data.models.ButtonType
import com.strategytracker.data.models.OverlayPosition
import com.strategytracker.data.models.Strategy
import com.strategytracker.data.models.Trade
import com.strategytracker.data.models.TradeResult

class Converters {
    @TypeConverter
    fun fromTradeResult(value: TradeResult?): String? {
        return value?.name
    }

    @TypeConverter
    fun toTradeResult(value: String?): TradeResult? {
        return value?.let { TradeResult.valueOf(it) }
    }

    @TypeConverter
    fun fromButtonType(value: ButtonType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toButtonType(value: String?): ButtonType? {
        return value?.let { ButtonType.valueOf(it) }
    }
}

@Database(
    entities = [Strategy::class, Trade::class, OverlayPosition::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun strategyDao(): StrategyDao
    abstract fun tradeDao(): TradeDao
    abstract fun overlayPositionDao(): OverlayPositionDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "strategy_tracker_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
