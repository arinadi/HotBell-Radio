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

    init {
        loadTopStations()
    }

    fun loadTopStations() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _stations.value = radioRepository.getTopStations(50)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchStations(query: String) {
        if (query.isBlank()) {
            loadTopStations()
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _stations.value = radioRepository.searchStations(name = query)
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
