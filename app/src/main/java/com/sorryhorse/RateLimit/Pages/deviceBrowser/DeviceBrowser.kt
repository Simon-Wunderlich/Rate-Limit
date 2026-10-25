package com.sorryhorse.RateLimit.Pages.deviceBrowser

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import com.sorryhorse.RateLimit.R
import androidx.activity.enableEdgeToEdge
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.annotation.RequiresPermission
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import com.sorryhorse.RateLimit.Pages.deviceBrowser.components.BtDeviceCard
import com.sorryhorse.RateLimit.Preferences
import com.sorryhorse.RateLimit.services.BluetoothService
import kotlinx.coroutines.launch

class DeviceBrowser : ComponentActivity() {
    private val devices: HashMap<String, BluetoothDevice> = HashMap()
    private val HEARTRATE_SERVICE = "0000180d-0000-1000-8000-00805f9b34fb"
    private val bluetoothService = BluetoothService
    private var previous_device = ""


    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bluetoothService.init(this, this)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                "#ffd8a8".toColorInt(),
                "#ffd8a8".toColorInt()
            )
        )

        setContentView(R.layout.device_browser)

        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener {
            finish()
        }
        startScan()
        val refresh = findViewById<ImageView>(R.id.refresh)
        refresh.setOnClickListener {
            startScan()
        }

        lifecycleScope.launch {
            val preferences = Preferences(this@DeviceBrowser)
            preferences.deviceFlow.collect { value ->
                println(value)
                previous_device = value
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun startScan() {
        val progress = findViewById<ProgressBar>(R.id.progress)
        progress.visibility = VISIBLE

        val refresh = findViewById<ImageView>(R.id.refresh)
        refresh.visibility = GONE

        val connectedDevice = bluetoothService.getDevice()
        if (connectedDevice != null)
            createDeviceCard(connectedDevice, -1)

        bluetoothService.setScanCallbacks(::scanCallback, ::onScanComplete)
        bluetoothService.scan(this)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun scanCallback(result: ScanResult) {
        val device: BluetoothDevice = result.device
        val services = result.scanRecord?.serviceUuids?.map { uuid -> uuid.toString() }
        if (services?.contains(HEARTRATE_SERVICE) == true) {
            createDeviceCard(device, result.rssi)
            devices[device.address] = device
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun createDeviceCard(device: BluetoothDevice, rssi : Int) {
        if (devices.contains(device.address))
            return
        val deviceCard = BtDeviceCard(this)
        if (device.name == null)
            deviceCard.setData("Unknown", device,rssi,this)
        else
            deviceCard.setData(device.name, device, rssi, this)

        if (device.address == previous_device)
            deviceCard.connectToDevice(device, this)

        val deviceList = findViewById<LinearLayout>(R.id.deviceList)
        deviceList.addView(deviceCard)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun onScanComplete(){
        println("DONE")
        val progress = findViewById<ProgressBar>(R.id.progress)
        progress.visibility = GONE

        val refresh = findViewById<ImageView>(R.id.refresh)
        refresh.visibility = VISIBLE
    }

}