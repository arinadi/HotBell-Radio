package com.hotbell.radio.ui.radio

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hotbell.radio.data.AppDatabase
import com.hotbell.radio.data.FavoriteStationEntity
import com.hotbell.radio.network.RadioRepository
import com.hotbell.radio.network.StationNetworkModel
import com.hotbell.radio.player.RadioPlayerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RadioViewModel(application: Application) : AndroidViewModel(application) {

    private val radioRepository = RadioRepository()
    private val db = AppDatabase.getInstance(application)
    private val favoriteDao = db.favoriteStationDao()

    private val _stations = MutableStateFlow<List<StationNetworkModel>>(emptyList())
    val stations: StateFlow<List<StationNetworkModel>> = _stations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val favorites = favoriteDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playbackState = RadioPlayerManager.playbackState

    // Filter states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCountry = MutableStateFlow<String?>(null)
    val selectedCountry: StateFlow<String?> = _selectedCountry.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    init {
        loadTopStations()
    }

    fun loadTopStations() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _stations.value = radioRepository.getTopStations(10)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCountryFilter(country: String?) {
        _selectedCountry.value = country
        applyFilters()
    }

    fun setTagFilter(tag: String?) {
        _selectedTag.value = tag
        applyFilters()
    }

    fun searchStations(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    private fun applyFilters() {
        val query = _searchQuery.value
        val country = _selectedCountry.value
        val tag = _selectedTag.value

        if (query.isBlank() && country == null && tag == null) {
            loadTopStations()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _stations.value = radioRepository.searchStations(
                    name = query.ifBlank { null },
                    tag = tag,
                    countryCode = country
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun playStation(station: StationNetworkModel) {
        RadioPlayerManager.play(getApplication(), station.urlResolved, station.name)
        viewModelScope.launch {
            try { radioRepository.registerClick(station.stationUuid) } catch (_: Exception) {}
        }
    }

    fun stopPlayback() {
        RadioPlayerManager.stop(getApplication())
    }

    fun toggleFavorite(station: StationNetworkModel) {
        viewModelScope.launch {
            if (favoriteDao.isFavorite(station.stationUuid)) {
                favoriteDao.deleteByUuid(station.stationUuid)
            } else {
                favoriteDao.insert(
                    FavoriteStationEntity(
                        stationUuid = station.stationUuid,
                        name = station.name,
                        urlResolved = station.urlResolved,
                        favicon = station.favicon ?: "",
                        codec = station.codec ?: ""
                    )
                )
            }
        }
    }
}
