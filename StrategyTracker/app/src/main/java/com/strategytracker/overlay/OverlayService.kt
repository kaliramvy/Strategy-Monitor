package com.strategytracker.overlay

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.strategytracker.R
import com.strategytracker.StrategyTrackerApp
import com.strategytracker.data.models.ButtonType
import com.strategytracker.data.models.Trade
import com.strategytracker.data.models.TradeResult
import com.strategytracker.ui.home.HomeActivity
import kotlinx.coroutines.*

class OverlayService : Service() {
    
    private lateinit var windowManager: WindowManager
    private lateinit var repository: com.strategytracker.data.repository.TradeRepository
    
    private var blueButton: View? = null
    private var greenButton: View? = null
    private var redButton: View? = null
    
    private var blueParams: WindowManager.LayoutParams? = null
    private var greenParams: WindowManager.LayoutParams? = null
    private var redParams: WindowManager.LayoutParams? = null
    
    private var strategyId: Long = -1
    private var activeTrade: Trade? = null
    private var isTradeActive = false
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    companion object {
        const val EXTRA_STRATEGY_ID = "extra_strategy_id"
        const val ACTION_STOP = "action_stop"
        
        private var isRunning = false
        private var currentStrategyId: Long = -1
        
        fun isServiceRunning(): Boolean = isRunning
        fun getCurrentStrategyId(): Long = currentStrategyId
        
        fun start(context: Context, strategyId: Long) {
            val intent = Intent(context, OverlayService::class.java).apply {
                putExtra(EXTRA_STRATEGY_ID, strategyId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stop(context: Context) {
            context.stopService(Intent(context, OverlayService::class.java))
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        repository = StrategyTrackerApp.getInstance().repository
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        
        strategyId = intent?.getLongExtra(EXTRA_STRATEGY_ID, -1) ?: -1
        if (strategyId == -1L) {
            stopSelf()
            return START_NOT_STICKY
        }
        
        currentStrategyId = strategyId
        isRunning = true
        
        startForeground(StrategyTrackerApp.OVERLAY_NOTIFICATION_ID, createNotification())
        
        serviceScope.launch {
            checkForActiveTrade()
            createOverlayButtons()
        }
        
        return START_STICKY
    }
    
    private suspend fun checkForActiveTrade() {
        activeTrade = repository.getActiveTrade(strategyId)
        isTradeActive = activeTrade != null
    }
    
    private fun createNotification(): Notification {
        val stopIntent = Intent(this, OverlayService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val openIntent = Intent(this, HomeActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, StrategyTrackerApp.OVERLAY_CHANNEL_ID)
            .setContentTitle("Trade Overlay Active")
            .setContentText("Tap to open app • Swipe to dismiss")
            .setSmallIcon(R.drawable.ic_overlay)
            .setOngoing(true)
            .setContentIntent(openPendingIntent)
            .addAction(R.drawable.ic_close, "Stop", stopPendingIntent)
            .build()
    }
    
    private suspend fun createOverlayButtons() {
        val inflater = LayoutInflater.from(this)
        val displayMetrics = DisplayMetrics()
        
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        
        // Load saved positions or use defaults
        val positions = repository.getPositionsForStrategy(strategyId)
        
        val bluePos = positions.find { it.buttonType == ButtonType.BLUE }
        val greenPos = positions.find { it.buttonType == ButtonType.GREEN }
        val redPos = positions.find { it.buttonType == ButtonType.RED }
        
        // Create blue button
        blueButton = inflater.inflate(R.layout.overlay_button_blue, null)
        blueParams = createLayoutParams(
            bluePos?.positionX ?: (screenWidth - 200),
            bluePos?.positionY ?: (screenHeight / 2 - 200)
        )
        
        // Create green button
        greenButton = inflater.inflate(R.layout.overlay_button_green, null)
        greenParams = createLayoutParams(
            greenPos?.positionX ?: (screenWidth - 200),
            greenPos?.positionY ?: (screenHeight / 2)
        )
        
        // Create red button
        redButton = inflater.inflate(R.layout.overlay_button_red, null)
        redParams = createLayoutParams(
            redPos?.positionX ?: (screenWidth - 200),
            redPos?.positionY ?: (screenHeight / 2 + 200)
        )
        
        // Add views to window
        windowManager.addView(blueButton, blueParams)
        windowManager.addView(greenButton, greenParams)
        windowManager.addView(redButton, redParams)
        
        // Setup touch listeners
        setupButtonTouchListener(blueButton!!, blueParams!!, ButtonType.BLUE)
        setupButtonTouchListener(greenButton!!, greenParams!!, ButtonType.GREEN)
        setupButtonTouchListener(redButton!!, redParams!!, ButtonType.RED)
        
        // Update button states
        updateButtonStates()
    }
    
    private fun createLayoutParams(x: Int, y: Int): WindowManager.LayoutParams {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            this.x = x
            this.y = y
        }
    }
    
    private fun setupButtonTouchListener(
        view: View,
        params: WindowManager.LayoutParams,
        buttonType: ButtonType
    ) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isClick = true
        val clickThreshold = 10
        
        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isClick = true
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (event.rawX - initialTouchX).toInt()
                    val deltaY = (event.rawY - initialTouchY).toInt()
                    
                    if (Math.abs(deltaX) > clickThreshold || Math.abs(deltaY) > clickThreshold) {
                        isClick = false
                    }
                    
                    params.x = initialX + deltaX
                    params.y = initialY + deltaY
                    windowManager.updateViewLayout(view, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isClick) {
                        handleButtonClick(buttonType)
                    } else {
                        // Save position
                        serviceScope.launch {
                            repository.saveButtonPosition(strategyId, buttonType, params.x, params.y)
                        }
                    }
                    true
                }
                else -> false
            }
        }
        
        view.setOnLongClickListener {
            showCloseOverlayOption()
            true
        }
    }
    
