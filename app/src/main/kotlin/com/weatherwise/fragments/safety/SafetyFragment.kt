package com.weatherwise.fragments.safety

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.LocationServices
import com.weatherwise.R
import com.weatherwise.databinding.FragmentSafetyBinding
import com.weatherwise.fragments.common.ListItem
import com.weatherwise.fragments.common.ListItemAdapter
import com.weatherwise.network.repository.WeatherDataRepository
import com.weatherwise.safety.SosHelper
import com.weatherwise.storage.SharedPreferencesManager
import org.koin.android.ext.android.inject

/**
 * Safety Center: the big SOS button and links to Alerts, Contacts, Guides and Helplines.
 *
 * How SOS works (step by step):
 *   1. User taps SOS -> we ask "Are you sure?" (so it is not sent by accident)
 *   2. We ask for Location + SMS permission (only the first time)
 *   3. We get the GPS location (if it takes more than 10 seconds, we use the last known one)
 *   4. We send an SMS with a Google Maps link to every emergency contact
 */
class SafetyFragment : Fragment() {

    private companion object {
        const val ITEM_ALERTS = 0
        const val ITEM_CONTACTS = 1
        const val ITEM_GUIDES = 2
        const val ITEM_HELPLINES = 3
        const val GPS_TIMEOUT_MILLIS = 10_000L
        const val EMERGENCY_NUMBER = "112"
    }

    private var _binding: FragmentSafetyBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val sharedPreferencesManager: SharedPreferencesManager by inject()
    private val weatherDataRepository: WeatherDataRepository by inject()

    private val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private val listAdapter = ListItemAdapter(
        onItemClicked = { item -> onFeatureClicked(item.id) }
    )

    // true while we are waiting for the GPS. It also makes sure we send the SOS only ONCE
    // (because both the GPS and the 10-second timer can try to send it).
    private var isSosPending = false
    private var appContext: Context? = null
    private val handler = Handler(Looper.getMainLooper())
    private val gpsTimeoutRunnable = Runnable { deliverSosWithSavedLocation() }

