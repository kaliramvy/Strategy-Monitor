package com.strategytracker.overlay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Check if we have overlay permission
            if (Settings.canDrawOverlays(context)) {
                // Check shared preferences for last active strategy
                val prefs = context.getSharedPreferences("overlay_prefs", Context.MODE_PRIVATE)
                val lastStrategyId = prefs.getLong("last_strategy_id", -1)
                val wasActive = prefs.getBoolean("was_active", false)
                
                if (wasActive && lastStrategyId != -1L) {
                    OverlayService.start(context, lastStrategyId)
                }
            }
        }
    }
}
