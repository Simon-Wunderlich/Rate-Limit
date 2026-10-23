package com.sorryhorse.RateLimit.Pages.main

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sorryhorse.RateLimit.Pages.deviceBrowser.DeviceBrowser
import com.sorryhorse.RateLimit.Pages.main.components.DeviceCard
import com.sorryhorse.RateLimit.R

class MainActivity : AppCompatActivity() {

    @RequiresPermission(allOf= [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val deviceCard = findViewById<DeviceCard>(R.id.deviceCard)
        deviceCard.setOnClickListener {
            val intent = Intent(this, DeviceBrowser::class.java)
            startActivity(intent)
        }
//        val bluetoothService = BluetoothService(this, this)
//        bluetoothService.scan()
        //Java


    }
}