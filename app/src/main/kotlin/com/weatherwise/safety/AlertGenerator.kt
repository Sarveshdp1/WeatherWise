package com.weatherwise.safety

import com.weatherwise.data.AlertSeverity
import com.weatherwise.data.AlertType
import com.weatherwise.data.RemoteEarthquakeData
import com.weatherwise.data.RemoteWeatherData
import com.weatherwise.data.SafetyAlert
import java.util.Locale
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Turns raw weather / earthquake data into simple alerts for the user.
 *
 * This file does NOT use any Android classes, so it is easy to read and easy to unit test.
 * All the "rules" (for example "40 degrees or more = heatwave") are written in one place below,
 * so you can change a number here and the whole app follows.
 */
object AlertGenerator {

    // ---------- Rules (thresholds) ----------
    private const val RAIN_INFO_MM = 25f          // moderate to heavy rain
    private const val RAIN_WARNING_MM = 64.5f     // "heavy rain" as defined by IMD
    private const val RAIN_DANGER_MM = 115f       // "very heavy rain" as defined by IMD
    private const val RAIN_MIN_CHANCE = 50        // % chance of rain needed for the rain rules

    private const val HEAT_WARNING_C = 40f
    private const val HEAT_DANGER_C = 45f

    private const val COLD_WARNING_C = 4f
    private const val COLD_DANGER_C = 0f

    private const val WIND_WARNING_KPH = 50f
    private const val WIND_DANGER_KPH = 75f

    private const val QUAKE_WARNING_MAG = 4.5
    private const val QUAKE_DANGER_MAG = 6.0
    private const val MAX_QUAKES_SHOWN = 5

    // WeatherAPI condition codes that mean "thunder"
    private val THUNDER_CODES = setOf(1087, 1273, 1276, 1279, 1282)

    // ---------- 1. Alerts from the weather forecast ----------
    fun fromWeather(weather: RemoteWeatherData): List<SafetyAlert> {
        val official = officialAlerts(weather)
        val forecastRisks = forecastRiskAlerts(weather)
        val air = airQualityAlert(weather)
        return official + forecastRisks + listOfNotNull(air)
    }

    private fun officialAlerts(weather: RemoteWeatherData): List<SafetyAlert> {
        val list = weather.alerts?.alert.orEmpty()
        return list.map { alert ->
            val title = alert.event?.takeIf { it.isNotBlank() }
                ?: alert.headline?.takeIf { it.isNotBlank() }
                ?: "Weather warning"
            SafetyAlert(
                type = AlertType.OFFICIAL,
                severity = officialSeverity(alert.severity),
                title = title,
                message = alert.desc?.trim().orEmpty().ifBlank { alert.headline.orEmpty() },
                advice = alert.instruction?.trim().orEmpty()
                    .ifBlank { "Follow instructions from your local authorities." },
                source = "Official weather warning",
                whenText = alert.areas.orEmpty()
            )
        }
    }

    private fun officialSeverity(text: String?): AlertSeverity {
        return when (text?.trim()?.lowercase(Locale.ROOT)) {
            "extreme", "severe" -> AlertSeverity.DANGER
            "moderate" -> AlertSeverity.WARNING
            else -> AlertSeverity.INFO
        }
    }

