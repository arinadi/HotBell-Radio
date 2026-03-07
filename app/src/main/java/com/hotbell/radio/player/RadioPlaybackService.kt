package com.hotbell.radio.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.audiofx.LoudnessEnhancer
import android.os.Build
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import com.hotbell.radio.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RadioPlaybackService : MediaSessionService() {

    companion object {
        private const val TAG = "RadioPlaybackService"
        private const val CHANNEL_ID = "hotbell_radio_channel"
        private const val NOTIFICATION_ID = 1

        private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
        val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

        private var _currentStationName: String = ""
    }

    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var fadeJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main)

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                androidx.media3.common.AudioAttributes.Builder()
                    .setUsage(androidx.media3.common.C.USAGE_MEDIA)
                    .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true // Handle audio focus automatically
            )
            .build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                            Log.d(TAG, "Buffering...")
                            _playbackState.value = PlaybackState.Buffering
                        }
                        Player.STATE_READY -> {
                            Log.d(TAG, "Player State Ready. isPlaying=$isPlaying")
                            if (playWhenReady) {
                                Log.d(TAG, "Playing: $_currentStationName")
                                _playbackState.value = PlaybackState.Playing(_currentStationName)
                                // Update notification with proper media style when ready
                                updateNotification(_currentStationName)
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
        Log.d(TAG, "onStartCommand: action=$action")
        when (action) {
            "PLAY" -> {
                val url = intent.getStringExtra("STREAM_URL")
                val stationName = intent.getStringExtra("STATION_NAME") ?: "Unknown Station"
                Log.d(TAG, "onStartCommand PLAY: url=$url, stationName=$stationName")
                if (url != null) {
                    playStream(url, stationName)
                } else {
                    Log.e(TAG, "onStartCommand PLAY: URL is null!")
                }
            }
            "STOP" -> {
                Log.d(TAG, "onStartCommand STOP")
                stopPlayback()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @OptIn(UnstableApi::class)
    private fun playStream(url: String, stationName: String) {
        Log.d(TAG, "playStream: url=$url, stationName=$stationName")
        _currentStationName = stationName
        _playbackState.value = PlaybackState.Buffering

        // Build MediaItem with metadata for system media controls
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
            volume = 0.1f // Start at 10%
            prepare()
            play()
        }

        // MUST call startForeground immediately to avoid ForegroundServiceDidNotStartInTimeException
        startForeground(NOTIFICATION_ID, buildMediaStyleNotification(stationName))
        
        startVolumeCrescendo()
    }

    private fun stopPlayback() {
        fadeJob?.cancel()
        loudnessEnhancer?.release()
        loudnessEnhancer = null
        player?.stop()
        player?.clearMediaItems()
        _playbackState.value = PlaybackState.Idle
        _currentStationName = ""
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    @OptIn(UnstableApi::class)
    private fun buildMediaStyleNotification(stationName: String): Notification {
        val session = mediaSession ?: return buildFallbackNotification(stationName)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(stationName)
            .setContentText("HotBell Radio")
            .setSmallIcon(R.drawable.ic_radio)
            .setStyle(MediaStyleNotificationHelper.MediaStyle(session))
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun buildFallbackNotification(stationName: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(stationName)
            .setContentText("HotBell Radio")
            .setSmallIcon(R.drawable.ic_radio)
            .setOngoing(true)
            .build()
    }

    @OptIn(UnstableApi::class)
    private fun updateNotification(stationName: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildMediaStyleNotification(stationName))
    }

    private fun createNotificationChannel() {
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

    @OptIn(UnstableApi::class)
    private fun startVolumeCrescendo() {
        fadeJob?.cancel()
        loudnessEnhancer?.release()
        
        val exoPlayer = player ?: return
        
        // Ensure LoudnessEnhancer is attached to the player's sessionId
        try {
            loudnessEnhancer = LoudnessEnhancer(exoPlayer.audioSessionId).apply {
                setTargetGain(0)
                enabled = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize LoudnessEnhancer", e)
        }

        fadeJob = serviceScope.launch {
            // Stage 1: Native player volume 10% to 100% over 40 seconds
            val stage1Seconds = 40
            val baseVol = 0.1f
            for (i in 1..stage1Seconds) {
                if (exoPlayer.playbackState != Player.STATE_IDLE && exoPlayer.playbackState != Player.STATE_ENDED) {
                    exoPlayer.volume = baseVol + ((1f - baseVol) * (i.toFloat() / stage1Seconds.toFloat()))
                    Log.d(TAG, "Crescendo Stage 1: native volume = ${exoPlayer.volume}")
                }
                delay(1000)
            }

            // Stage 2: LoudnessEnhancer gain 0 to +4000mB (approx 150% perceived) over 20 seconds
            val stage2Seconds = 20
            val maxGainMB = 4000 // milliBels
            
            for (i in 1..stage2Seconds) {
                if (exoPlayer.playbackState != Player.STATE_IDLE && exoPlayer.playbackState != Player.STATE_ENDED) {
                    val gain = (i.toFloat() / stage2Seconds.toFloat() * maxGainMB).toInt()
                    loudnessEnhancer?.setTargetGain(gain)
                    Log.d(TAG, "Crescendo Stage 2: enhancer gain = $gain mB")
                }
                delay(1000)
            }
            Log.d(TAG, "Crescendo complete")
        }
    }

    override fun onDestroy() {
        fadeJob?.cancel()
        loudnessEnhancer?.release()
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
