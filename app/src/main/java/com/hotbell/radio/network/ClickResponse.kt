package com.hotbell.radio.network

import com.google.gson.annotations.SerializedName

data class ClickResponse(
    @SerializedName("ok") val ok: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("name") val name: String?,
    @SerializedName("url") val url: String?
)
