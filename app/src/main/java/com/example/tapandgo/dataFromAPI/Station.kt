package com.example.tapandgo.dataFromAPI

import com.google.gson.annotations.SerializedName

data class Station(
    @SerializedName(value = "adresse")
    val address: String,
    @SerializedName(value = "banking")
    val banking: Boolean,
    @SerializedName(value = "bonus")
    val bonus: Boolean,
    @SerializedName(value = "connected")
    val connected: Boolean,
    @SerializedName(value = "contractName")
    val contractName: String,
    @SerializedName(value = "lastUpdate")
    val lastUpdate: String,
    @SerializedName(value = "mainStands")
    val mainStands: MainStands,
    @SerializedName(value = "name")
    val name: String,
    @SerializedName(value = "number")
    val number: Int,
    @SerializedName(value = "overflow")
    val overflow: Boolean,
    @SerializedName(value = "overflowStands")
    val overflowStands: Any,
    @SerializedName(value = "position")
    val position: Position,
    @SerializedName(value = "shape")
    val shape: Any,
    @SerializedName(value = "status")
    val status: String,
    @SerializedName(value = "totalStands")
    val totalStands: TotalStands
)