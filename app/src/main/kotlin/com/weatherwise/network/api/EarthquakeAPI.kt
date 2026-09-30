package com.weatherwise.network.api

import com.weatherwise.data.RemoteEarthquakeData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

// Free earthquake data from USGS (no API key needed).
// The address is different from WeatherAPI, so we pass the full address using @Url.
interface EarthquakeAPI {

    companion object {
        const val QUERY_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query"
    }

    @GET
    suspend fun getEarthquakes(
        @Url url: String = QUERY_URL,
        @Query("format") format: String = "geojson",
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("maxradiuskm") maxRadiusKm: Int = 500,
        @Query("minmagnitude") minMagnitude: Double = 2.5,
        @Query("starttime") startTime: String,
        @Query("orderby") orderBy: String = "time",
        @Query("limit") limit: Int = 20
    ): Response<RemoteEarthquakeData>
}
