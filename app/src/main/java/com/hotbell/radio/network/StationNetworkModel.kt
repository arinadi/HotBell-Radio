package com.hotbell.radio.network

import com.google.gson.annotations.SerializedName

data class StationNetworkModel(
    @SerializedName("stationuuid") val stationUuid: String,
    @SerializedName("name") val name: String,
    @SerializedName("url") val url: String,
    @SerializedName("url_resolved") val urlResolved: String,
    @SerializedName("homepage") val homepage: String?,
    @SerializedName("favicon") val favicon: String?,
    @SerializedName("tags") val tags: String?,
    @SerializedName("countrycode") val countryCode: String?,
    @SerializedName("votes") val votes: Int,
    @SerializedName("codec") val codec: String?,
    @SerializedName("bitrate") val bitrate: Int
)
