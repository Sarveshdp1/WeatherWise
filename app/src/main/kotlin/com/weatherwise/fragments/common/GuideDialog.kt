package com.weatherwise.fragments.common

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.weatherwise.safety.SafetyGuide
import com.weatherwise.safety.SafetyGuides

// Shows one safety guide in a pop-up. Used by the Guides screen and by the Alert details.
object GuideDialog {
    fun show(context: Context, guide: SafetyGuide) {
        AlertDialog.Builder(context)
            .setTitle("${guide.emoji} ${guide.title}")
            .setMessage(SafetyGuides.toText(guide))
            .setPositiveButton("Close", null)
            .show()
    }
}
