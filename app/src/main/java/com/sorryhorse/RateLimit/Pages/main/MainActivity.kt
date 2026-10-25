package com.sorryhorse.RateLimit.Pages.main

import android.Manifest
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.sorryhorse.RateLimit.Pages.deviceBrowser.DeviceBrowser
import com.sorryhorse.RateLimit.Pages.main.components.DeviceCard
import com.sorryhorse.RateLimit.Preferences
import com.sorryhorse.RateLimit.R
import com.sorryhorse.RateLimit.services.BluetoothService
import kotlinx.coroutines.launch
import me.bastanfar.semicirclearcprogressbar.SemiCircleArcProgressBar

class MainActivity : AppCompatActivity() {
    private var threshold = 145
    private var previous_device = ""

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

        val bluetoothService = BluetoothService
        bluetoothService.init(this, this)
        bluetoothService.setGattCallback(::setHeartRate)
        bluetoothService.setScanCallbacks(::scanCallback, {})

        lifecycleScope.launch {
            val preferences = Preferences(this@MainActivity)
            preferences.thresholdFlow.collect { value ->
                threshold = value
                val thresholdButton = findViewById<TextView>(R.id.threshold)
                thresholdButton.text = "Threshold: $threshold"
            }
        }

        lifecycleScope.launch {
            val preferences = Preferences(this@MainActivity)
            preferences.deviceFlow.collect { value ->
                previous_device = value
                val bluetoothService = BluetoothService
                bluetoothService.scan(this@MainActivity)
            }
        }


        val deviceCard = findViewById<DeviceCard>(R.id.deviceCard)
        deviceCard.setOnClickListener {
            val intent = Intent(this, DeviceBrowser::class.java)
            startActivity(intent)
        }


        val thresholdButton = findViewById<TextView>(R.id.threshold)
        thresholdButton.setOnClickListener {
            val input = EditText(this).apply {
                inputType = InputType.TYPE_CLASS_NUMBER
            }
            AlertDialog.Builder(this)
                .setTitle("Set threshold")
                .setView(input)
                .setPositiveButton("OK") { dialog, _ ->
                    threshold = input.text.toString().toInt()
                    lifecycleScope.launch {
                        val preferences = Preferences(this@MainActivity)
                        preferences.saveThreshold(threshold)
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create().show()
        }
    }

    fun scanCallback(result : ScanResult) {
        if (result.device.address != previous_device)
            return
        val bluetoothService = BluetoothService
        bluetoothService.connectToDevice(result.device, this)
        setDeviceCard()
    }

    fun setDeviceCard() {
        val bluetoothService = BluetoothService
        if (bluetoothService.getDevice() == null)
            return
        val deviceCard = findViewById<DeviceCard>(R.id.deviceCard)
        if (deviceCard.isConnected())
            return
        deviceCard.setDevice(bluetoothService.getDevice())
    }

    fun setHeartRate(heartRate: Int) {
        runOnUiThread {
            setDeviceCard()
            val percent = rateToPercent(heartRate)
            val progress = findViewById<SemiCircleArcProgressBar>(R.id.graph)
            progress.setPercent(percent)

            val heart_rate = findViewById<TextView>(R.id.heart_rate)
            heart_rate.text = heartRate.toString()
        }
    }

    fun rateToPercent(rate : Int) : Int {
        return Math.min(100, 100 * rate / threshold)
    }
}