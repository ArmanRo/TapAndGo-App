package com.example.tapandgo.dataFromAPI

import com.google.gson.annotations.SerializedName

data class Contract(
    @SerializedName("cities")
    val cities: List<String>,
    @SerializedName("commercial_name")
    val commercial_name: String,
    @SerializedName("country_code")
    val country_code: String,
    @SerializedName("name")
    val name: String
)