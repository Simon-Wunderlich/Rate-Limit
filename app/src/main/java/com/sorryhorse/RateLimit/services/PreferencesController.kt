package com.sorryhorse.RateLimit.services

import android.content.Context
import com.sorryhorse.RateLimit.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class PreferencesController(private val context: Context, private val scope: CoroutineScope) {
    private var thresholdCallback: ((Int) -> Unit)? = null
    private var threshold: Int? = null
    private var deviceCallback: ((String) -> Unit)? = null
    private var device: String? = null
    private var appCallback: ((Set<String>) -> Unit)? = null
    private var apps: Set<String> = setOf()

    init {
        scope.launch {
            val preferences = Preferences(context)
            preferences.thresholdFlow.collect { value ->
                threshold = value
                thresholdCallback?.invoke(value)
            }
        }
        scope.launch {
            val preferences = Preferences(context)
            preferences.deviceFlow.collect { value ->
                device = value
                deviceCallback?.invoke(value)
            }
        }

        scope.launch {
            val preferences = Preferences(context)
            preferences.appFlow.collect { value ->
                apps = value
                appCallback?.invoke(value)
            }
        }
    }

    fun setThreshold(threshold: Int) {
        this.threshold = threshold
        scope.launch {
            val preferences = Preferences(context)
            preferences.saveThreshold(threshold)
        }
    }

    fun setDevice(device: String) {
        this.device = device
        scope.launch {
            val preferences = Preferences(context)
            preferences.saveDevice(device)
        }
    }

    fun setAppSet(appSet: Set<String>) {
        this.apps = appSet
        scope.launch {
            val preferences = Preferences(context)
            preferences.saveApps(appSet)
        }
    }


    fun onThresholdChanged(callback: (Int) -> Unit) {
        thresholdCallback = callback
    }

    fun onDeviceChanged(callback: (String) -> Unit) {
        deviceCallback = callback
    }

    fun onAppListChanged(callback: (Set<String>) -> Unit) {
        appCallback = callback
    }

    fun getDevice(): String? {
        return device
    }

    fun getThreshold(): Int {
        return threshold ?: 150
    }

    fun getAppIds(): Set<String> {
        return apps
    }
}