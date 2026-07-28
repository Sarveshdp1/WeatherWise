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

data class CurrentWeather(
    val icon: String,
    val temperature: Float,
    val wind: Float,
    val humidity: Int,
    val chanceOfRain: Int
) : WeatherData()

data class Forecast(
    val time: String,
    val temperature: Float,
    val feelsLikeTemperature: Float,
    val icon: String
) : WeatherData()

private fun getCurrentData() : String {
    val currentDate = Date()
    val formatter = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
    return "Today ${formatter.format(currentDate)}"
}