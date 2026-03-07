package com.hotbell.radio.ui.wakeup

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hotbell.radio.player.PlaybackState
import com.hotbell.radio.player.RadioPlayerManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WakeUpViewModel(application: Application) : AndroidViewModel(application) {

    private val ringtoneFallbackManager = RingtoneFallbackManager(application)
    
    private val _challenge = MutableStateFlow(MathChallengeGenerator.generate())
    val challenge: StateFlow<MathChallenge> = _challenge.asStateFlow()

    private val _isFallbackActive = MutableStateFlow(false)
    val isFallbackActive: StateFlow<Boolean> = _isFallbackActive.asStateFlow()

    private var fallbackTimeoutJob: Job? = null

    init {
        // Monitor playback state to stop fallback if radio eventually successfully plays
        viewModelScope.launch {
            RadioPlayerManager.playbackState.collect { state ->
                if (state is PlaybackState.Playing) {
                    fallbackTimeoutJob?.cancel()
                    stopFallback()
                } else if (state is PlaybackState.Error && !_isFallbackActive.value) {
                    startFallback()
                }
            }
        }
    }

    fun startAlarm(context: Context, stationUuid: String?, stationName: String?, stationUrl: String? = null) {
        android.util.Log.d("WakeUpViewModel", "startAlarm called with stationUuid=$stationUuid, stationName=$stationName, stationUrl=$stationUrl")
        viewModelScope.launch {
            if (stationUuid == null && stationUrl == null) {
                android.util.Log.e("WakeUpViewModel", "Both stationUuid and stationUrl are null, triggering fallback")
                startFallback()
                return@launch
            }
            if (!hasInternetConnection()) {
                android.util.Log.e("WakeUpViewModel", "No internet connection, triggering fallback")
                startFallback()
                return@launch
            }

            // Use passed URL if available, otherwise fetch from DB
            val finalUrl = stationUrl ?: run {
                val db = com.hotbell.radio.data.AppDatabase.getInstance(getApplication())
                db.favoriteStationDao().getByUuid(stationUuid!!)?.urlResolved
            }

            if (finalUrl == null) {
                android.util.Log.e("WakeUpViewModel", "Failed to resolve stationUrl, triggering fallback")
                startFallback()
                return@launch
            }

            android.util.Log.d("WakeUpViewModel", "Trying to play radio stream: $finalUrl")
            RadioPlayerManager.play(context, finalUrl, stationName ?: "Alarm Station")

            // Wait 30 seconds, if not playing, start fallback
            fallbackTimeoutJob = viewModelScope.launch {
                delay(30000)
                val currentState = RadioPlayerManager.playbackState.value
                android.util.Log.d("WakeUpViewModel", "30-second timeout reached. currentState=$currentState")
                if (currentState !is PlaybackState.`Playing`) {
                    android.util.Log.e("WakeUpViewModel", "Not playing after 30 seconds, triggering fallback")
                    startFallback()
                } else {
                    android.util.Log.d("WakeUpViewModel", "Radio is playing after 30 seconds, no fallback needed")
                }
            }
        }
    }

    private fun startFallback() {
        if (!_isFallbackActive.value) {
            android.util.Log.d("WakeUpViewModel", "Starting fallback ringtone. Current Radio State: ${RadioPlayerManager.playbackState.value}")
            _isFallbackActive.value = true
            ringtoneFallbackManager.playFallbackAlarm()
            RadioPlayerManager.stop(getApplication())
        }
    }

    private fun stopFallback() {
        if (_isFallbackActive.value) {
            android.util.Log.d("WakeUpViewModel", "Stopping fallback ringtone")
            _isFallbackActive.value = false
            ringtoneFallbackManager.stop()
        }
    }

    fun generateNewChallenge() {
        _challenge.value = MathChallengeGenerator.generate()
    }

    fun dismissAlarm(onDismissed: () -> Unit) {
        android.util.Log.d("WakeUpViewModel", "Dismissing alarm")
        fallbackTimeoutJob?.cancel()
        RadioPlayerManager.stop(getApplication())
        stopFallback()
        onDismissed()
    }

    private fun hasInternetConnection(): Boolean {
        val cm = getApplication<Application>().getSystemService(ConnectivityManager::class.java)
        val network = cm.activeNetwork
        if (network == null) {
            android.util.Log.w("WakeUpViewModel", "hasInternetConnection: activeNetwork is null")
            return false
        }
        val capabilities = cm.getNetworkCapabilities(network)
        if (capabilities == null) {
            android.util.Log.w("WakeUpViewModel", "hasInternetConnection: capabilities are null")
            return false
        }
        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        android.util.Log.d("WakeUpViewModel", "hasInternetConnection: $hasInternet")
        return hasInternet
    }

    override fun onCleared() {
        super.onCleared()
        ringtoneFallbackManager.stop()
        RadioPlayerManager.stop(getApplication())
    }
}
