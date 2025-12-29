package com.strategytracker.ui.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.strategytracker.R
import com.strategytracker.data.models.Trade
import com.strategytracker.data.models.TradeResult
import com.strategytracker.databinding.ItemTradeLogBinding
import com.strategytracker.utils.DateTimeUtils

class TradeLogAdapter : ListAdapter<Trade, TradeLogAdapter.TradeViewHolder>(TradeDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TradeViewHolder {
        val binding = ItemTradeLogBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TradeViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: TradeViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }
    
    inner class TradeViewHolder(
        private val binding: ItemTradeLogBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(trade: Trade, tradeNumber: Int) {
            binding.apply {
                tvTradeNumber.text = "#$tradeNumber"
                tvEntryTime.text = DateTimeUtils.formatDisplayDateTime(trade.entryDateTime)
                tvExitTime.text = trade.exitDateTime?.let { 
                    DateTimeUtils.formatDisplayDateTime(it) 
                } ?: "-"
                
                // Result indicator
                when (trade.result) {
                    TradeResult.WIN -> {
                        tvResult.text = "WIN"
                        tvResult.setTextColor(ContextCompat.getColor(root.context, R.color.green))
                        indicatorResult.setBackgroundColor(ContextCompat.getColor(root.context, R.color.green))
                    }
                    TradeResult.LOSS -> {
                        tvResult.text = "LOSS"
                        tvResult.setTextColor(ContextCompat.getColor(root.context, R.color.red))
                        indicatorResult.setBackgroundColor(ContextCompat.getColor(root.context, R.color.red))
                    }
                    null -> {
                        tvResult.text = "-"
                        tvResult.setTextColor(ContextCompat.getColor(root.context, R.color.gray))
                        indicatorResult.setBackgroundColor(ContextCompat.getColor(root.context, R.color.gray))
                    }
                }
                
                // P&L
                val pnl = trade.pnlAmount ?: 0.0
                tvPnl.text = if (pnl >= 0) "+${String.format("%.2f", pnl)}" 
                            else String.format("%.2f", pnl)
                tvPnl.setTextColor(
                    if (pnl >= 0) ContextCompat.getColor(root.context, R.color.green)
                    else ContextCompat.getColor(root.context, R.color.red)
                )
            }
        }
    }
}

class TradeDiffCallback : DiffUtil.ItemCallback<Trade>() {
    override fun areItemsTheSame(oldItem: Trade, newItem: Trade): Boolean {
        return oldItem.id == newItem.id
    }
    
    override fun areContentsTheSame(oldItem: Trade, newItem: Trade): Boolean {
        return oldItem == newItem
    }
}
