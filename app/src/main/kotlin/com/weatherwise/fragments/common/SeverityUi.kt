package com.weatherwise.fragments.common

import androidx.annotation.ColorRes
import com.weatherwise.R
import com.weatherwise.data.AlertSeverity

// Which color and emoji to show for each alert severity.
// These are "extension functions": you can call them like  alert.severity.colorRes()

@ColorRes
fun AlertSeverity.colorRes(): Int {
    return when (this) {
        AlertSeverity.DANGER -> R.color.alert_danger
        AlertSeverity.WARNING -> R.color.alert_warning
        AlertSeverity.INFO -> R.color.alert_info
    }
}

fun AlertSeverity.emoji(): String {
    return when (this) {
        AlertSeverity.DANGER -> "\uD83D\uDEA8"      // siren
        AlertSeverity.WARNING -> "\u26A0\uFE0F"     // warning sign
        AlertSeverity.INFO -> "\u2139\uFE0F"        // info
    }
}
