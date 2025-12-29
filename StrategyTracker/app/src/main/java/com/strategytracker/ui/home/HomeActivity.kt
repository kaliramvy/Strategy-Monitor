package com.strategytracker.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.strategytracker.R
import com.strategytracker.StrategyTrackerApp
import com.strategytracker.data.models.Strategy
import com.strategytracker.databinding.ActivityHomeBinding
import com.strategytracker.overlay.OverlayService
import com.strategytracker.ui.detail.StrategyDetailActivity
import com.strategytracker.ui.dialogs.AddStrategyDialog

class HomeActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(StrategyTrackerApp.getInstance().repository)
    }
    
    private lateinit var strategyAdapter: StrategyAdapter
    private var pendingStrategyId: Long = -1
    
    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Settings.canDrawOverlays(this)) {
            if (pendingStrategyId != -1L) {
                startOverlayService(pendingStrategyId)
                pendingStrategyId = -1
            }
        } else {
            Toast.makeText(this, "Overlay permission denied", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        setupFab()
        observeData()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }
    
    private fun setupRecyclerView() {
        strategyAdapter = StrategyAdapter(
            onStrategyClick = { strategy ->
                openStrategyDetail(strategy)
            },
            onOverlayClick = { strategy ->
                handleOverlayClick(strategy)
            },
            onStrategyLongClick = { strategy ->
                showDeleteDialog(strategy)
            }
        )
        
        binding.recyclerStrategies.apply {
            layoutManager = GridLayoutManager(this@HomeActivity, 2)
            adapter = strategyAdapter
        }
    }
    
    private fun setupFab() {
        binding.fabAddStrategy.setOnClickListener {
            showAddStrategyDialog()
        }
    }
    
    private fun observeData() {
        viewModel.strategies.observe(this) { strategies ->
            if (strategies.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.recyclerStrategies.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.recyclerStrategies.visibility = View.VISIBLE
                strategyAdapter.submitList(strategies)
            }
        }
    }
    
    private fun showAddStrategyDialog() {
        AddStrategyDialog { strategyName ->
            viewModel.addStrategy(strategyName)
        }.show(supportFragmentManager, "AddStrategyDialog")
    }
    
    private fun openStrategyDetail(strategy: Strategy) {
        val intent = Intent(this, StrategyDetailActivity::class.java).apply {
            putExtra(StrategyDetailActivity.EXTRA_STRATEGY_ID, strategy.id)
        }
        startActivity(intent)
    }
    
    private fun handleOverlayClick(strategy: Strategy) {
        // Check if overlay is already running for different strategy
        if (OverlayService.isServiceRunning()) {
            if (OverlayService.getCurrentStrategyId() == strategy.id) {
                // Stop current overlay
                OverlayService.stop(this)
                Toast.makeText(this, "Overlay stopped", Toast.LENGTH_SHORT).show()
            } else {
                // Ask to switch
                MaterialAlertDialogBuilder(this)
                    .setTitle("Overlay Active")
                    .setMessage("Another strategy overlay is active. Do you want to switch to ${strategy.name}?")
                    .setPositiveButton("Switch") { _, _ ->
                        OverlayService.stop(this)
                        requestOverlayPermission(strategy.id)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        } else {
            requestOverlayPermission(strategy.id)
        }
    }
    
    private fun requestOverlayPermission(strategyId: Long) {
        if (Settings.canDrawOverlays(this)) {
            startOverlayService(strategyId)
        } else {
            pendingStrategyId = strategyId
            MaterialAlertDialogBuilder(this)
                .setTitle("Permission Required")
                .setMessage("To show overlay buttons over other apps, please grant the 'Display over other apps' permission.")
                .setPositiveButton("Grant") { _, _ ->
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    overlayPermissionLauncher.launch(intent)
                }
                .setNegativeButton("Cancel") { _, _ ->
                    pendingStrategyId = -1
                }
                .show()
        }
    }
    
    private fun startOverlayService(strategyId: Long) {
        OverlayService.start(this, strategyId)
        
        // Save state for boot receiver
        getSharedPreferences("overlay_prefs", MODE_PRIVATE)
            .edit()
            .putLong("last_strategy_id", strategyId)
            .putBoolean("was_active", true)
            .apply()
        
        Toast.makeText(this, "Overlay activated", Toast.LENGTH_SHORT).show()
    }
    
    private fun showDeleteDialog(strategy: Strategy) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Strategy")
            .setMessage("Are you sure you want to delete '${strategy.name}'? All trades will be lost.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteStrategy(strategy)
                Toast.makeText(this, "Strategy deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onResume() {
        super.onResume()
        // Update adapter to show correct overlay state
        strategyAdapter.setActiveOverlayStrategyId(
            if (OverlayService.isServiceRunning()) OverlayService.getCurrentStrategyId() else -1
        )
    }
}
