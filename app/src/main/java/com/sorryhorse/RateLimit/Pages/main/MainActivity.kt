package com.sorryhorse.RateLimit.Pages.main

import android.Manifest
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.sorryhorse.RateLimit.Pages.deviceBrowser.DeviceBrowser
import com.sorryhorse.RateLimit.Pages.main.components.AppCard
import com.sorryhorse.RateLimit.Pages.main.components.AppList
import com.sorryhorse.RateLimit.Pages.main.components.DeviceCard
import com.sorryhorse.RateLimit.R
import com.sorryhorse.RateLimit.models.App
import com.sorryhorse.RateLimit.services.AppService
import com.sorryhorse.RateLimit.services.BluetoothService
import com.sorryhorse.RateLimit.services.PreferencesController
import me.bastanfar.semicirclearcprogressbar.SemiCircleArcProgressBar

class MainActivity : AppCompatActivity() {
    private lateinit var prefController: PreferencesController
    private var previousDevice = ""

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
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

        prefController = PreferencesController(this, lifecycleScope)
        prefController.onThresholdChanged { value ->
            val thresholdButton = findViewById<TextView>(R.id.threshold)
            thresholdButton.text = "Threshold: $value"
        }

        prefController.onDeviceChanged { value ->
            previousDevice = value
            val bluetoothService = BluetoothService
            bluetoothService.scan(this)
        }

        prefController.onAppListChanged { value ->
            val appService = AppService(this@MainActivity)
            value.forEach { println(it) }
            updateAppList(appService.filter { value.contains(it.id) })
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
                    prefController.setThreshold(input.text.toString().toInt())
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create().show()
        }

        val edit = findViewById<TextView>(R.id.edit)
        edit.setOnClickListener {
            val appList = AppList(prefController.getAppIds())
            appList.show(supportFragmentManager, AppList.TAG)
            supportFragmentManager.setFragmentResultListener(
                "blockedApps",
                this
            ) { requestKey, bundle ->
                val appList = bundle.getSerializable("apps") as? ArrayList<String>
                    ?: return@setFragmentResultListener
                prefController.setAppSet(appList.toSet())
            }
        }
    }

    fun updateAppList(apps: List<App>) {
        val appListView = findViewById<LinearLayout>(R.id.app_list)

        appListView.removeAllViews()
        apps.forEach { item ->
            val card = AppCard(this)
            card.setData(item, false)
            appListView.addView(card)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun scanCallback(result: ScanResult) {
        if (result.device.address != previousDevice)
            return
        val bluetoothService = BluetoothService
        bluetoothService.connectToDevice(result.device, this)
        setDeviceCard()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun setDeviceCard() {
        val bluetoothService = BluetoothService
        if (bluetoothService.getDevice() == null)
            return
        val deviceCard = findViewById<DeviceCard>(R.id.deviceCard)
        if (deviceCard.isConnected())
            return
        deviceCard.setDevice(bluetoothService.getDevice())
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun setHeartRate(heartRate: Int) {
        val progress = findViewById<SemiCircleArcProgressBar>(R.id.graph)
        val heartRateTitle = findViewById<TextView>(R.id.heart_rate)


        val threshold = prefController.getThreshold()
        val percent = rateToPercent(heartRate, threshold)
        runOnUiThread {
            setDeviceCard()

            progress.setPercent(percent)

            heartRateTitle.text = heartRate.toString()

            if (heartRate < threshold) {
                progress.setProgressBarColor(ContextCompat.getColor(this, R.color.red))
            } else {
                progress.setProgressBarColor(ContextCompat.getColor(this, R.color.primary))
            }
        }
    }

    fun rateToPercent(rate: Int, threshold: Int): Int {
        return 100.coerceAtMost(100 * rate / threshold)
    }
}