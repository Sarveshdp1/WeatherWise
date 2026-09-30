package com.weatherwise.data

// How serious an alert is. A higher level number means more serious.
enum class AlertSeverity(val level: Int) {
    INFO(1),
    WARNING(2),
    DANGER(3)
}

// What kind of hazard the alert is about (used to find the matching safety guide).
enum class AlertType {
    OFFICIAL,
    RAIN_FLOOD,
    HEAT,
    COLD,
    WIND,
    THUNDERSTORM,
    AIR_QUALITY,
    EARTHQUAKE
}

// One alert shown to the user.
data class SafetyAlert(
    val type: AlertType,
    val severity: AlertSeverity,
    val title: String,      // short heading, e.g. "Heavy rain expected"
    val message: String,    // what is happening
    val advice: String,     // what the user should do
    val source: String,     // where the information came from
    val whenText: String    // "Today", "Tomorrow", "2 hours ago" ...
)

// What the screens observe to know if alerts are loading, loaded or failed.
data class AlertsUiState(
    val isLoading: Boolean = false,
    val alerts: List<SafetyAlert>? = null,
    val error: String? = null
)
