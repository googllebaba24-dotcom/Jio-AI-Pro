package org.sgod.jioai

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.TextView

class FloatingService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var container: FrameLayout
    private lateinit var bubbleView: TextView
    private lateinit var params: WindowManager.LayoutParams
    private lateinit var webView: WebView
    private var isMinimized = false

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        container = FrameLayout(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
        }

        webView = WebView(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            addJavascriptInterface(WebAppInterface(), "Android")
            layoutParams = FrameLayout.LayoutParams(600, 600)
            loadUrl("file:///android_asset/index.html")
        }
        container.addView(webView)

        bubbleView = TextView(this).apply {
            text = "🟢"
            setTextColor(Color.WHITE)
            textSize = 22f
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#cc000000"))
            setPadding(20, 20, 20, 20)
            visibility = View.GONE
        }

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        // Yahan FLAG_NOT_FOCUSABLE hata diya hai taaki keyboard easily khul sake
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }

        webView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        if (Math.abs(dx) > 5 || Math.abs(dy) > 5) {
                            params.x = initialX + dx
                            params.y = initialY + dy
                            windowManager.updateViewLayout(container, params)
                            return true
                        }
                    }
                }
                return false
            }
        })

        bubbleView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var isMoved = false

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isMoved = false
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        if (Math.abs(dx) > 5 || Math.abs(dy) > 5) {
                            isMoved = true
                            params.x = initialX + dx
                            params.y = initialY + dy
                            windowManager.updateViewLayout(bubbleView, params)
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!isMoved) {
                            expandPanel()
                        }
                        return true
                    }
                }
                return false
            }
        })

        windowManager.addView(container, params)
        windowManager.addView(bubbleView, params)
    }

    inner class WebAppInterface {
        @JavascriptInterface
        fun minimizePanel() {
            android.os.Handler(mainLooper).post {
                container.visibility = View.GONE
                bubbleView.visibility = View.VISIBLE
                isMinimized = true
            }
        }

        @JavascriptInterface
        fun closeApp() {
            // Service ko poori tarah band karne ke liye stopSelf() call kiya hai
            android.os.Handler(mainLooper).post {
                stopSelf()
            }
        }

        @JavascriptInterface
        fun enableFocus() {}
    }

    private fun expandPanel() {
        bubbleView.visibility = View.GONE
        container.visibility = View.VISIBLE
        isMinimized = false
    }

    override fun onDestroy() {
        super.onDestroy()
        // App close hone par views ko screen se hamesha ke liye hata diya jayega
        if (::container.isInitialized) {
            try { windowManager.removeView(container) } catch (e: Exception) {}
        }
        if (::bubbleView.isInitialized) {
            try { windowManager.removeView(bubbleView) } catch (e: Exception) {}
        }
    }
}
