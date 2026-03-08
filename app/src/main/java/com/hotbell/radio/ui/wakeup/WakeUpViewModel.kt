package com.hotbell.radio.ui.wakeup

import android.app.Application
import android.content.Context
import android.hardware.camera2.CameraManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hotbell.radio.data.AlarmLogEntity
import com.hotbell.radio.data.AppDatabase
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
    private val alarmLogDao = AppDatabase.getInstance(application).alarmLogDao()
    private var vibrator: Vibrator? = null
    private var alarmFiredTime: Long = 0L
    
    private val _challenge = MutableStateFlow(MathChallengeGenerator.generate())
    val challenge: StateFlow<MathChallenge> = _challenge.asStateFlow()

    private val _isFallbackActive = MutableStateFlow(false)
    val isFallbackActive: StateFlow<Boolean> = _isFallbackActive.asStateFlow()

    // Snooze config
    private var snoozeDurationMin = 5
    private var maxSnoozeCount = 3
    private var autoDismissMin = 10
    private var currentSnoozeCount = 0
    private var autoDismissJob: Job? = null

    private val _canSnooze = MutableStateFlow(true)
    val canSnooze: StateFlow<Boolean> = _canSnooze.asStateFlow()

    private val _snoozeCountRemaining = MutableStateFlow(3)
    val snoozeCountRemaining: StateFlow<Int> = _snoozeCountRemaining.asStateFlow()

    // Station info saved from startAlarm for snooze re-scheduling
    private var alarmStationUuid: String? = null
    private var alarmStationName: String? = null
    private var alarmStationUrl: String? = null

    private var flashlightJob: Job? = null

    private var fallbackTimeoutJob: Job? = null

    // Photo Match
    private val _dismissType = MutableStateFlow("math")
    val dismissType: StateFlow<String> = _dismissType.asStateFlow()

    private val _targetPhotoPath = MutableStateFlow<String?>(null)
    val targetPhotoPath: StateFlow<String?> = _targetPhotoPath.asStateFlow()

    private val _verificationResult = MutableStateFlow<com.hotbell.radio.alarms.VerificationResult?>(null)
    val verificationResult: StateFlow<com.hotbell.radio.alarms.VerificationResult?> = _verificationResult.asStateFlow()

    private val _isVerifying = MutableStateFlow(false)
    val isVerifying: StateFlow<Boolean> = _isVerifying.asStateFlow()

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

        // Save station info for snooze
        alarmStationUuid = stationUuid
        alarmStationName = stationName
        alarmStationUrl = stationUrl
        alarmFiredTime = System.currentTimeMillis()

        // Log fired event
        viewModelScope.launch {
            alarmLogDao.insert(AlarmLogEntity(
                alarmId = stationUuid ?: "unknown",
                eventType = "fired"
            ))
        }

        // Start continuous vibration if enabled in settings
        val prefs = getApplication<Application>().getSharedPreferences("hotbell_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("vibrate_on_wake", true)) {
            startContinuousVibration()
        }

        // Start flashlight blink if enabled
        if (prefs.getBoolean("flashlight_on_wake", false)) {
            startFlashlightBlink()
        }

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

    fun configureSnooze(snoozeDuration: Int, maxSnooze: Int, autoDismiss: Int) {
        snoozeDurationMin = snoozeDuration
        maxSnoozeCount = maxSnooze
        autoDismissMin = autoDismiss
        _snoozeCountRemaining.value = maxSnooze
        _canSnooze.value = maxSnooze > 0

        // Start auto-dismiss timer
        autoDismissJob?.cancel()
        autoDismissJob = viewModelScope.launch {
            delay(autoDismiss * 60 * 1000L)
            android.util.Log.d("WakeUpViewModel", "Auto-dismiss triggered after $autoDismiss minutes")
            alarmLogDao.insert(AlarmLogEntity(
                alarmId = alarmStationUuid ?: "unknown",
                eventType = "auto_dismissed",
                responseTimeMs = System.currentTimeMillis() - alarmFiredTime
            ))
            dismissAlarm {}
        }
    }

    fun configureDismissType(type: String, targetPath: String?) {
        _dismissType.value = type
        _targetPhotoPath.value = targetPath
    }

    fun verifyPhoto(capturedImagePath: String) {
        val apiKey = getApplication<Application>().getSharedPreferences("hotbell_prefs", Context.MODE_PRIVATE)
            .getString("gemini_api_key", "") ?: ""
        
        val verifier = com.hotbell.radio.alarms.GeminiVerifier(apiKey)
        _isVerifying.value = true
        _verificationResult.value = null

        viewModelScope.launch {
            val result = verifier.verifyMatch(_targetPhotoPath.value ?: "", capturedImagePath)
            _verificationResult.value = result
            _isVerifying.value = false
            
            if (result.match) {
                dismissAlarm {}
            }
        }
    }

    fun snoozeAlarm(onSnoozed: () -> Unit) {
        if (currentSnoozeCount >= maxSnoozeCount) return
        currentSnoozeCount++
        _snoozeCountRemaining.value = maxSnoozeCount - currentSnoozeCount
        _canSnooze.value = currentSnoozeCount < maxSnoozeCount

        android.util.Log.d("WakeUpViewModel", "Snoozing alarm ($currentSnoozeCount/$maxSnoozeCount) for $snoozeDurationMin min")

        // Log snoozed event
        viewModelScope.launch {
            alarmLogDao.insert(AlarmLogEntity(
                alarmId = alarmStationUuid ?: "unknown",
                eventType = "snoozed",
                responseTimeMs = System.currentTimeMillis() - alarmFiredTime
            ))
        }

        // Stop current alarm
        vibrator?.cancel()
        flashlightJob?.cancel()
        stopFlashlight()
        autoDismissJob?.cancel()
        fallbackTimeoutJob?.cancel()
        RadioPlayerManager.stop(getApplication())
        stopFallback()

        // Schedule snooze via AlarmManager
        val context = getApplication<Application>() as Context
        val snoozeIntent = Intent(context, com.hotbell.radio.alarms.AlarmReceiver::class.java).apply {
            putExtra("EXTRA_ALARM_ID", "SNOOZE_${System.currentTimeMillis()}")
            putExtra("EXTRA_STATION_UUID", alarmStationUuid ?: "")
            putExtra("EXTRA_STATION_NAME", alarmStationName ?: "Alarm Station")
            putExtra("EXTRA_STATION_URL", alarmStationUrl ?: "")
            putExtra("EXTRA_SNOOZE_DURATION", snoozeDurationMin)
            putExtra("EXTRA_MAX_SNOOZE", maxSnoozeCount - currentSnoozeCount)
            putExtra("EXTRA_AUTO_DISMISS", autoDismissMin)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            "SNOOZE".hashCode(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = System.currentTimeMillis() + (snoozeDurationMin * 60 * 1000L)
        try {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerTime, pendingIntent),
                pendingIntent
            )
        } catch (e: SecurityException) {
            android.util.Log.e("WakeUpViewModel", "Failed to schedule snooze alarm", e)
        }
        onSnoozed()
    }

    fun dismissAlarm(onDismissed: () -> Unit) {
        android.util.Log.d("WakeUpViewModel", "Dismissing alarm")

        // Log dismissed event with response time
        val responseTime = System.currentTimeMillis() - alarmFiredTime
        if (alarmFiredTime > 0) {
            viewModelScope.launch {
                alarmLogDao.insert(AlarmLogEntity(
                    alarmId = alarmStationUuid ?: "unknown",
                    eventType = "dismissed",
                    responseTimeMs = responseTime
                ))
            }
            alarmFiredTime = 0L
        }

        vibrator?.cancel()
        flashlightJob?.cancel()
        stopFlashlight()
        autoDismissJob?.cancel()
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

    private fun startContinuousVibration() {
        val app = getApplication<Application>()
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = app.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            app.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        // Repeating pattern: vibrate 800ms, pause 400ms
        val timings = longArrayOf(0, 800, 400)
        val amplitudes = intArrayOf(0, 255, 0)
        vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, 0))
    }

    private fun startFlashlightBlink() {
        val app = getApplication<Application>()
        val cameraManager = app.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return
        val cameraId = try {
            cameraManager.cameraIdList.firstOrNull()
        } catch (e: Exception) {
            null
        } ?: return

        flashlightJob = viewModelScope.launch {
            try {
                while (true) {
                    cameraManager.setTorchMode(cameraId, true)
                    delay(500)
                    cameraManager.setTorchMode(cameraId, false)
                    delay(500)
                }
            } catch (e: Exception) {
                android.util.Log.e("WakeUpViewModel", "Flashlight error", e)
            }
        }
    }

    private fun stopFlashlight() {
        try {
            val app = getApplication<Application>()
            val cameraManager = app.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            val cameraId = cameraManager?.cameraIdList?.firstOrNull()
            if (cameraId != null) {
                cameraManager.setTorchMode(cameraId, false)
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    override fun onCleared() {
        super.onCleared()
        vibrator?.cancel()
        flashlightJob?.cancel()
        stopFlashlight()
        autoDismissJob?.cancel()
        ringtoneFallbackManager.stop()
        RadioPlayerManager.stop(getApplication())
    }
}
