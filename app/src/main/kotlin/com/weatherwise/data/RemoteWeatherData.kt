package com.weatherwise.data

import com.google.gson.annotations.SerializedName

// These classes describe the JSON that WeatherAPI.com sends us.
// Fields marked with "?" can be missing in the JSON, so they are allowed to be null.

data class RemoteWeatherData(
    val current: CurrentWeatherRemote,
    val forecast: ForecastRemote,
    val alerts: AlertsRemote? = null
)

data class CurrentWeatherRemote(
    @SerializedName("temp_c") val  temperature: Float,
    val condition: WeatherConditionRemote,
    @SerializedName("wind_kph") val wind: Float,
    val humidity: Int,
    @SerializedName("air_quality") val airQuality: AirQualityRemote? = null
)

data class AirQualityRemote(
    // US EPA index: 1 Good, 2 Moderate, 3 Unhealthy for sensitive people,
    // 4 Unhealthy, 5 Very unhealthy, 6 Hazardous
    @SerializedName("us-epa-index") val usEpaIndex: Int? = null
)

data class ForecastRemote(
    @SerializedName("forecastday") val forecastDay: List<ForecastDayRemote>
)

data class ForecastDayRemote(
    val day: DayRemote,
    val hour: List<ForecastHourRemote>
)

data class DayRemote(
    @SerializedName("daily_chance_of_rain") val changeOfRain: Int,
    @SerializedName("maxtemp_c") val maxTemperature: Float = 0f,
    @SerializedName("mintemp_c") val minTemperature: Float? = null,   // null = unknown, so no cold alert
    @SerializedName("maxwind_kph") val maxWind: Float = 0f,
    @SerializedName("totalprecip_mm") val totalRainMm: Float = 0f,
    val condition: WeatherConditionRemote? = null
)

data class ForecastHourRemote(
    val time: String,
    @SerializedName("temp_c") val temperature: Float,
    @SerializedName("feelslike_c") val feelsLikeTemperature: Float,
    val condition: WeatherConditionRemote
)

data class WeatherConditionRemote(
    val icon: String,
    val text: String? = null,
    val code: Int = 0
)

// Official weather warnings (only some countries/regions have them)
data class AlertsRemote(
    val alert: List<AlertRemote>? = null
)

data class AlertRemote(
    val headline: String? = null,
    val severity: String? = null,
    val event: String? = null,
    val desc: String? = null,
    val instruction: String? = null,
    val areas: String? = null
)
