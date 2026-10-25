package com.sorryhorse.RateLimit.services

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sorryhorse.RateLimit.getActivity
import java.util.UUID

object BluetoothService {
    private var scanResultCallback: ((ScanResult) ->Unit)? = null
    private var finishedCallback: (() -> Unit)? = null
    private var gattResultCallback: ((Int) -> Unit)? = null
    private var caller : ActivityResultCaller? = null
    private var onPermissionGrantedAction: (() -> Unit)? = null
    private var connectedDevice : BluetoothDevice? = null
    private var results = mutableSetOf<ScanResult> ()
    fun init(caller : ActivityResultCaller, context: Context) {
        this.caller = caller
        onPermissionGrantedAction = {scan(context)}
    }


    private val permissionLauncher: ActivityResultLauncher<String>? =
        caller?.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                onPermissionGrantedAction?.invoke()
            }
        }

    fun checkAndRequestPermission(context : Context) : Boolean {
        val permissions = listOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        val missingPermissions = permissions.filter {
                ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (!missingPermissions.isEmpty())
        {
            permissionLauncher?.launch(missingPermissions.first())
            return false
        }
        return true
    }

    fun setScanCallbacks(resultCallback: (ScanResult) -> Unit, finishCallback: () -> Unit) {
        this.scanResultCallback = resultCallback
        this.finishedCallback = finishCallback
    }

    fun setGattCallback(gattResultCallBack: ((Int) -> Unit)) {
        this.gattResultCallback = gattResultCallBack
    }

    fun scan(context : Context) {
        // Checks for permissions
        // If all permissions are granted, continue
        // If at least 1 permission has not been granted, the function exits and will be called again once permission is accepted
        if (!checkAndRequestPermission(context))
            return
        results.clear()

        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        val REQUEST_ENABLE_BT = 2
        val activity = context.getActivity()
        if (activity != null)
            ActivityCompat.startActivityForResult(activity, enableBtIntent, REQUEST_ENABLE_BT, null)
        else
            return

        val bleScanner = bluetoothAdapter?.bluetoothLeScanner
        var isScanning = false
        val handler = Handler(Looper.getMainLooper())

        val SCAN_PERIOD: Long = 10000

        fun scanLeDevice() {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                checkAndRequestPermission(context)
            }
            if (!isScanning) {
                handler.postDelayed({
                    isScanning = false
                    finishedCallback?.invoke()
                    bleScanner?.stopScan(ResultScanCallback)
                }, SCAN_PERIOD)

                isScanning = true
                bleScanner?.startScan(ResultScanCallback)
            } else {
                isScanning = false
                bleScanner?.stopScan(ResultScanCallback)
            }
        }

        scanLeDevice()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectToDevice(device: BluetoothDevice, context : Context) {
        device.connectGatt(context, false, gattCallback)
        connectedDevice = device
    }

    private val ResultScanCallback = object : ScanCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            results.add(result)
            scanResultCallback?.invoke(result)
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                println("CONNECTED")
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                gatt.close()
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                readCustomCharacteristic(gatt)
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                println(characteristic.value[1])
                val value: Int = characteristic.value[1].toInt()
                gattResultCallback?.invoke(value)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            println(characteristic.value[1])
            val value: Int = characteristic.value[1].toInt()
            gattResultCallback?.invoke(value)
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        fun readCustomCharacteristic(gatt: BluetoothGatt) {
            val SERVICE_UUID = UUID.fromString("0000180D-0000-1000-8000-00805F9B34FB")
            val CHARACTERISTIC_UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
            val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
            val service = gatt.getService(SERVICE_UUID)

            val characteristic = service?.getCharacteristic(CHARACTERISTIC_UUID)
            gatt.setCharacteristicNotification(characteristic, true)
            val descriptor = characteristic?.getDescriptor(CCCD_UUID)
            if (descriptor != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gatt.writeDescriptor(
                        descriptor,
                        BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    )
                } else {
                    @Suppress("DEPRECATION")
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    @Suppress("DEPRECATION")
                    gatt.writeDescriptor(descriptor)
                }
            } else {
                Log.e("BLE", "CCCD Descriptor not found on Heart Rate characteristic.")
            }
        }
    }

    fun getDevice(): BluetoothDevice? {
        return connectedDevice
    }
}