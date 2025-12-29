package com.strategytracker.ui.detail

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.*
import com.strategytracker.data.models.Strategy
import com.strategytracker.data.models.Trade
import com.strategytracker.data.models.TradeStatistics
import com.strategytracker.data.repository.TradeRepository
import com.strategytracker.utils.CSVExporter
import kotlinx.coroutines.launch

class StrategyDetailViewModel(
    private val repository: TradeRepository,
    private val strategyId: Long
) : ViewModel() {
    
    val strategy: LiveData<Strategy?> = repository.getStrategyByIdLiveData(strategyId)
    val trades: LiveData<List<Trade>> = repository.getTradesByStrategyLiveData(strategyId)
    
    private val _statistics = MutableLiveData<TradeStatistics>()
    val statistics: LiveData<TradeStatistics> = _statistics
    
    private val _cumulativePnL = MutableLiveData<List<Double>>()
    val cumulativePnL: LiveData<List<Double>> = _cumulativePnL
    
    private val _dailyPnL = MutableLiveData<List<Pair<String, Double>>>()
    val dailyPnL: LiveData<List<Pair<String, Double>>> = _dailyPnL
    
    private val _monthlyPnL = MutableLiveData<List<Pair<Int, Double>>>()
    val monthlyPnL: LiveData<List<Pair<Int, Double>>> = _monthlyPnL
    
    private val _exportResult = MutableLiveData<Uri?>()
    val exportResult: LiveData<Uri?> = _exportResult
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            // Load statistics
            val stats = repository.getTradeStatistics(strategyId)
            _statistics.value = stats
            
            // Calculate cumulative P&L
            calculateCumulativePnL()
            
            // Load daily P&L
            loadDailyPnL()
            
            // Load monthly P&L
            loadMonthlyPnL()
        }
    }
    
    private suspend fun calculateCumulativePnL() {
        val completedTrades = repository.getAllCompletedTradesForExport(strategyId)
        val cumulativeList = mutableListOf<Double>()
        var runningTotal = 0.0
        
        completedTrades.forEach { trade ->
            runningTotal += trade.pnlAmount ?: 0.0
            cumulativeList.add(runningTotal)
        }
        
        _cumulativePnL.value = cumulativeList
    }
    
    private suspend fun loadDailyPnL() {
        val dailyResults = repository.getDailyPnLForMonth(strategyId)
        _dailyPnL.value = dailyResults.map { it.date to it.pnl }
    }
    
    private suspend fun loadMonthlyPnL() {
        val monthlyResults = repository.getMonthlyPnLForYear(strategyId)
        _monthlyPnL.value = monthlyResults.map { it.month to it.pnl }
    }
    
    fun updateDescription(description: String) {
        viewModelScope.launch {
            repository.updateDescription(strategyId, description)
        }
    }
    
    fun updateProfitAmount(amount: Double) {
        viewModelScope.launch {
            repository.updateProfitAmount(strategyId, amount)
        }
    }
    
    fun updateLossAmount(amount: Double) {
        viewModelScope.launch {
            repository.updateLossAmount(strategyId, amount)
        }
    }
    
    fun exportToCsv(context: Context) {
        viewModelScope.launch {
            val strategyName = strategy.value?.name ?: "trades"
            val tradesForExport = repository.getAllCompletedTradesForExport(strategyId)
            
            if (tradesForExport.isEmpty()) {
                Toast.makeText(context, "No trades to export", Toast.LENGTH_SHORT).show()
                return@launch
            }
            
            val uri = CSVExporter.exportTrades(context, strategyName, tradesForExport)
            if (uri != null) {
                Toast.makeText(context, "CSV exported to Downloads", Toast.LENGTH_SHORT).show()
                _exportResult.value = uri
            } else {
                Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    fun refreshData() {
        loadData()
    }
}

class StrategyDetailViewModelFactory(
    private val repository: TradeRepository,
    private val strategyId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StrategyDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StrategyDetailViewModel(repository, strategyId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
