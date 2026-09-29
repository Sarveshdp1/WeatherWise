package com.weatherwise.fragments.home

import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import com.weatherwise.R
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.LocationServices
import com.weatherwise.data.AlertsUiState
import com.weatherwise.data.CurrentLocation
import com.weatherwise.databinding.FragmentHomeBinding
import com.weatherwise.fragments.common.colorRes
import com.weatherwise.fragments.common.emoji
import com.weatherwise.storage.SharedPreferencesManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment() {

    companion object {
        const val REQUEST_KEY_MANUAL_LOCATION_SEARCH = "manualLocationSearch"
        const val KEY_LOCATION_TEXT = "locationText"
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val homeViewModel: HomeViewModel by viewModel()
    private val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireContext())
    }
    private val geocoder by lazy { Geocoder(requireContext()) }

    private val weatherDataAdapter = WeatherDataAdapter(
        onLocationClicked = { showLocationOptions() }
    )

    private val sharedPreferencesManager: SharedPreferencesManager by inject()

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private var isInitialLocationSet: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setWeatherDataAdapter()
        setObservers()
        setListeners()
        if (!isInitialLocationSet) {
            setCurrentLocation(currentLocation = sharedPreferencesManager.getCurrentLocation())
            isInitialLocationSet = true
        }
    }

    private fun setListeners() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            setCurrentLocation(sharedPreferencesManager.getCurrentLocation())
        }
        // Open the Safety Center (SOS, contacts, guides, helplines)
        binding.buttonSafety.setOnClickListener {
            findNavController().navigate(R.id.action_home_fragment_to_safety_fragment)
        }
        // Tapping the alert bar opens the full list of alerts
        binding.textAlertBanner.setOnClickListener {
            findNavController().navigate(R.id.action_home_fragment_to_alerts_fragment)
        }
    }

    private fun setObservers() {
        with(homeViewModel) {
            currentLocation.observe(viewLifecycleOwner) {
                val currentLocationDataState = it.getContentIfNotHandled() ?: return@observe
                if (currentLocationDataState.isLoading) {
                    showLoading()
                }
                currentLocationDataState.currentLocation?.let { currentLocation ->
                    hideLoading()
                    sharedPreferencesManager.saveCurrentLocation(currentLocation)
                    setCurrentLocation(currentLocation)
                }
                currentLocationDataState.error?.let { error ->
                    hideLoading()
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                }
            }
            alertsState.observe(viewLifecycleOwner) { state ->
                showAlertsBar(state)
            }
            weatherData.observe(viewLifecycleOwner) {
                val weatherDataState = it.getContentIfNotHandled() ?: return@observe
                binding.swipeRefreshLayout.isRefreshing = weatherDataState.isLoading
                weatherDataState.currentWeather?.let { currentWeather ->
                    weatherDataAdapter.setCurrentWeather(currentWeather)
                }
                weatherDataState.forecast?.let { forecasts ->
                    weatherDataAdapter.setForecastData(forecasts)
                }
                weatherDataState.error?.let { error ->
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setWeatherDataAdapter() {
        binding.weatherDataRecycleView.itemAnimator = null
        binding.weatherDataRecycleView.adapter = weatherDataAdapter
    }

    private fun setCurrentLocation(currentLocation: CurrentLocation? = null) {
        weatherDataAdapter.setCurrentLocation(currentLocation ?: CurrentLocation())
        if (currentLocation == null) {
            setAlertsBarText("Choose a location to see safety alerts", R.color.sonic_silver)
        }
        currentLocation?.let { getWeatherData(currentLocation = it) }
    }

    private fun getCurrentLocation() {
        homeViewModel.getCurrentLocation(fusedLocationProviderClient, geocoder)
    }

    private fun isLocationPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun proceedWithCurrentLocation() {
        if (isLocationPermissionGranted()) {
            getCurrentLocation()
        } else {
            requestLocationPermission()
        }
    }

    private fun showLocationOptions() {
        val options = arrayOf("Current Location", "Search Manually")
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Choose Location Method")
            setItems(options) { _, which ->
                when(which) {
                    0 -> proceedWithCurrentLocation()
                    1 -> startManualLocationSearch()
                }
            }
            show()
        }
    }

    private fun showLoading() {
        with(binding) {
            weatherDataRecycleView.visibility = View.GONE
            swipeRefreshLayout.isEnabled = false
            swipeRefreshLayout.isRefreshing = true
        }
    }

    private fun hideLoading() {
        with(binding) {
            weatherDataRecycleView.visibility = View.VISIBLE
            swipeRefreshLayout.isEnabled = true
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun startManualLocationSearch() {
        startListeningManualLocationSelection()
        findNavController().navigate(R.id.action_home_fragment_to_location_fragment)
    }

    private fun startListeningManualLocationSelection() {
        setFragmentResultListener(REQUEST_KEY_MANUAL_LOCATION_SEARCH) { _, bundle ->
            val currentLocation = CurrentLocation(
                location = bundle.getString(KEY_LOCATION_TEXT) ?: "N/A",
                latitude = bundle.getDouble(LATITUDE),
                longitude = bundle.getDouble(LONGITUDE)
            )
            sharedPreferencesManager.saveCurrentLocation(currentLocation)
            setCurrentLocation(currentLocation)
        }
    }

    private fun stopListeningManualLocationSelection() {
        clearFragmentResultListener(REQUEST_KEY_MANUAL_LOCATION_SEARCH)
    }

    // Updates the alert bar at the top: green = all clear, orange/red = something to know.
    private fun showAlertsBar(state: AlertsUiState) {
        val alerts = state.alerts ?: emptyList()
        if (state.isLoading) {
            setAlertsBarText("Checking safety alerts...", R.color.sonic_silver)
        } else if (state.error != null) {
            setAlertsBarText("Couldn't check safety alerts (are you online?)", R.color.sonic_silver)
        } else if (alerts.isEmpty()) {
            setAlertsBarText("\u2705 All clear - no active alerts for your area", R.color.alert_safe)
        } else {
            // The list is sorted, so the first alert is the most serious one.
            val top = alerts.first()
            val count = if (alerts.size == 1) "1 alert" else "${alerts.size} alerts"
            setAlertsBarText(
                "${top.severity.emoji()} $count: ${top.title}. Tap to view.",
                top.severity.colorRes()
            )
        }
    }

    private fun setAlertsBarText(text: String, colorRes: Int) {
        binding.textAlertBanner.text = text
        binding.textAlertBanner.setTextColor(ContextCompat.getColor(requireContext(), colorRes))
    }

    private fun getWeatherData(currentLocation: CurrentLocation) {
        if (currentLocation.latitude != null && currentLocation.longitude != null) {
            homeViewModel.getWeatherData(
                latitude = currentLocation.latitude,
                longitude = currentLocation.longitude
            )
            homeViewModel.getAlerts(
                latitude = currentLocation.latitude,
                longitude = currentLocation.longitude
            )
        }
    }
}