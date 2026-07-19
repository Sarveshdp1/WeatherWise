package com.weatherwise.data

sealed class WeatherData

data class CurrentLocation(
    val data: String,
    val location:String = "Choose your location"
) : WeatherData()