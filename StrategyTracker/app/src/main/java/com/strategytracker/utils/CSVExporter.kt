package com.strategytracker.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.strategytracker.data.models.Trade
import com.strategytracker.data.models.TradeResult
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object CSVExporter {
    
    fun exportTrades(
        context: Context,
        strategyName: String,
        trades: List<Trade>
    ): Uri? {
        val fileName = "trades_${strategyName.replace(" ", "_")}_${System.currentTimeMillis()}.csv"
        val csvContent = buildCsvContent(trades)
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveToMediaStore(context, fileName, csvContent)
        } else {
            saveToExternalStorage(context, fileName, csvContent)
        }
    }
    
    private fun buildCsvContent(trades: List<Trade>): String {
        val sb = StringBuilder()
        
        // Header
        sb.append("Trade_No,Entry_DateTime,Exit_DateTime,Result,PnL_Amount\n")
        
        // Data rows
        trades.forEachIndexed { index, trade ->
            sb.append("${index + 1},")
            sb.append("${DateTimeUtils.formatForCsv(trade.entryDateTime)},")
            sb.append("${trade.exitDateTime?.let { DateTimeUtils.formatForCsv(it) } ?: ""},")
            sb.append("${trade.result?.name ?: ""},")
            sb.append("${formatPnL(trade.pnlAmount, trade.result)}\n")
        }
        
        return sb.toString()
    }
    
    private fun formatPnL(amount: Double?, result: TradeResult?): String {
        if (amount == null) return ""
        return when (result) {
            TradeResult.WIN -> "+${String.format("%.2f", amount)}"
            TradeResult.LOSS -> String.format("%.2f", amount)
            null -> String.format("%.2f", amount)
        }
    }
    
    private fun saveToMediaStore(context: Context, fileName: String, content: String): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        
        uri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                outputStream.write(content.toByteArray())
            }
        }
        
        return uri
    }
    
    private fun saveToExternalStorage(context: Context, fileName: String, content: String): Uri? {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }
        
        val file = File(downloadsDir, fileName)
        
        return try {
            FileOutputStream(file).use { outputStream ->
                outputStream.write(content.toByteArray())
            }
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun createShareIntent(context: Context, uri: Uri, fileName: String): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Trade Log Export - $fileName")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
