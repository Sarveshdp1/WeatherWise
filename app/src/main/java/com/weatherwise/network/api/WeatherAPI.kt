package com.weatherwise.network.api

import com.weatherwise.data.RemoteLocation
import com.weatherwise.data.RemoteWeatherData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherAPI {

    companion object {
        const val  BASE_URL = "https://api.weatherapi.com/v1/"
        const val API_KEY = "ccb1aa5ff14a4d49944171721262107"
    }

    @GET("search.json")
    suspend fun searchLocation(
        @Query("key") key: String = API_KEY,
        @Query("q") query: String
    ): Response<List<RemoteLocation>>

    @GET("forecast.json")
    suspend fun getWeatherData(
        @Query("key") key: String = API_KEY,
        @Query("q") query: String
    ) : Response<RemoteWeatherData>
}