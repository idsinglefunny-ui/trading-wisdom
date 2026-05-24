package com.tradeyourplan.notification

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.tradeyourplan.R
import com.tradeyourplan.ui.main.MainActivity

class QuoteReminderService : Service() {

    companion object {
        private const val TAG = "QuoteReminderService"
        private const val EXTRA_QUOTE_TEXT = "quote_text"
        private const val FOREGROUND_NOTIFICATION_ID = 3001

        fun start(context: Context, quoteText: String) {
            val intent = Intent(context, QuoteReminderService::class.java)
            intent.putExtra(EXTRA_QUOTE_TEXT, quoteText)
            try {
                context.startForegroundService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start service", e)
            }
        }

        fun canDrawOverlays(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else {
                true
            }
        }
    }

    private var overlayView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val quoteText = intent?.getStringExtra(EXTRA_QUOTE_TEXT)
            ?: "计划你的交易，交易你的计划。"

        Log.d(TAG, "Service started, quoteText=$quoteText, canDrawOverlays=${canDrawOverlays(this)}")

        // 1. Start as foreground service
        val fgNotification = createForegroundNotification(quoteText)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(FOREGROUND_NOTIFICATION_ID, fgNotification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(FOREGROUND_NOTIFICATION_ID, fgNotification)
            }
            Log.d(TAG, "Foreground service started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start foreground", e)
        }

        // 2. Show WindowManager overlay
        if (canDrawOverlays(this)) {
            Log.d(TAG, "Overlay permission granted, showing WindowManager overlay")
            showOverlay(quoteText)
        } else {
            Log.w(TAG, "Overlay permission NOT granted, falling back to Activity launch")
            // Fallback: try Activity
            try {
                val overlayIntent = Intent(this, QuoteReminderActivity::class.java)
                overlayIntent.putExtra(QuoteReminderActivity.EXTRA_QUOTE_TEXT, quoteText)
                overlayIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(overlayIntent)
                Log.d(TAG, "Fallback activity launched")
            } catch (e: Exception) {
                Log.e(TAG, "Fallback activity failed", e)
            }
            Handler(Looper.getMainLooper()).postDelayed({ stopSelf() }, 3000)
        }

        return START_NOT_STICKY
    }

    @SuppressLint("ClickableViewAccessibility", "RtlHardcoded")
    private fun showOverlay(quoteText: String) {
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Use flags that allow overlay on lock screen with interaction
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )
        params.screenOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        // Allow overlay to receive touch events
        params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

        val view = createOverlayView(quoteText, windowManager)
        overlayView = view

        try {
            windowManager.addView(view, params)
            Log.d(TAG, "WindowManager overlay shown successfully")
            android.util.Log.d("QuoteReminderOverlay", "Overlay added to WindowManager")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show overlay", e)
            android.util.Log.e("QuoteReminderOverlay", "Failed to add overlay", e)
        }
    }

    private fun dp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics
        ).toInt()
    }

    private fun createOverlayView(quoteText: String, windowManager: WindowManager): View {
        android.util.Log.d("QuoteReminderOverlay", "Creating overlay view for quote: $quoteText")

        // Root - semi-transparent dark background
        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#EB000000"))
            isFocusable = true
            isFocusableInTouchMode = true
        }
        root.setOnClickListener { dismissOverlay(windowManager) }

        // Card with rounded corners
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            val bg = GradientDrawable().apply {
                setColor(Color.parseColor("#FF1E293B"))
                cornerRadius = dp(24).toFloat()
            }
            background = bg
            setPadding(dp(32), dp(40), dp(32), dp(40))
        }

        val cardParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(dp(28), 0, dp(28), 0)
            gravity = Gravity.CENTER
        }

        // Close button
        val closeBtn = ImageView(this).apply {
            setImageDrawable(resources.getDrawable(android.R.drawable.ic_menu_close_clear_cancel, null))
            setColorFilter(Color.parseColor("#B0FFFFFF"))
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setOnClickListener { dismissOverlay(windowManager) }
        }
        val closeParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(dp(16), dp(28), dp(16), 0)
            gravity = Gravity.END or Gravity.TOP
        }

        // Title
        val title = TextView(this).apply {
            text = "交易智慧"
            setTextColor(Color.parseColor("#FF3B82F6"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            gravity = Gravity.CENTER
        }
        card.addView(title, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        // Spacer
        card.addView(View(this), LinearLayout.LayoutParams(1, dp(24)))

        // Quote text
        val quoteView = TextView(this)
        quoteView.text = "「$quoteText」"
        quoteView.setTextColor(Color.WHITE)
        quoteView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
        quoteView.setLineSpacing(dp(8).toFloat(), 1f)
        quoteView.gravity = Gravity.CENTER
        card.addView(quoteView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        // Spacer
        card.addView(View(this), LinearLayout.LayoutParams(1, dp(32)))

        // Dismiss hint
        val dismissHint = TextView(this).apply {
            text = "点击任意处关闭"
            setTextColor(Color.parseColor("#FF64748B"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            gravity = Gravity.CENTER
        }
        card.addView(dismissHint, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        root.addView(card, cardParams)
        root.addView(closeBtn, closeParams)

        android.util.Log.d("QuoteReminderOverlay", "Overlay view created successfully")
        return root
    }

    private fun dismissOverlay(windowManager: WindowManager) {
        overlayView?.let {
            try { windowManager.removeView(it) } catch (_: Exception) {}
        }
        overlayView = null
        stopSelf()
    }

    override fun onDestroy() {
        overlayView?.let {
            try {
                (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(it)
            } catch (_: Exception) {}
        }
        overlayView = null
        super.onDestroy()
    }

    private fun createForegroundNotification(quoteText: String): Notification {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        // Create fullScreenIntent for lock screen display
        val fullScreenIntent = Intent(this, QuoteReminderActivity::class.java)
        fullScreenIntent.putExtra(QuoteReminderActivity.EXTRA_QUOTE_TEXT, quoteText)
        fullScreenIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        val fullScreenPendingIntent = PendingIntent.getActivity(this, 0, fullScreenIntent, flags)

        val contentIntent = Intent(this, MainActivity::class.java)
        contentIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        return NotificationCompat.Builder(this, NotificationHelper.CHANNEL_FULLSCREEN_V2)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("交易智慧")
            .setContentText(quoteText)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(PendingIntent.getActivity(this, 0, contentIntent, flags))
            .setAutoCancel(true)
            .build()
    }
}
