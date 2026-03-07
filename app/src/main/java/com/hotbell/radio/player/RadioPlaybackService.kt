package com.hotbell.radio.player

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RadioPlaybackService : MediaSessionService() {

    companion object {
        private const val TAG = "RadioPlaybackService"
        private const val CHANNEL_ID = "hotbell_radio_channel"

        private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
        val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

        private var _currentStationName: String = ""
    }

    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        player = ExoPlayer.Builder(this).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                            Log.d(TAG, "Buffering...")
                            _playbackState.value = PlaybackState.Buffering
                        }
                        Player.STATE_READY -> {
                            if (isPlaying) {
                                Log.d(TAG, "Playing: $_currentStationName")
                                _playbackState.value = PlaybackState.Playing(_currentStationName)
                            }
                        }
                        Player.STATE_IDLE -> {
                            _playbackState.value = PlaybackState.Idle
                        }
                        Player.STATE_ENDED -> {
                            _playbackState.value = PlaybackState.Idle
                        }
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) {
                        _playbackState.value = PlaybackState.Playing(_currentStationName)
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    Log.e(TAG, "Player error: ${error.message}")
                    _playbackState.value = PlaybackState.Error(
                        error.message ?: "Unknown playback error"
                    )
                }
            })
        }

        mediaSession = MediaSession.Builder(this, player!!).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            "PLAY" -> {
                val url = intent.getStringExtra("STREAM_URL") ?: return START_NOT_STICKY
                val stationName = intent.getStringExtra("STATION_NAME") ?: "Unknown Station"
                playStream(url, stationName)
            }
            "STOP" -> {
                stopPlayback()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun playStream(url: String, stationName: String) {
        _currentStationName = stationName
        _playbackState.value = PlaybackState.Buffering

        // Build MediaItem with metadata so the system media notification shows station info
        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(stationName)
                    .setArtist("HotBell Radio")
                    .setIsPlayable(true)
                    .build()
            )
            .build()

        player?.apply {
            stop()
            clearMediaItems()
            setMediaItem(mediaItem)
            prepare()
            play()
        }

        // Media3 MediaSessionService handles the foreground notification automatically
        // when a MediaSession is active and playing. No need for manual startForeground.
    }

    private fun stopPlayback() {
        player?.stop()
        player?.clearMediaItems()
        _playbackState.value = PlaybackState.Idle
        _currentStationName = ""
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Radio Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows when radio is playing"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        player = null
        _playbackState.value = PlaybackState.Idle
        super.onDestroy()
    }
}
