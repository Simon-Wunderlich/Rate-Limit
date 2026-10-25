package com.sorryhorse.RateLimit

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore by preferencesDataStore(name="preferences")

class Preferences(private val context: Context) {
    companion object {
        val THRESHOLD_KEY = intPreferencesKey("threshold")
        val DEVICE_KEY = stringPreferencesKey("device")
    }

    val thresholdFlow: Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[THRESHOLD_KEY] ?: 145
        }

    val deviceFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[DEVICE_KEY] ?: ""
        }

    suspend fun saveThreshold(threshold: Int) {
        context.dataStore.edit { preferences ->
            preferences[THRESHOLD_KEY] = threshold
        }
    }

    suspend fun saveDevice(device: String) {
        context.dataStore.edit { preferences ->
            preferences[DEVICE_KEY] = device
        }
    }
}