package com.hotbell.radio

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hotbell.radio.network.RadioRepository
import com.hotbell.radio.player.PlaybackState
import com.hotbell.radio.player.RadioPlayerManager
import com.hotbell.radio.ui.theme.ElectricBlue
import com.hotbell.radio.ui.theme.HotBellTheme
import com.hotbell.radio.ui.theme.NeonRed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val repository = RadioRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HotBellTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RadioTestScreen()
                }
            }
        }
    }

    @Composable
    fun RadioTestScreen() {
        val playbackState by RadioPlayerManager.playbackState.collectAsState()
        var statusText by remember { mutableStateOf("Ready to test") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "HotBell Radio",
                color = ElectricBlue,
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Player: ${playbackState.toDisplayString()}",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = statusText,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    statusText = "Searching jazz stations..."
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val stations = repository.searchStations(name = "jazz")
                            val msg = "Found ${stations.size} stations"
                            Log.d("RadioTest", msg)
                            stations.take(3).forEach {
                                Log.d("RadioTest", "  - ${it.name} (${it.urlResolved})")
                            }
                            statusText = msg
                            if (stations.isNotEmpty()) {
                                val first = stations.first()
                                statusText = "$msg\nPlaying: ${first.name}"
                                RadioPlayerManager.play(
                                    this@MainActivity,
                                    first.urlResolved,
                                    first.name
                                )
                            }
                        } catch (e: Exception) {
                            Log.e("RadioTest", "Error: ${e.message}")
                            statusText = "Error: ${e.message}"
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Search & Play Jazz", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        RadioPlayerManager.stop(this@MainActivity)
                        statusText = "Stopped"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Stop")
                }

                Button(
                    onClick = {
                        statusText = "Testing invalid URL..."
                        RadioPlayerManager.play(
                            this@MainActivity,
                            "http://invalid.stream.url/test",
                            "Invalid Test"
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Test Error")
                }
            }
        }
    }

    private fun PlaybackState.toDisplayString(): String {
        return when (this) {
            is PlaybackState.Idle -> "Idle"
            is PlaybackState.Buffering -> "Buffering..."
            is PlaybackState.Playing -> "Playing: $stationName"
            is PlaybackState.Error -> "Error: $message"
        }
    }
}
