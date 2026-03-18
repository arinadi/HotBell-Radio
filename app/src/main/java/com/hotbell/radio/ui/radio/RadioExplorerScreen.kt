package com.hotbell.radio.ui.radio

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hotbell.radio.network.CountryModel
import com.hotbell.radio.network.StationNetworkModel
import com.hotbell.radio.player.PlaybackState
import com.hotbell.radio.ui.theme.DarkGray
import com.hotbell.radio.ui.theme.ElectricBlue
import com.hotbell.radio.ui.theme.NeonRed
import com.hotbell.radio.ui.theme.HotBellOrange
import com.hotbell.radio.ui.theme.PitchBlack

private val COUNTRY_FILTERS = listOf(
    null to "All",
    "ID" to "🇮🇩 Indonesia",
    "US" to "🇺🇸 USA",
    "GB" to "🇬🇧 UK",
    "JP" to "🇯🇵 Japan",
    "KR" to "🇰🇷 Korea",
    "DE" to "🇩🇪 Germany",
    "FR" to "🇫🇷 France",
    "BR" to "🇧🇷 Brazil",
    "AU" to "🇦🇺 Australia"
)

private val TAG_FILTERS = listOf(
    null to "All",
    "pop" to "Pop",
    "rock" to "Rock",
    "jazz" to "Jazz",
    "classical" to "Classical",
    "news" to "News",
    "hiphop" to "Hip Hop",
    "electronic" to "Electronic",
    "reggae" to "Reggae",
    "country" to "Country",
    "metal" to "Metal",
    "r&b" to "R&B",
    "lounge" to "Lounge",
    "ambient" to "Ambient"
)

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
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()
    val searchedCountries by viewModel.searchedCountries.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var countrySearchQuery by remember { mutableStateOf("") }

    val favoriteUuids = favorites.map { it.stationUuid }.toSet()

    var countryMenuExpanded by remember { mutableStateOf(false) }
    var tagMenuExpanded by remember { mutableStateOf(false) }

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
            
            // Custom Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelectMode) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (isSelectMode) "Select Station" else "Explore Radio",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.searchStations(it)
                },
                placeholder = { Text("Search stations...", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = DarkGray.copy(alpha = 0.5f),
                    unfocusedContainerColor = DarkGray.copy(alpha = 0.5f),
                    cursorColor = HotBellOrange,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Dropdown Filters
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Country Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    androidx.compose.material3.OutlinedButton(
                        onClick = { countryMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            containerColor = DarkGray.copy(alpha = 0.3f),
                            contentColor = Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedCountry != null) HotBellOrange else DarkGray),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = COUNTRY_FILTERS.firstOrNull { it.first == selectedCountry }?.second ?: "All Countries",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    androidx.compose.material3.DropdownMenu(
                        expanded = countryMenuExpanded,
                        onDismissRequest = {
                            countryMenuExpanded = false
                            countrySearchQuery = ""
                            viewModel.searchCountries("")
                        },
                        modifier = Modifier
                            .background(DarkGray)
                            .heightIn(max = 350.dp)
                    ) {
                        // Search TextField inside dropdown
                        Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                            OutlinedTextField(
                                value = countrySearchQuery,
                                onValueChange = {
                                    countrySearchQuery = it
                                    viewModel.searchCountries(it)
                                },
                                placeholder = { Text("Search country...", color = Color.Gray, fontSize = 13.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = HotBellOrange,
                                    focusedBorderColor = HotBellOrange,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                            )
                        }

                        if (countrySearchQuery.isBlank()) {
                            // Show hardcoded defaults
                            COUNTRY_FILTERS.forEach { (code, label) ->
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text(label, color = Color.White) },
                                    onClick = {
                                        viewModel.setCountryFilter(code)
                                        countryMenuExpanded = false
                                        countrySearchQuery = ""
                                        viewModel.searchCountries("")
                                    }
                                )
                            }
                        } else {
                            // "All" option always available
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text("All Countries", color = Color.White) },
                                onClick = {
                                    viewModel.setCountryFilter(null)
                                    countryMenuExpanded = false
                                    countrySearchQuery = ""
                                    viewModel.searchCountries("")
                                }
                            )
                            // Show API search results
                            searchedCountries.forEach { country ->
                                androidx.compose.material3.DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${country.iso31661} ${country.name}",
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = "${country.stationCount}",
                                                color = Color.Gray,
                                                fontSize = 12.sp
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.setCountryFilter(country.iso31661)
                                        countryMenuExpanded = false
                                        countrySearchQuery = ""
                                        viewModel.searchCountries("")
                                    }
                                )
                            }
                            if (searchedCountries.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No countries found", color = Color.Gray, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                // Genre Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    androidx.compose.material3.OutlinedButton(
                        onClick = { tagMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            containerColor = DarkGray.copy(alpha = 0.3f),
                            contentColor = Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedTag != null) HotBellOrange else DarkGray),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = TAG_FILTERS.firstOrNull { it.first == selectedTag }?.second ?: "All Genres",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    androidx.compose.material3.DropdownMenu(
                        expanded = tagMenuExpanded,
                        onDismissRequest = { tagMenuExpanded = false },
                        modifier = Modifier.background(DarkGray)
                    ) {
                        TAG_FILTERS.forEach { (tag, label) ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(label, color = Color.White) },
                                onClick = {
                                    viewModel.setTagFilter(tag)
                                    tagMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Playing indicator removed since NowPlayingBar is global


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
                    val isStationPlaying = playbackState is PlaybackState.Playing && (playbackState as PlaybackState.Playing).stationName == station.name
                    StationCard(
                        station = station,
                        isFavorite = station.stationUuid in favoriteUuids,
                        isSelectMode = isSelectMode,
                        isPlaying = isStationPlaying,
                        onPlay = { viewModel.playStation(station) },
                        onStop = { viewModel.stopPlayback() },
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
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onStop: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = isSelectMode, onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) HotBellOrange.copy(alpha = 0.15f) else DarkGray.copy(alpha = 0.2f)
        ),
        border = if (isPlaying) androidx.compose.foundation.BorderStroke(1.dp, HotBellOrange.copy(alpha = 0.5f)) else null,
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
                onClick = if (isPlaying) onStop else onPlay,
                modifier = Modifier
                    .size(48.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(HotBellOrange.copy(alpha = if (isPlaying) 0.1f else 0.2f))
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
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
                    station.countryCode?.let {
                        Text(text = "🏁 $it", color = Color.Gray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    station.codec?.let {
                        Text(text = it, color = Color.Gray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    if (station.bitrate > 0) {
                        Text(text = "${station.bitrate}kbps", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }

            if (isSelectMode) {
                IconButton(onClick = onSelect) {
                    Text("✓", color = HotBellOrange, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) HotBellOrange else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
