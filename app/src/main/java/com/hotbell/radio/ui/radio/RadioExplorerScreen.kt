package com.hotbell.radio.ui.radio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hotbell.radio.network.StationNetworkModel
import com.hotbell.radio.player.PlaybackState
import com.hotbell.radio.ui.theme.DarkGray
import com.hotbell.radio.ui.theme.ElectricBlue
import com.hotbell.radio.ui.theme.NeonRed
import com.hotbell.radio.ui.theme.PitchBlack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioExplorerScreen(
    viewModel: RadioViewModel,
    isSelectMode: Boolean = false,
    onStationSelected: ((StationNetworkModel) -> Unit)? = null,
    onBack: () -> Unit
) {
    val stations by viewModel.stations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorMessage.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val favoriteUuids = favorites.map { it.stationUuid }.toSet()

    Scaffold(
        containerColor = PitchBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isSelectMode) "Select Station" else "Explore Radio",
                        color = ElectricBlue
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ElectricBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PitchBlack)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Search bar
            TextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.searchStations(it)
                },
                placeholder = { Text("Search stations...", color = DarkGray) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = DarkGray.copy(alpha = 0.3f),
                    unfocusedContainerColor = DarkGray.copy(alpha = 0.2f),
                    cursorColor = ElectricBlue,
                    focusedIndicatorColor = ElectricBlue,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Playing indicator
            if (playbackState is PlaybackState.Playing || playbackState is PlaybackState.Buffering) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NeonRed.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (playbackState) {
                                is PlaybackState.Playing -> "▶ ${(playbackState as PlaybackState.Playing).stationName}"
                                is PlaybackState.Buffering -> "⏳ Buffering..."
                                else -> ""
                            },
                            color = NeonRed,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.stopPlayback() }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Stop", tint = NeonRed, modifier = Modifier.size(20.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Loading/Error
            if (isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ElectricBlue)
                }
            }

            error?.let {
                Text("Error: $it", color = NeonRed, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Station list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(stations, key = { it.stationUuid }) { station ->
                    StationCard(
                        station = station,
                        isFavorite = station.stationUuid in favoriteUuids,
                        isSelectMode = isSelectMode,
                        onPlay = { viewModel.playStation(station) },
                        onToggleFavorite = { viewModel.toggleFavorite(station) },
                        onSelect = { onStationSelected?.invoke(station) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StationCard(
    station: StationNetworkModel,
    isFavorite: Boolean,
    isSelectMode: Boolean,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkGray.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = station.name,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row {
                    station.codec?.let {
                        Text(text = it, color = DarkGray, fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    station.countryCode?.let {
                        Text(text = it, color = DarkGray, fontSize = 11.sp)
                    }
                }
            }

            if (isSelectMode) {
                IconButton(onClick = onSelect) {
                    Text("✓", color = ElectricBlue, fontSize = 18.sp)
                }
            } else {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) NeonRed else DarkGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            IconButton(onClick = onPlay) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = ElectricBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
