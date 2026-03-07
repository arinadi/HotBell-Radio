package com.hotbell.radio.ui.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hotbell.radio.network.GithubUpdater
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("hotbell_prefs", Context.MODE_PRIVATE)
    private val updater = GithubUpdater(application)

    // Vibrate on Wake
    private val _vibrateOnWake = MutableStateFlow(prefs.getBoolean("vibrate_on_wake", true))
    val vibrateOnWake: StateFlow<Boolean> = _vibrateOnWake.asStateFlow()

    // Alarm Config
    private val _startVolume = MutableStateFlow(prefs.getInt("alarm_start_volume", 10))
    val startVolume: StateFlow<Int> = _startVolume.asStateFlow()

    private val _maxBoost = MutableStateFlow(prefs.getInt("alarm_max_boost", 150))
    val maxBoost: StateFlow<Int> = _maxBoost.asStateFlow()

    private val _crescendoSec = MutableStateFlow(prefs.getInt("alarm_crescendo_sec", 60))
    val crescendoSec: StateFlow<Int> = _crescendoSec.asStateFlow()

    private val _dismissHoldSec = MutableStateFlow(prefs.getInt("alarm_dismiss_hold_sec", 3))
    val dismissHoldSec: StateFlow<Int> = _dismissHoldSec.asStateFlow()

    fun setVibrateOnWake(vibrate: Boolean) {
        prefs.edit().putBoolean("vibrate_on_wake", vibrate).apply()
        _vibrateOnWake.value = vibrate
    }

    // Flashlight on Wake
    private val _flashlightOnWake = MutableStateFlow(prefs.getBoolean("flashlight_on_wake", false))
    val flashlightOnWake: StateFlow<Boolean> = _flashlightOnWake.asStateFlow()

    fun setFlashlightOnWake(enabled: Boolean) {
        prefs.edit().putBoolean("flashlight_on_wake", enabled).apply()
        _flashlightOnWake.value = enabled
    }

    fun setStartVolume(value: Int) {
        prefs.edit().putInt("alarm_start_volume", value).apply()
        _startVolume.value = value
    }

    fun setMaxBoost(value: Int) {
        prefs.edit().putInt("alarm_max_boost", value).apply()
        _maxBoost.value = value
    }

    fun setCrescendoSec(value: Int) {
        prefs.edit().putInt("alarm_crescendo_sec", value).apply()
        _crescendoSec.value = value
    }

    fun setDismissHoldSec(value: Int) {
        prefs.edit().putInt("alarm_dismiss_hold_sec", value).apply()
        _dismissHoldSec.value = value
    }

    fun checkForUpdates(manual: Boolean = false) {
        viewModelScope.launch {
            updater.checkAndInstallUpdate(manual)
        }
    }
}