    // Called after the user answers the permission pop-up.
    // We continue no matter what the answer was: without a permission we use a backup plan.
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        sendSos()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSafetyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            imageClose.setOnClickListener { findNavController().popBackStack() }
            cardSos.setOnClickListener { onSosClicked() }
            recyclerView.adapter = listAdapter
        }
        listAdapter.setData(
            listOf(
                ListItem(ITEM_ALERTS, "\uD83D\uDEA8 Live Alerts", "Weather warnings, forecast risks, air quality, earthquakes", R.color.alert_warning),
                ListItem(ITEM_CONTACTS, "\uD83D\uDC65 Emergency Contacts", "People who receive your SOS message", R.color.alert_info),
                ListItem(ITEM_GUIDES, "\uD83D\uDCD6 Safety Guides", "What to do in a flood, earthquake, heatwave... (works offline)", R.color.alert_safe),
                ListItem(ITEM_HELPLINES, "\u260E\uFE0F Emergency Helplines", "One-tap calling for police, ambulance, disaster help", R.color.alert_danger)
            )
        )
    }

    // Runs every time the screen appears (also when coming back from the Contacts screen).
    override fun onResume() {
        super.onResume()
        updateContactsInfo()
    }

    private fun updateContactsInfo() {
        val count = sharedPreferencesManager.getEmergencyContacts().size
        binding.textContactsInfo.text = if (count == 0) {
            "No emergency contacts yet. Add at least one to use SOS."
        } else {
            "SOS will be sent to $count emergency contact(s)."
        }
    }

    private fun onFeatureClicked(id: Int) {
        val action = when (id) {
            ITEM_ALERTS -> R.id.action_safety_fragment_to_alerts_fragment
            ITEM_CONTACTS -> R.id.action_safety_fragment_to_contacts_fragment
            ITEM_GUIDES -> R.id.action_safety_fragment_to_guides_fragment
            else -> R.id.action_safety_fragment_to_helplines_fragment
        }
        findNavController().navigate(action)
    }

    // ---------- SOS: step 1 - confirm ----------
    private fun onSosClicked() {
        val contacts = sharedPreferencesManager.getEmergencyContacts()
        if (contacts.isEmpty()) {
            AlertDialog.Builder(requireContext())
                .setTitle("No emergency contacts")
                .setMessage("Add at least one emergency contact first, so we know who to alert.")
                .setPositiveButton("Add contact") { _, _ ->
                    findNavController().navigate(R.id.action_safety_fragment_to_contacts_fragment)
                }
                .setNegativeButton("Cancel", null)
                .show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Send SOS?")
            .setMessage(
                "Your location will be sent by SMS to ${contacts.size} emergency contact(s). " +
                        "Normal SMS charges may apply."
            )
            .setPositiveButton("Send SOS") { _, _ -> requestPermissionsThenSend() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ---------- SOS: step 2 - permissions ----------
    private fun requestPermissionsThenSend() {
        val missing = listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS)
            .filter { !isGranted(it) }
        if (missing.isEmpty()) {
            sendSos()
        } else {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }

    private fun isGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), permission) ==
                PackageManager.PERMISSION_GRANTED
    }

    // ---------- SOS: step 3 - get the location ----------
    private fun sendSos() {
        if (isSosPending) return
        isSosPending = true
        // We keep the application context: it stays valid even if the user leaves this screen.
        appContext = requireContext().applicationContext

        if (!isGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            deliverSosWithSavedLocation()
            return
        }

        Toast.makeText(requireContext(), "Getting your location...", Toast.LENGTH_SHORT).show()
        // If the GPS is too slow (for example indoors), don't keep the user waiting.
        handler.postDelayed(gpsTimeoutRunnable, GPS_TIMEOUT_MILLIS)
        weatherDataRepository.getCurrentLocation(
            fusedLocationProviderClient = fusedLocationProviderClient,
            onSuccess = { location -> deliverSos(location.latitude, location.longitude, false) },
            onFailure = { deliverSosWithSavedLocation() }
        )
    }

    private fun deliverSosWithSavedLocation() {
        val saved = sharedPreferencesManager.getCurrentLocation()
        deliverSos(saved?.latitude, saved?.longitude, true)
    }

    // ---------- SOS: step 4 - send the SMS ----------
    private fun deliverSos(latitude: Double?, longitude: Double?, isLastKnown: Boolean) {
        if (!isSosPending) return          // already sent (GPS and timer can both call this)
        isSosPending = false
        handler.removeCallbacks(gpsTimeoutRunnable)

        val ctx = appContext ?: return
        val contacts = sharedPreferencesManager.getEmergencyContacts()
        if (contacts.isEmpty()) return
        val message = SosHelper.buildMessage(latitude, longitude, isLastKnown)

        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.SEND_SMS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            val sentCount = SosHelper.sendSms(ctx, contacts, message)
            showResult(ctx, sentCount, contacts.size)
        } else {
            // Backup plan: open the messaging app with the message ready to send.
            Toast.makeText(ctx, "SMS permission denied. Opening your messages app...", Toast.LENGTH_LONG).show()
            val opened = SosHelper.openSmsApp(ctx, contacts.first().phone, message)
            if (!opened) {
                Toast.makeText(ctx, "Could not open a messaging app. Please call $EMERGENCY_NUMBER.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showResult(context: Context, sentCount: Int, total: Int) {
        val text = if (sentCount > 0) {
            "SOS sent to $sentCount of $total contact(s).\n\nIf you are in immediate danger, call $EMERGENCY_NUMBER."
        } else {
            "The SOS message could not be sent.\n\nPlease call $EMERGENCY_NUMBER now."
        }
        if (isAdded && _binding != null) {
            AlertDialog.Builder(requireContext())
                .setTitle(if (sentCount > 0) "SOS sent" else "SOS not sent")
                .setMessage(text)
                .setPositiveButton("Call $EMERGENCY_NUMBER") { _, _ ->
                    SosHelper.openDialer(context, EMERGENCY_NUMBER)
                }
                .setNegativeButton("Close", null)
                .show()
        } else {
            // The user already left this screen, so a simple message is enough.
            Toast.makeText(context, text, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // Note: we do NOT cancel a pending SOS here. If the user pressed SOS and then left
        // the screen, the message should still be sent when the location arrives (or after 10 s).
    }
}
