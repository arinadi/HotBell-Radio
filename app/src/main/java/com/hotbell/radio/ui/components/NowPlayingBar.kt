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
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

private val SLEEP_TIMER_OPTIONS = listOf(15, 30, 45, 60, 90)

@Composable
fun NowPlayingBar() {
    val playbackState by RadioPlayerManager.playbackState.collectAsState()
    val sleepTimerRemaining by RadioPlayerManager.sleepTimerRemaining.collectAsState()
    val context = LocalContext.current

    val isVisible = playbackState !is PlaybackState.Idle && playbackState !is PlaybackState.Error
    var showSleepMenu by remember { mutableStateOf(false) }

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
                        // Show sleep timer countdown if active
                        if (sleepTimerRemaining > 0) {
                            val mins = sleepTimerRemaining / 60
                            val secs = sleepTimerRemaining % 60
                            Text(
                                text = "\uD83C\uDF19 ${String.format("%d:%02d", mins, secs)}",
                                color = Color(0xFF90CAF9),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Sleep Timer Button
                    Box {
                        IconButton(
                            onClick = {
                                if (sleepTimerRemaining > 0) {
                                    RadioPlayerManager.cancelSleepTimer(context)
                                } else {
                                    showSleepMenu = true
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (sleepTimerRemaining > 0) Color(0xFF90CAF9).copy(alpha = 0.2f)
                                    else Color.White.copy(alpha = 0.1f)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.NightsStay,
                                contentDescription = "Sleep Timer",
                                tint = if (sleepTimerRemaining > 0) Color(0xFF90CAF9) else Color.Gray
                            )
                        }
                        DropdownMenu(
                            expanded = showSleepMenu,
                            onDismissRequest = { showSleepMenu = false },
                            modifier = Modifier.background(DarkGray)
                        ) {
                            SLEEP_TIMER_OPTIONS.forEach { minutes ->
                                DropdownMenuItem(
                                    text = { Text("$minutes min", color = Color.White) },
                                    onClick = {
                                        RadioPlayerManager.setSleepTimer(context, minutes)
                                        showSleepMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

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
