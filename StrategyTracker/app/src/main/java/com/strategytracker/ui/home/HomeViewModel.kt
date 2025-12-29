package com.strategytracker.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.strategytracker.data.models.Strategy
import com.strategytracker.data.repository.TradeRepository
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: TradeRepository) : ViewModel() {
    
    val strategies: LiveData<List<Strategy>> = repository.getAllStrategiesLiveData()
    
    fun addStrategy(name: String) {
        viewModelScope.launch {
            val strategy = Strategy(name = name)
            repository.insertStrategy(strategy)
        }
    }
    
    fun deleteStrategy(strategy: Strategy) {
        viewModelScope.launch {
            repository.deleteStrategy(strategy)
        }
    }
    
    fun updateStrategy(strategy: Strategy) {
        viewModelScope.launch {
            repository.updateStrategy(strategy)
        }
    }
}

class HomeViewModelFactory(private val repository: TradeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
