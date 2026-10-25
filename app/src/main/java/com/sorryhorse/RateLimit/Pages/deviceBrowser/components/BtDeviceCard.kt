package com.sorryhorse.RateLimit.Pages.deviceBrowser.components

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import com.sorryhorse.RateLimit.R
import com.sorryhorse.RateLimit.services.BluetoothService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BtDeviceCard (
    context: Context,
    attrs: AttributeSet? = null,
) : ConstraintLayout(context) {
    private val nameTextView : TextView
    private val connectionTextView : TextView
    private var selected : Boolean = false
    private val selectedIcon : ImageView
    private var root : ConstraintLayout
    private val scope = CoroutineScope(Dispatchers.IO)



    init {
        LayoutInflater.from(context).inflate(R.layout.bt_device_card, this)
        nameTextView = findViewById(R.id.name)
        connectionTextView = findViewById(R.id.connection)
        root = findViewById(R.id.root)
        selectedIcon = findViewById(R.id.selectedIcon)

        attrs?.let {
            context.withStyledAttributes(it, R.styleable.BtDeviceCard, 0, 0) {
                selected = getBoolean(R.styleable.BtDeviceCard_selected, false)
                nameTextView.text = getString(R.styleable.BtDeviceCard_btDeviceName)
                connectionTextView.text = getString(R.styleable.BtDeviceCard_btConnectionStrength)
                if (selected) {
                    selectedIcon.visibility = VISIBLE
                }
            }
        }
    }

    @androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun setData(name : String, device : BluetoothDevice, connection : Int, context: Context) {
        nameTextView.text = name
        connectionTextView.text = "Signal strength: " + when (connection) {
            in 100 downTo -50 -> "Excellent"
            in -60 downTo -70 -> "Good"
            in -80 downTo -90 -> "Weak"
            else -> {"Extremely poor"}
        }
        val bluetoothService = BluetoothService
        if (bluetoothService.getDevice()?.address == device.address){
            selected = true
            selectedIcon.visibility = VISIBLE
        }
        this.setOnClickListener  {
            connectToDevice(device, context)
        }
    }

    fun connectToDevice(device : BluetoothDevice, context: Context) {
        val bluetoothService = BluetoothService
        bluetoothService.connectToDevice(device, context)
        selected = true
        selectedIcon.visibility = VISIBLE
        scope.launch {
            val preferences = com.sorryhorse.RateLimit.Preferences(context)
            preferences.saveDevice(device.address)
        }
    }
}