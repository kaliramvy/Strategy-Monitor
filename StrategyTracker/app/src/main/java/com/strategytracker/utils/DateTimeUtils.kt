package com.strategytracker.utils

import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtils {
    
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val displayDateTimeFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val displayTimeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val csvDateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    fun formatDateTime(timestamp: Long): String {
        return dateTimeFormat.format(Date(timestamp))
    }
    
    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }
    
    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }
    
    fun formatDisplayDateTime(timestamp: Long): String {
        return displayDateTimeFormat.format(Date(timestamp))
    }
    
    fun formatDisplayDate(timestamp: Long): String {
        return displayDateFormat.format(Date(timestamp))
    }
    
    fun formatDisplayTime(timestamp: Long): String {
        return displayTimeFormat.format(Date(timestamp))
    }
    
    fun formatForCsv(timestamp: Long): String {
        return csvDateTimeFormat.format(Date(timestamp))
    }
    
    fun getCurrentMonthYear(): String {
        val format = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return format.format(Date())
    }
    
    fun getCurrentYear(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.YEAR)
    }
    
    fun getCurrentMonth(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.MONTH) + 1
    }
    
    fun getDaysInCurrentMonth(): Int {
        val calendar = Calendar.getInstance()
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    
    fun getMonthName(month: Int): String {
        val monthNames = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        return if (month in 1..12) monthNames[month - 1] else ""
    }
    
    fun getDayOfMonth(timestamp: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.DAY_OF_MONTH)
    }
}
