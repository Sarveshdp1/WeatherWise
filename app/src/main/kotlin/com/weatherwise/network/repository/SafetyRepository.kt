package com.weatherwise.network.repository

import com.weatherwise.data.RemoteEarthquakeData
import com.weatherwise.data.RemoteWeatherData
import com.weatherwise.data.SafetyAlert
import com.weatherwise.network.api.EarthquakeAPI
import com.weatherwise.network.api.WeatherAPI
import com.weatherwise.safety.AlertGenerator
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.cancellation.CancellationException

/**
 * Collects all safety alerts for a location:
 *   1. weather warnings + forecast risks + air quality (from WeatherAPI)
 *   2. recent earthquakes nearby (from USGS)
 *
 * The result is remembered for 10 minutes, so opening the Alerts screen right after the
 * Home screen is instant and does not waste internet data.
 */
class SafetyRepository(
    private val weatherAPI: WeatherAPI,
    private val earthquakeAPI: EarthquakeAPI
) {

    private companion object {
        const val CACHE_MILLIS = 10 * 60 * 1000L   // 10 minutes
        const val EARTHQUAKE_DAYS = 7              // look at earthquakes of the last 7 days
    }

    private var cachedKey: String? = null
    private var cachedTime: Long = 0L
    private var cachedAlerts: List<SafetyAlert> = emptyList()

    /**
     * Returns the alerts (most serious first). An empty list means "all clear".
     * Returns null when we could not find out (for example no internet).
     */
    suspend fun getAlerts(
        latitude: Double,
        longitude: Double,
        forceRefresh: Boolean = false
    ): List<SafetyAlert>? {
        val key = "$latitude,$longitude"
        val now = System.currentTimeMillis()

        if (!forceRefresh && key == cachedKey && now - cachedTime < CACHE_MILLIS) {
            return cachedAlerts
        }

        val weather = loadWeather(key)
        val earthquakes = loadEarthquakes(latitude, longitude, now)

        val alerts = mutableListOf<SafetyAlert>()
        if (weather != null) {
            alerts += try {
                AlertGenerator.fromWeather(weather)
            } catch (e: Exception) {
                emptyList()
            }
        }
        if (earthquakes != null) {
            alerts += try {
                AlertGenerator.fromEarthquakes(earthquakes, latitude, longitude, now)
            } catch (e: Exception) {
                emptyList()
            }
        }
        val sorted = AlertGenerator.sortBySeverity(alerts)

        val bothWorked = weather != null && earthquakes != null
        if (!bothWorked && sorted.isEmpty()) {
            // We could not check everything and found nothing, so we can't say "all clear".
            return null
        }
        if (bothWorked) {
            cachedKey = key
            cachedTime = now
            cachedAlerts = sorted
        }
        return sorted
    }

    private suspend fun loadWeather(query: String): RemoteWeatherData? {
        return callApi { weatherAPI.getWeatherData(query = query) }
    }

    private suspend fun loadEarthquakes(
        latitude: Double,
        longitude: Double,
        now: Long
    ): RemoteEarthquakeData? {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        val startTime = formatter.format(Date(now - EARTHQUAKE_DAYS * 24L * 60 * 60 * 1000))
        return callApi {
            earthquakeAPI.getEarthquakes(
                latitude = latitude,
                longitude = longitude,
                startTime = startTime
            )
        }
    }

    // Runs a network call and returns null if anything goes wrong (no internet, server error...)
    private suspend fun <T> callApi(block: suspend () -> Response<T>): T? {
        return try {
            val response = block()
            if (response.isSuccessful) response.body() else null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }
}
