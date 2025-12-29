package com.strategytracker.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.strategytracker.R
import com.strategytracker.data.models.Strategy
import com.strategytracker.databinding.ItemStrategyBinding

class StrategyAdapter(
    private val onStrategyClick: (Strategy) -> Unit,
    private val onOverlayClick: (Strategy) -> Unit,
    private val onStrategyLongClick: (Strategy) -> Unit
) : ListAdapter<Strategy, StrategyAdapter.StrategyViewHolder>(StrategyDiffCallback()) {
    
    private var activeOverlayStrategyId: Long = -1
    
    fun setActiveOverlayStrategyId(strategyId: Long) {
        val oldId = activeOverlayStrategyId
        activeOverlayStrategyId = strategyId
        
        // Refresh items that changed
        currentList.forEachIndexed { index, strategy ->
            if (strategy.id == oldId || strategy.id == strategyId) {
                notifyItemChanged(index)
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StrategyViewHolder {
        val binding = ItemStrategyBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StrategyViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: StrategyViewHolder, position: Int) {
        val strategy = getItem(position)
        holder.bind(strategy, strategy.id == activeOverlayStrategyId)
    }
    
    inner class StrategyViewHolder(
        private val binding: ItemStrategyBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(strategy: Strategy, isOverlayActive: Boolean) {
            binding.apply {
                tvStrategyName.text = strategy.name
                
                // Update overlay button appearance
                btnOverlay.setImageResource(
                    if (isOverlayActive) R.drawable.ic_overlay_active
                    else R.drawable.ic_overlay
                )
                
                // Card click
                root.setOnClickListener {
                    onStrategyClick(strategy)
                }
                
                // Long click for delete
                root.setOnLongClickListener {
                    onStrategyLongClick(strategy)
                    true
                }
                
                // Overlay button click
                btnOverlay.setOnClickListener {
                    onOverlayClick(strategy)
                }
            }
        }
    }
}

class StrategyDiffCallback : DiffUtil.ItemCallback<Strategy>() {
    override fun areItemsTheSame(oldItem: Strategy, newItem: Strategy): Boolean {
        return oldItem.id == newItem.id
    }
    
    override fun areContentsTheSame(oldItem: Strategy, newItem: Strategy): Boolean {
        return oldItem == newItem
    }
}