    private fun forecastRiskAlerts(weather: RemoteWeatherData): List<SafetyAlert> {
        val all = mutableListOf<SafetyAlert>()

        weather.forecast.forecastDay.forEachIndexed { index, forecastDay ->
            val day = forecastDay.day
            val label = dayLabel(index)

            // Rain / flood
            val rain = day.totalRainMm
            if (day.changeOfRain >= RAIN_MIN_CHANCE) {
                if (rain >= RAIN_DANGER_MM) {
                    all += rainAlert(AlertSeverity.DANGER, "Very heavy rain - flood risk", rain, label)
                } else if (rain >= RAIN_WARNING_MM) {
                    all += rainAlert(AlertSeverity.WARNING, "Heavy rain expected", rain, label)
                } else if (rain >= RAIN_INFO_MM) {
                    all += rainAlert(AlertSeverity.INFO, "Moderate to heavy rain likely", rain, label)
                }
            }

            // Heat
            val hottest = day.maxTemperature
            if (hottest >= HEAT_DANGER_C) {
                all += heatAlert(AlertSeverity.DANGER, "Extreme heat", hottest, label)
            } else if (hottest >= HEAT_WARNING_C) {
                all += heatAlert(AlertSeverity.WARNING, "Heatwave conditions", hottest, label)
            }

            // Cold
            val coldest = day.minTemperature   // can be null if the server did not send it
            if (coldest != null) {
                if (coldest <= COLD_DANGER_C) {
                    all += coldAlert(AlertSeverity.DANGER, "Freezing temperatures", coldest, label)
                } else if (coldest <= COLD_WARNING_C) {
                    all += coldAlert(AlertSeverity.WARNING, "Cold wave conditions", coldest, label)
                }
            }

            // Wind
            val wind = day.maxWind
            if (wind >= WIND_DANGER_KPH) {
                all += windAlert(AlertSeverity.DANGER, "Damaging winds", wind, label)
            } else if (wind >= WIND_WARNING_KPH) {
                all += windAlert(AlertSeverity.WARNING, "Strong winds", wind, label)
            }

            // Thunderstorm / lightning
            val code = day.condition?.code ?: 0
            if (code in THUNDER_CODES) {
                all += SafetyAlert(
                    type = AlertType.THUNDERSTORM,
                    severity = AlertSeverity.WARNING,
                    title = "Thunderstorm and lightning likely",
                    message = "Thunderstorms are forecast in your area.",
                    advice = "Stay indoors during the storm. Avoid open fields, tall trees and water. " +
                            "Unplug sensitive electronics.",
                    source = "Weather forecast",
                    whenText = label
                )
            }
        }

        // If the same hazard shows up on several days, only keep the most serious one
        // (and the earliest day if they are equally serious).
        return all.groupBy { it.type }.mapNotNull { (_, sameType) ->
            sameType.maxByOrNull { it.severity.level }
        }
    }

    private fun rainAlert(severity: AlertSeverity, title: String, rainMm: Float, label: String) =
        SafetyAlert(
            type = AlertType.RAIN_FLOOD,
            severity = severity,
            title = title,
            message = "About ${rainMm.roundToInt()} mm of rain is forecast. Low-lying areas may flood.",
            advice = "Avoid low-lying areas and flooded roads. Keep phones charged and " +
                    "keep emergency supplies ready. Never walk or drive through floodwater.",
            source = "Weather forecast",
            whenText = label
        )

    private fun heatAlert(severity: AlertSeverity, title: String, tempC: Float, label: String) =
        SafetyAlert(
            type = AlertType.HEAT,
            severity = severity,
            title = title,
            message = "Temperature may reach ${tempC.roundToInt()}\u00B0C.",
            advice = "Drink plenty of water, avoid going out between 12 PM and 4 PM, wear light " +
                    "cotton clothes and never leave children or pets in a parked vehicle.",
            source = "Weather forecast",
            whenText = label
        )

    private fun coldAlert(severity: AlertSeverity, title: String, tempC: Float, label: String) =
        SafetyAlert(
            type = AlertType.COLD,
            severity = severity,
            title = title,
            message = "Temperature may drop to ${tempC.roundToInt()}\u00B0C.",
            advice = "Wear warm layers, keep your head and hands covered, and check on elderly " +
                    "neighbours. Never use charcoal heaters in a closed room.",
            source = "Weather forecast",
            whenText = label
        )