    private fun handleButtonClick(buttonType: ButtonType) {
        when (buttonType) {
            ButtonType.BLUE -> handleBlueClick()
            ButtonType.GREEN -> handleGreenClick()
            ButtonType.RED -> handleRedClick()
        }
    }
    
    private fun handleBlueClick() {
        if (isTradeActive) {
            Toast.makeText(this, "Trade already active!", Toast.LENGTH_SHORT).show()
            return
        }
        
        serviceScope.launch {
            val tradeId = repository.startTrade(strategyId)
            activeTrade = repository.getTradeById(tradeId)
            isTradeActive = true
            updateButtonStates()
            Toast.makeText(this@OverlayService, "Trade Entry Recorded", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun handleGreenClick() {
        if (!isTradeActive || activeTrade == null) {
            Toast.makeText(this, "Start trade first (Blue button)", Toast.LENGTH_SHORT).show()
            return
        }
        
        serviceScope.launch {
            val strategy = repository.getStrategyById(strategyId)
            if (strategy != null && activeTrade != null) {
                repository.endTrade(activeTrade!!.id, true, strategy)
                val pnl = strategy.profitAmount
                Toast.makeText(this@OverlayService, "Trade closed: +$pnl", Toast.LENGTH_SHORT).show()
                activeTrade = null
                isTradeActive = false
                updateButtonStates()
            }
        }
    }
    
    private fun handleRedClick() {
        if (!isTradeActive || activeTrade == null) {
            Toast.makeText(this, "Start trade first (Blue button)", Toast.LENGTH_SHORT).show()
            return
        }
        
        serviceScope.launch {
            val strategy = repository.getStrategyById(strategyId)
            if (strategy != null && activeTrade != null) {
                repository.endTrade(activeTrade!!.id, false, strategy)
                val pnl = strategy.lossAmount
                Toast.makeText(this@OverlayService, "Trade closed: -$pnl", Toast.LENGTH_SHORT).show()
                activeTrade = null
                isTradeActive = false
                updateButtonStates()
            }
        }
    }
    
    private fun updateButtonStates() {
        blueButton?.let { view ->
            val button = view.findViewById<ImageButton>(R.id.btn_blue)
            button?.apply {
                isEnabled = !isTradeActive
                alpha = if (isTradeActive) 0.5f else 1.0f
            }
        }
        
        greenButton?.let { view ->
            val button = view.findViewById<ImageButton>(R.id.btn_green)
            button?.apply {
                isEnabled = isTradeActive
                alpha = if (isTradeActive) 1.0f else 0.5f
            }
        }
        
        redButton?.let { view ->
            val button = view.findViewById<ImageButton>(R.id.btn_red)
            button?.apply {
                isEnabled = isTradeActive
                alpha = if (isTradeActive) 1.0f else 0.5f
            }
        }
    }
    
    private fun showCloseOverlayOption() {
        Toast.makeText(this, "Stopping overlay...", Toast.LENGTH_SHORT).show()
        stopSelf()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        currentStrategyId = -1
        serviceScope.cancel()
        
        blueButton?.let { windowManager.removeView(it) }
        greenButton?.let { windowManager.removeView(it) }
        redButton?.let { windowManager.removeView(it) }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}
