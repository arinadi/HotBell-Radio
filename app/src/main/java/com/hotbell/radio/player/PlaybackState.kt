package com.hotbell.radio.player

sealed class PlaybackState {
    data object Idle : PlaybackState()
    data object Buffering : PlaybackState()
    data class Playing(val stationName: String) : PlaybackState()
    data class Error(val message: String) : PlaybackState()
}
