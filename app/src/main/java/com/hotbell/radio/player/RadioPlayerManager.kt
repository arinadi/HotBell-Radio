package com.hotbell.radio.player

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.StateFlow

object RadioPlayerManager {

    val playbackState: StateFlow<PlaybackState>
        get() = RadioPlaybackService.playbackState

    val sleepTimerRemaining: StateFlow<Long>
        get() = RadioPlaybackService.sleepTimerRemaining

    fun play(context: Context, streamUrl: String, stationName: String) {
        val intent = Intent(context, RadioPlaybackService::class.java).apply {
            action = "PLAY"
            putExtra("STREAM_URL", streamUrl)
            putExtra("STATION_NAME", stationName)
        }
        try {
            context.startForegroundService(intent)
        } catch (e: Exception) {
            android.util.Log.e("RadioPlayerManager", "Failed to start foreground service", e)
            try {
                context.startService(intent)
            } catch (fallbackEx: Exception) {
                android.util.Log.e("RadioPlayerManager", "Also failed to start normal service", fallbackEx)
            }
        }
    }

    fun stop(context: Context) {
        val intent = Intent(context, RadioPlaybackService::class.java).apply {
            action = "STOP"
        }
        context.startService(intent)
    }

    fun setSleepTimer(context: Context, minutes: Int) {
        val intent = Intent(context, RadioPlaybackService::class.java).apply {
            action = "SLEEP_TIMER"
            putExtra("SLEEP_MINUTES", minutes)
        }
        context.startService(intent)
    }

    fun cancelSleepTimer(context: Context) {
        val intent = Intent(context, RadioPlaybackService::class.java).apply {
            action = "CANCEL_SLEEP_TIMER"
        }
        context.startService(intent)
    }
}
