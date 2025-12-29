package com.strategytracker.ui.detail

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.strategytracker.R
import com.strategytracker.StrategyTrackerApp
import com.strategytracker.databinding.ActivityStrategyDetailBinding
import com.strategytracker.utils.CSVExporter
import com.strategytracker.utils.DateTimeUtils

class StrategyDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_STRATEGY_ID = "extra_strategy_id"
    }

    private lateinit var binding: ActivityStrategyDetailBinding
    private val viewModel: StrategyDetailViewModel by viewModels {
        StrategyDetailViewModelFactory(
            StrategyTrackerApp.getInstance().repository,
            intent.getLongExtra(EXTRA_STRATEGY_ID, -1)
        )
    }

    private lateinit var tradeLogAdapter: TradeLogAdapter
    private var isInitialLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStrategyDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val strategyId = intent.getLongExtra(EXTRA_STRATEGY_ID, -1)
        if (strategyId == -1L) {
            Toast.makeText(this, "Invalid strategy", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupTradeLog()
        setupExportButton()
        observeData()
        setupTextWatchers()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupTradeLog() {
        tradeLogAdapter = TradeLogAdapter()
        binding.recyclerTradeLog.apply {
            layoutManager = LinearLayoutManager(this@StrategyDetailActivity)
            adapter = tradeLogAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupTextWatchers() {
        // Description
        binding.etDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!isInitialLoad) {
                    viewModel.updateDescription(s.toString())
                }
            }
        })

        // Profit
        binding.etProfitAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!isInitialLoad) {
                    val amount = s.toString().toDoubleOrNull() ?: 0.0
                    viewModel.updateProfitAmount(amount)
                }
            }
        })

        // Loss
        binding.etLossAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!isInitialLoad) {
                    val amount = s.toString().toDoubleOrNull() ?: 0.0
                    viewModel.updateLossAmount(amount)
                }
            }
        })
    }

    private fun setupExportButton() {
        binding.btnExportCsv.setOnClickListener {
            viewModel.exportToCsv(this)
        }
    }

    private fun observeData() {
        viewModel.strategy.observe(this) { strategy ->
            strategy?.let {
                supportActionBar?.title = it.name
                
                binding.etDescription.setText(it.description)
                binding.etProfitAmount.setText(if (it.profitAmount == 0.0) "" else it.profitAmount.toString())
                binding.etLossAmount.setText(if (it.lossAmount == 0.0) "" else it.lossAmount.toString())
                
                // Mark initial load complete after first data
                isInitialLoad = false
            }
        }

        viewModel.trades.observe(this) { trades ->
            tradeLogAdapter.submitList(trades.filter { !it.isActive })
        }

        viewModel.statistics.observe(this) { stats ->
            binding.tvTotalTrades.text = "Total Trades: ${stats.totalTrades}"
            binding.tvWins.text = "Wins: ${stats.wins}"
            binding.tvLosses.text = "Losses: ${stats.losses}"
            binding.tvWinRate.text = "Win Rate: ${String.format("%.1f", stats.winRate)}%"
            binding.tvTotalPnl.text = "Total P&L: ${formatPnL(stats.totalPnL)}"
            binding.tvTotalPnl.setTextColor(
                if (stats.totalPnL >= 0)
                    ContextCompat.getColor(this, R.color.green)
                else
                    ContextCompat.getColor(this, R.color.red)
            )
            setupPieChart(stats.wins, stats.losses)
        }

        viewModel.cumulativePnL.observe(this) { pnlData ->
            setupLineChart(pnlData)
        }

        viewModel.dailyPnL.observe(this) { dailyData ->
            setupDailyBarChart(dailyData)
        }

        viewModel.monthlyPnL.observe(this) { monthlyData ->
            setupMonthlyBarChart(monthlyData)
        }

        viewModel.exportResult.observe(this) { uri ->
            uri?.let {
                val strategyName = viewModel.strategy.value?.name ?: "trades"
                val shareIntent = CSVExporter.createShareIntent(this, it, strategyName)
                startActivity(Intent.createChooser(shareIntent, "Share Trade Log"))
            }
        }
    }

    private fun formatPnL(amount: Double): String {
        return if (amount >= 0) "+${String.format("%.2f", amount)}"
        else String.format("%.2f", amount)
    }

    private fun setupPieChart(wins: Int, losses: Int) {
        val pieChart = binding.pieChart
        if (wins == 0 && losses == 0) {
            pieChart.setNoDataText("No trades yet")
            pieChart.invalidate()
            return
        }

        val entries = ArrayList<PieEntry>()
        if (wins > 0) entries.add(PieEntry(wins.toFloat(), "Wins"))
        if (losses > 0) entries.add(PieEntry(losses.toFloat(), "Losses"))

        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                ContextCompat.getColor(this@StrategyDetailActivity, R.color.green),
                ContextCompat.getColor(this@StrategyDetailActivity, R.color.red)
            )
            valueTextSize = 14f
            valueTextColor = Color.WHITE
            valueFormatter = PercentFormatter(pieChart)
        }

        pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 40f
            transparentCircleRadius = 45f
            centerText = "Total\n${wins + losses}"
            setCenterTextSize(16f)
            setUsePercentValues(true)
            legend.isEnabled = true
            animateY(1000, Easing.EaseInOutQuad)
            invalidate()
        }
    }

    private fun setupLineChart(pnlData: List<Double>) {
        val lineChart = binding.lineChartPnl
        if (pnlData.isEmpty()) {
            lineChart.setNoDataText("No trades yet")
            lineChart.invalidate()
            return
        }

        val entries = pnlData.mapIndexed { index, pnl ->
            Entry(index.toFloat(), pnl.toFloat())
        }

        val dataSet = LineDataSet(entries, "Cumulative P&L").apply {
            color = ContextCompat.getColor(this@StrategyDetailActivity, R.color.blue)
            lineWidth = 2f
            setDrawCircles(true)
            circleRadius = 3f
            setCircleColor(ContextCompat.getColor(this@StrategyDetailActivity, R.color.blue))
            setDrawFilled(true)
            fillColor = ContextCompat.getColor(this@StrategyDetailActivity, R.color.blue)
            fillAlpha = 50
            valueTextSize = 10f
        }

        lineChart.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            axisRight.isEnabled = false
            legend.isEnabled = true
            animateX(1000)
            invalidate()
        }
    }

    private fun setupDailyBarChart(dailyData: List<Pair<String, Double>>) {
        val barChart = binding.barChartDaily
        if (dailyData.isEmpty()) {
            barChart.setNoDataText("No trades this month")
            barChart.invalidate()
            return
        }

        val entries = dailyData.mapIndexed { index, (_, pnl) ->
            BarEntry(index.toFloat(), pnl.toFloat())
        }

        val colors = dailyData.map { (_, pnl) ->
            if (pnl >= 0) ContextCompat.getColor(this, R.color.green)
            else ContextCompat.getColor(this, R.color.red)
        }

        val dataSet = BarDataSet(entries, "Daily P&L").apply {
            setColors(colors)
            valueTextSize = 8f
        }

        barChart.apply {
            data = BarData(dataSet)
            description.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            axisRight.isEnabled = false
            legend.isEnabled = true
            animateY(1000)
            invalidate()
        }
    }

    private fun setupMonthlyBarChart(monthlyData: List<Pair<Int, Double>>) {
        val barChart = binding.barChartMonthly
        if (monthlyData.isEmpty()) {
            barChart.setNoDataText("No trades this year")
            barChart.invalidate()
            return
        }

        val entries = monthlyData.mapIndexed { index, (_, pnl) ->
            BarEntry(index.toFloat(), pnl.toFloat())
        }

        val colors = monthlyData.map { (_, pnl) ->
            if (pnl >= 0) ContextCompat.getColor(this, R.color.green)
            else ContextCompat.getColor(this, R.color.red)
        }

        val dataSet = BarDataSet(entries, "Monthly P&L").apply {
            setColors(colors)
            valueTextSize = 10f
        }

        val labels = monthlyData.map { DateTimeUtils.getMonthName(it.first) }

        barChart.apply {
            data = BarData(dataSet)
            description.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.granularity = 1f
            axisRight.isEnabled = false
            legend.isEnabled = true
            animateY(1000)
            invalidate()
        }
    }
}
