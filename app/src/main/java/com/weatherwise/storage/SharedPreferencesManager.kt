package com.weatherwise.storage

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.weatherwise.data.CurrentLocation
import com.weatherwise.data.EmergencyContact

class SharedPreferencesManager(context: Context, private val gson: Gson) {

    private companion object {
        const val PREF_NAME = "WeatherAppPref"
        const val KEY_CURRENT_LOCATION = "currentLocation"
        const val KEY_EMERGENCY_CONTACTS = "emergencyContacts"
    }

    private val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveCurrentLocation(currentLocation: CurrentLocation) {
        val currentLocationJson = gson.toJson(currentLocation)
        sharedPreferences.edit {
            putString(KEY_CURRENT_LOCATION, currentLocationJson)
        }
    }

    fun getCurrentLocation(): CurrentLocation? {
        return sharedPreferences.getString(
            KEY_CURRENT_LOCATION,
            null
        )?.let { currentLocationJson ->
            gson.fromJson(currentLocationJson, CurrentLocation::class.java)
        }
    }

    // ---------- Emergency contacts (the people who get our SOS message) ----------

    fun saveEmergencyContacts(contacts: List<EmergencyContact>) {
        sharedPreferences.edit {
            putString(KEY_EMERGENCY_CONTACTS, gson.toJson(contacts))
        }
    }

    fun getEmergencyContacts(): List<EmergencyContact> {
        val json = sharedPreferences.getString(KEY_EMERGENCY_CONTACTS, null) ?: return emptyList()
        return try {
            gson.fromJson(json, Array<EmergencyContact>::class.java)?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
