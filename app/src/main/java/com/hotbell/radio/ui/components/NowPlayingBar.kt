package com.hotbell.radio.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hotbell.radio.player.PlaybackState
import com.hotbell.radio.player.RadioPlayerManager
import com.hotbell.radio.ui.theme.DarkGray
import com.hotbell.radio.ui.theme.HotBellOrange
import com.hotbell.radio.ui.theme.PitchBlack
import com.hotbell.radio.ui.wakeup.VisualEqualizer

@Composable
fun NowPlayingBar() {
    val playbackState by RadioPlayerManager.playbackState.collectAsState()
    val context = LocalContext.current

    val isVisible = playbackState !is PlaybackState.Idle && playbackState !is PlaybackState.Error

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(PitchBlack)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkGray),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Playback Indicator
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        when (playbackState) {
                            is PlaybackState.Buffering -> {
                                CircularProgressIndicator(
                                    color = HotBellOrange,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                            is PlaybackState.Playing -> {
                                VisualEqualizer()
                            }
                            else -> {}
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Station Info
                    val stationName = when (val state = playbackState) {
                        is PlaybackState.Playing -> state.stationName
                        is PlaybackState.Buffering -> "Buffering..."
                        else -> ""
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "LIVE RADIO",
                            color = HotBellOrange,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = stationName,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Stop Button
                    IconButton(
                        onClick = { RadioPlayerManager.stop(context) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop Radio",
                            tint = HotBellOrange
                        )
                    }
                }
            }
        }
    }
}
