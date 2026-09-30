package com.weatherwise.network.repository

import android.annotation.SuppressLint
import android.location.Geocoder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.weatherwise.data.CurrentLocation
import com.weatherwise.data.RemoteLocation
import com.weatherwise.data.RemoteWeatherData
import com.weatherwise.network.api.WeatherAPI
import kotlin.coroutines.cancellation.CancellationException

class WeatherDataRepository(private val weatherAPI: WeatherAPI) {

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(
        fusedLocationProviderClient: FusedLocationProviderClient,
        onSuccess: (currentLocation: CurrentLocation) -> Unit,
        onFailure: () -> Unit
    ) {
        fusedLocationProviderClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location ->
            // The phone can return "no location" (for example GPS is off).
            // Before, this crashed the app. Now we report a failure instead.
            if (location == null) {
                onFailure()
            } else {
                onSuccess(
                    CurrentLocation(
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                )
            }
        }.addOnFailureListener { onFailure() }
    }

    @Suppress("DEPRECATION")
    fun updateAddressText(
        currentLocation: CurrentLocation,
        geocoder: Geocoder
    ): CurrentLocation {
        val latitude = currentLocation.latitude ?: return currentLocation
        val longitude = currentLocation.longitude ?: return currentLocation
        return geocoder.getFromLocation(latitude, longitude, 1)?.let { addresses ->
            val address = addresses[0]
            val addressText = StringBuilder()
            addressText.append(address.locality).append(", ")
            addressText.append(address.adminArea).append(", ")
            addressText.append(address.countryName)
            currentLocation.copy(
                location = addressText.toString()
            )
        } ?: currentLocation
    }

    // If there is no internet, Retrofit throws an exception. We catch it and return null,
    // so the screen can show a friendly error message instead of crashing.
    suspend fun searchLocation(query: String): List<RemoteLocation>? {
        return try {
            val response = weatherAPI.searchLocation(query = query)
            if (response.isSuccessful) response.body() else null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getWeatherData(latitude: Double, longitude: Double) : RemoteWeatherData? {
        return try {
            val response = weatherAPI.getWeatherData(query = "$latitude,$longitude")
            if (response.isSuccessful) response.body() else null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }
}
