package com.hotbell.radio.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hotbell.radio.ui.home.HomeViewModel
import com.hotbell.radio.ui.theme.DarkGray
import com.hotbell.radio.ui.theme.HotBellOrange
import com.hotbell.radio.ui.theme.PitchBlack

@Composable
fun FavoritesScreen(viewModel: HomeViewModel = viewModel()) {
    val favorites by viewModel.favorites.collectAsState()

    Scaffold(
        containerColor = PitchBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header
            Text(
                text = "Favorite Stations",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Your saved live radio streams",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (favorites.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No favorite stations yet.", color = Color.Gray, fontSize = 16.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(favorites, key = { it.stationUuid }) { station ->
                        FavoriteStationCard(
                            station = station,
                            onPlay = { viewModel.playFavoriteStation(station) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteStationCard(
    station: com.hotbell.radio.data.FavoriteStationEntity,
    onPlay: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onPlay() },
        colors = CardDefaults.cardColors(containerColor = DarkGray.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play Button on far left inside a circular background
            IconButton(
                onClick = onPlay,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(HotBellOrange.copy(alpha = 0.2f))
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = HotBellOrange,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = station.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (station.codec.isNotEmpty()) {
                        Text(text = station.codec, color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }

            IconButton(onClick = { /* Unfavorite not handled here right now, or via long press */ }) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Favorite",
                    tint = HotBellOrange,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
