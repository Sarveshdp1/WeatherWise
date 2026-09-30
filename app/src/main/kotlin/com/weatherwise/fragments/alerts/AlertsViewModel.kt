package com.weatherwise.fragments.alerts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weatherwise.data.AlertsUiState
import com.weatherwise.network.repository.SafetyRepository
import kotlinx.coroutines.launch

class AlertsViewModel(private val safetyRepository: SafetyRepository) : ViewModel() {

    private val _state = MutableLiveData<AlertsUiState>()
    val state: LiveData<AlertsUiState> get() = _state

    fun loadAlerts(latitude: Double, longitude: Double, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _state.value = AlertsUiState(isLoading = true)
            val alerts = safetyRepository.getAlerts(latitude, longitude, forceRefresh)
            _state.value = if (alerts == null) {
                AlertsUiState(error = "Couldn't load alerts. Please check your internet connection.")
            } else {
                AlertsUiState(alerts = alerts)
            }
        }
    }
}
