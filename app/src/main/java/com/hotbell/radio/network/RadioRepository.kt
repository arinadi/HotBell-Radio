package com.hotbell.radio.network

class RadioRepository(
    private val api: RadioApiService = RetrofitClient.radioApiService
) {

    suspend fun searchStations(
        name: String? = null,
        tag: String? = null,
        countryCode: String? = null,
        limit: Int = 50
    ): List<StationNetworkModel> {
        return api.searchStations(
            name = name?.trim()?.take(100),
            tag = tag?.trim()?.take(50),
            countryCode = countryCode?.trim()?.take(2),
            limit = limit
        )
    }

    suspend fun getTopStations(limit: Int = 20): List<StationNetworkModel> {
        return api.getTopStations(limit)
    }

    suspend fun getStationByUuid(uuid: String): StationNetworkModel? {
        return api.getStationByUuid(uuid).firstOrNull()
    }

    suspend fun registerClick(stationUuid: String): ClickResponse {
        return api.registerClick(stationUuid)
    }
}
