package com.sorryhorse.RateLimit.Pages.deviceBrowser

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import com.sorryhorse.RateLimit.R
import androidx.activity.enableEdgeToEdge

class DeviceBrowser : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.device_browser)

        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener {
            finish()
        }
    }
}