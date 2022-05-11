package com.example.tapandgo

import com.example.tapandgo.dataFromAPI.Contract
import com.example.tapandgo.dataFromAPI.Station
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {

    @GET( "vls/v3/stations")
    fun getStations(@Query("apiKey") apiKey: String): Call<List<Station>>   // for all stations of all contracts API request


    @GET("vls/v3/contracts")
    fun getContracts(@Query("apiKey") apiKey: String): Call<List<Contract>>   // for all contracts API request

}