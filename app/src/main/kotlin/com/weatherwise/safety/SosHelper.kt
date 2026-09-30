package com.weatherwise.safety

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import com.weatherwise.data.EmergencyContact
import java.util.Locale

/**
 * Everything needed to send the SOS message.
 * Kept in one small file so the Safety screen stays easy to read.
 */
object SosHelper {

    // Builds the SMS text. Plain English letters only, so it stays a cheap 1-2 part SMS.
    // isLastKnown = true means we could not get a fresh GPS position, so we say so honestly.
    fun buildMessage(latitude: Double?, longitude: Double?, isLastKnown: Boolean = false): String {
        val locationText = if (latitude != null && longitude != null) {
            val label = if (isLastKnown) "My last known location (may be outdated): " else "My location: "
            // Tapping this link opens Google Maps at the exact position.
            label + "https://maps.google.com/?q=" +
                    String.format(Locale.US, "%.6f,%.6f", latitude, longitude)
        } else {
            "My exact location is not available."
        }
        return "EMERGENCY! I need help. $locationText (Sent from WeatherWise SOS)"
    }

    /**
     * Sends the message directly by SMS to every contact (needs the SEND_SMS permission).
     * Returns how many contacts it was sent to successfully.
     */
    fun sendSms(context: Context, contacts: List<EmergencyContact>, message: String): Int {
        val smsManager = getSmsManager(context) ?: return 0
        var sentCount = 0
        for (contact in contacts) {
            try {
                // Long messages must be split into parts.
                val parts = smsManager.divideMessage(message)
                smsManager.sendMultipartTextMessage(contact.phone, null, parts, null, null)
                sentCount++
            } catch (e: Exception) {
                // One bad number should not stop the others.
            }
        }
        return sentCount
    }

    // Backup plan if the user did not allow SMS permission: open the messaging app
    // with the message already written, so the user only has to press "Send".
    fun openSmsApp(context: Context, phone: String, message: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone")).apply {
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }

    // Opens the phone dialer with a number filled in (the user presses the call button).
    fun openDialer(context: Context, number: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }

    // Android 12 (API 31) changed how to get the SmsManager, so we handle both ways.
    @Suppress("DEPRECATION")
    private fun getSmsManager(context: Context): SmsManager? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            SmsManager.getDefault()
        }
    }
}
