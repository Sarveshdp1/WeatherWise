package com.weatherwise.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class WeatherData

data class CurrentLocation(
    val data: String = getCurrentData(),
    val location:String = "Choose your location",
    val latitude: Double? = null,
    val longitude: Double? = null
) : WeatherData()

private fun getCurrentData() : String {
    val currentDate = Date()
    val formatter = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
    return "Today ${formatter.format(currentDate)}"
}