    private fun windAlert(severity: AlertSeverity, title: String, windKph: Float, label: String) =
        SafetyAlert(
            type = AlertType.WIND,
            severity = severity,
            title = title,
            message = "Winds may reach ${windKph.roundToInt()} km/h.",
            advice = "Stay indoors and away from windows. Secure or bring in loose objects and " +
                    "avoid travelling if possible.",
            source = "Weather forecast",
            whenText = label
        )

    private fun airQualityAlert(weather: RemoteWeatherData): SafetyAlert? {
        val index = weather.current.airQuality?.usEpaIndex ?: return null
        val severity = when {
            index >= 5 -> AlertSeverity.DANGER
            index == 4 -> AlertSeverity.WARNING
            index == 3 -> AlertSeverity.INFO
            else -> return null
        }
        val title = when (severity) {
            AlertSeverity.DANGER -> "Very unhealthy air quality"
            AlertSeverity.WARNING -> "Unhealthy air quality"
            AlertSeverity.INFO -> "Air quality is poor for sensitive people"
        }
        return SafetyAlert(
            type = AlertType.AIR_QUALITY,
            severity = severity,
            title = title,
            message = "The air right now may harm children, older people and people with " +
                    "asthma or heart problems.",
            advice = "Limit outdoor activity, keep windows closed and wear an N95 mask if you " +
                    "must go out.",
            source = "Air quality data",
            whenText = "Now"
        )
    }

    // ---------- 2. Alerts from earthquakes ----------
    fun fromEarthquakes(
        data: RemoteEarthquakeData,
        userLatitude: Double,
        userLongitude: Double,
        nowMillis: Long
    ): List<SafetyAlert> {
        val alerts = data.features.orEmpty().mapNotNull { feature ->
            val properties = feature.properties ?: return@mapNotNull null
            val magnitude = properties.mag ?: return@mapNotNull null

            val severity = when {
                magnitude >= QUAKE_DANGER_MAG -> AlertSeverity.DANGER
                magnitude >= QUAKE_WARNING_MAG -> AlertSeverity.WARNING
                else -> AlertSeverity.INFO
            }

            // USGS gives coordinates as [longitude, latitude, depth]
            val coordinates = feature.geometry?.coordinates
            val distanceText = if (coordinates != null && coordinates.size >= 2) {
                val km = distanceKm(userLatitude, userLongitude, coordinates[1], coordinates[0])
                " - about ${km.roundToInt()} km from you"
            } else {
                ""
            }

            SafetyAlert(
                type = AlertType.EARTHQUAKE,
                severity = severity,
                title = "Magnitude ${String.format(Locale.US, "%.1f", magnitude)} earthquake",
                message = (properties.place ?: "Near your area") + distanceText,
                advice = "Small earthquakes are usually harmless. If you feel shaking: Drop, " +
                        "Cover and Hold On, and expect aftershocks.",
                source = "USGS Earthquake Hazards Program",
                whenText = relativeTime(properties.time, nowMillis)
            )
        }
        return alerts.take(MAX_QUAKES_SHOWN)
    }

    // ---------- 3. Put everything in order: most serious first ----------
    fun sortBySeverity(alerts: List<SafetyAlert>): List<SafetyAlert> {
        return alerts.sortedByDescending { it.severity.level }
    }

    // ---------- Small helpers ----------
    private fun dayLabel(index: Int): String {
        return when (index) {
            0 -> "Today"
            1 -> "Tomorrow"
            else -> "In $index days"
        }
    }

    private fun relativeTime(timeMillis: Long?, nowMillis: Long): String {
        if (timeMillis == null || timeMillis <= 0L) return ""
        val minutes = (nowMillis - timeMillis) / 60_000L
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes min ago"
            minutes < 60 * 24 -> "${minutes / 60} hours ago"
            else -> "${minutes / (60 * 24)} days ago"
        }
    }

    // Distance between two GPS points in kilometres (haversine formula)
    fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        return 2 * earthRadiusKm * asin(sqrt(a))
    }
}
