package com.hotbell.radio.network

import com.google.gson.annotations.SerializedName

data class CountryModel(
    @SerializedName("name") val name: String,
    @SerializedName("iso_3166_1") val iso31661: String,
    @SerializedName("stationcount") val stationCount: Int
)
