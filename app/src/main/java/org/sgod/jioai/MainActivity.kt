package org.sgod.jioai

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
            gravity = android.view.Gravity.CENTER
            setBackgroundColor(android.graphics.Color.parseColor("#0f0f0f"))
        }

        val title = TextView(this).apply {
            text = "🟢 JIO AI PRO 🟢"
            setTextColor(android.graphics.Color.parseColor("#00ff00"))
            textSize = 20f
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = android.view.Gravity.CENTER
        }
        layout.addView(title)

        val desc = TextView(this).apply {
            text = "\nGrant overlay permission to start the Jio AI Pro rotating floating panel.\n"
            setTextColor(android.graphics.Color.WHITE)
            textSize = 14f
            gravity = android.view.Gravity.CENTER
        }
        layout.addView(desc)

        val btn = Button(this).apply {
            text = "GRANT PERMISSION"
            setBackgroundColor(android.graphics.Color.parseColor("#00ff00"))
            setTextColor(android.graphics.Color.BLACK)
            setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this@MainActivity)) {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                    startActivity(intent)
                } else {
                    startService(Intent(this@MainActivity, FloatingService::class.java))
                    finish()
                }
            }
        }
        layout.addView(btn)

        setContentView(layout)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
            startService(Intent(this, FloatingService::class.java))
            finish()
        }
    }
}
