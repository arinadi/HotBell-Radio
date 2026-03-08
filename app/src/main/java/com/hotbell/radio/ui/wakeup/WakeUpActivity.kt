package com.hotbell.radio.ui.wakeup

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.view.KeyEvent
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hotbell.radio.ui.theme.HotBellTheme
import com.hotbell.radio.ui.theme.PitchBlack

class WakeUpActivity : ComponentActivity() {

    private val viewModel: WakeUpViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setupLockScreenBypass()
        turnScreenOnAndKeyguardOff()
        hideSystemUI()

        val stationUuid = intent.getStringExtra("EXTRA_STATION_UUID")
        val stationName = intent.getStringExtra("EXTRA_STATION_NAME")
        val stationUrl = intent.getStringExtra("EXTRA_STATION_URL")
        val snoozeDurationMin = intent.getIntExtra("EXTRA_SNOOZE_DURATION", 5)
        val maxSnoozeCount = intent.getIntExtra("EXTRA_MAX_SNOOZE", 3)
        val autoDismissMin = intent.getIntExtra("EXTRA_AUTO_DISMISS", 10)
        val dismissType = intent.getStringExtra("EXTRA_DISMISS_TYPE") ?: "math"
        val targetPhotoPath = intent.getStringExtra("EXTRA_TARGET_PHOTO_PATH")

        setContent {
            // Trigger playback on launch
            androidx.compose.runtime.LaunchedEffect(Unit) {
                viewModel.startAlarm(this@WakeUpActivity, stationUuid, stationName, stationUrl)
                viewModel.configureSnooze(snoozeDurationMin, maxSnoozeCount, autoDismissMin)
                viewModel.configureDismissType(dismissType, targetPhotoPath)
            }

            HotBellTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = PitchBlack
                ) {
                    WakeUpScreen(
                        viewModel = viewModel,
                        stationName = stationName,
                        onDismissed = {
                            finishAndRemoveTask()
                        }
                    )
                }
            }
        }
    }

    private fun setupLockScreenBypass() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    @Suppress("DEPRECATION")
    private fun turnScreenOnAndKeyguardOff() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!powerManager.isInteractive) {
            val wakeLock = powerManager.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                "HotBell:AlarmWakelockTag"
            )
            wakeLock.acquire(3 * 60 * 1000L /*3 minutes*/)
        }
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_POWER,
            KeyEvent.KEYCODE_CAMERA -> true // Consume event
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_POWER,
            KeyEvent.KEYCODE_CAMERA -> true // Consume event
            else -> super.onKeyUp(keyCode, event)
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (viewModel.isAlarmActive.value) {
            // The user pressed Home or Recent Apps. Bring the activity back to the front immediately.
            val i = android.content.Intent(this, WakeUpActivity::class.java)
            i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(i)
        }
    }
}
