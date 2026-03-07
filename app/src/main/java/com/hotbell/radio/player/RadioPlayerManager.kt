package com.hotbell.radio.player

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.StateFlow

object RadioPlayerManager {

    val playbackState: StateFlow<PlaybackState>
        get() = RadioPlaybackService.playbackState

    fun play(context: Context, streamUrl: String, stationName: String) {
        val intent = Intent(context, RadioPlaybackService::class.java).apply {
            action = "PLAY"
            putExtra("STREAM_URL", streamUrl)
            putExtra("STATION_NAME", stationName)
        }
        context.startForegroundService(intent)
    }

    fun stop(context: Context) {
        val intent = Intent(context, RadioPlaybackService::class.java).apply {
            action = "STOP"
        }
        context.startService(intent)
    }
}
