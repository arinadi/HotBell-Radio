package com.hotbell.radio.ui.wakeup

import android.app.Application
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

    fun startAlarm(stationUrl: String?, stationName: String?) {
        if (stationUrl == null || !hasInternetConnection()) {
            startFallback()
            return
        }

        RadioPlayerManager.play(getApplication(), stationUrl, stationName ?: "Alarm Station")

        // Wait 3 seconds, if not playing, start fallback
        fallbackTimeoutJob = viewModelScope.launch {
            delay(3000)
            if (RadioPlayerManager.playbackState.value !is PlaybackState.Playing) {
                startFallback()
            }
        }
    }

    private fun startFallback() {
        if (!_isFallbackActive.value) {
            _isFallbackActive.value = true
            ringtoneFallbackManager.playFallbackAlarm()
            RadioPlayerManager.stop(getApplication())
        }
    }

    private fun stopFallback() {
        if (_isFallbackActive.value) {
            _isFallbackActive.value = false
            ringtoneFallbackManager.stop()
        }
    }

    fun generateNewChallenge() {
        _challenge.value = MathChallengeGenerator.generate()
    }

    fun dismissAlarm(onDismissed: () -> Unit) {
        fallbackTimeoutJob?.cancel()
        RadioPlayerManager.stop(getApplication())
        stopFallback()
        onDismissed()
    }

    private fun hasInternetConnection(): Boolean {
        val cm = getApplication<Application>().getSystemService(ConnectivityManager::class.java)
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onCleared() {
        super.onCleared()
        ringtoneFallbackManager.stop()
        RadioPlayerManager.stop(getApplication())
    }
}
