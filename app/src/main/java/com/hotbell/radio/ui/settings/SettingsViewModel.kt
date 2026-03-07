package com.hotbell.radio.ui.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("hotbell_prefs", Context.MODE_PRIVATE)

    private val _vibrateOnWake = MutableStateFlow(prefs.getBoolean("vibrate_on_wake", true))
    val vibrateOnWake: StateFlow<Boolean> = _vibrateOnWake.asStateFlow()

    private val _streamQuality = MutableStateFlow(prefs.getString("stream_quality", "High") ?: "High")
    val streamQuality: StateFlow<String> = _streamQuality.asStateFlow()

    fun setVibrateOnWake(vibrate: Boolean) {
        prefs.edit().putBoolean("vibrate_on_wake", vibrate).apply()
        _vibrateOnWake.value = vibrate
    }

    fun setStreamQuality(quality: String) {
        prefs.edit().putString("stream_quality", quality).apply()
        _streamQuality.value = quality
    }
}
