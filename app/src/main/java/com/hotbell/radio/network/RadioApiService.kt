package com.hotbell.radio.network

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RadioApiService {

    @GET("json/stations/search")
    suspend fun searchStations(
        @Query("name") name: String? = null,
        @Query("tag") tag: String? = null,
        @Query("countrycode") countryCode: String? = null,
        @Query("limit") limit: Int = 50,
        @Query("order") order: String = "clickcount",
        @Query("reverse") reverse: Boolean = true
    ): List<StationNetworkModel>

    @GET("json/stations/topclick/{limit}")
    suspend fun getTopStations(
        @Path("limit") limit: Int = 20
    ): List<StationNetworkModel>

    @GET("json/stations/byuuid/{uuid}")
    suspend fun getStationByUuid(
        @Path("uuid") uuid: String
    ): List<StationNetworkModel>

    @POST("json/url/{stationuuid}")
    suspend fun registerClick(
        @Path("stationuuid") stationUuid: String
    ): ClickResponse

    @GET("json/countries")
    suspend fun getCountries(
        @Query("order") order: String = "stationcount",
        @Query("reverse") reverse: Boolean = true
    ): List<CountryModel>
}
