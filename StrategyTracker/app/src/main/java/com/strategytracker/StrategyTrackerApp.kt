package com.strategytracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.strategytracker.data.database.AppDatabase
import com.strategytracker.data.repository.TradeRepository

class StrategyTrackerApp : Application() {
    
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val repository: TradeRepository by lazy { TradeRepository(database) }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                OVERLAY_CHANNEL_ID,
                "Trade Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows when trade overlay buttons are active"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    companion object {
        const val OVERLAY_CHANNEL_ID = "overlay_channel"
        const val OVERLAY_NOTIFICATION_ID = 1001
        
        @Volatile
        private var instance: StrategyTrackerApp? = null
        
        fun getInstance(): StrategyTrackerApp {
            return instance ?: throw IllegalStateException("Application not initialized")
        }
    }
}
