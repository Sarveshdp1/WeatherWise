package com.weatherwise.fragments.alerts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.weatherwise.data.SafetyAlert
import com.weatherwise.databinding.FragmentSimpleListBinding
import com.weatherwise.fragments.common.GuideDialog
import com.weatherwise.fragments.common.ListItem
import com.weatherwise.fragments.common.ListItemAdapter
import com.weatherwise.fragments.common.colorRes
import com.weatherwise.fragments.common.emoji
import com.weatherwise.safety.SafetyGuides
import com.weatherwise.storage.SharedPreferencesManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

// Screen that lists every safety alert for the saved location.
class AlertsFragment : Fragment() {

    private var _binding: FragmentSimpleListBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val alertsViewModel: AlertsViewModel by viewModel()
    private val sharedPreferencesManager: SharedPreferencesManager by inject()

    // The alerts currently shown (needed to find the alert that was tapped)
    private var alerts: List<SafetyAlert> = emptyList()

    private val listAdapter = ListItemAdapter(
        onItemClicked = { item -> showAlertDetails(item.id) }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSimpleListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            textTitle.text = "Live Safety Alerts"
            textSubtitle.text = "Weather warnings, forecast risks, air quality and earthquakes near you"
            textSubtitle.visibility = View.VISIBLE
            imageClose.setOnClickListener { findNavController().popBackStack() }
            recyclerView.adapter = listAdapter
        }
        setObserver()
        loadAlerts(forceRefresh = false)
    }

    private fun loadAlerts(forceRefresh: Boolean) {
        val location = sharedPreferencesManager.getCurrentLocation()
        val latitude = location?.latitude
        val longitude = location?.longitude
        if (latitude == null || longitude == null) {
            showMessage("Please choose a location on the home screen first.")
            return
        }
        alertsViewModel.loadAlerts(latitude, longitude, forceRefresh)
    }

    private fun setObserver() {
        alertsViewModel.state.observe(viewLifecycleOwner) { state ->
            // Start by hiding everything, then show only what is needed.
            with(binding) {
                progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                recyclerView.visibility = View.GONE
                textEmpty.visibility = View.GONE
                buttonAction.visibility = View.GONE
            }

            val error = state.error
            val loadedAlerts = state.alerts

            if (state.isLoading) {
                // Nothing else to show while loading.
            } else if (error != null) {
                showMessage(error)
                binding.buttonAction.text = "Try again"
                binding.buttonAction.setOnClickListener { loadAlerts(forceRefresh = true) }
                binding.buttonAction.visibility = View.VISIBLE
            } else if (loadedAlerts != null) {
                alerts = loadedAlerts
                if (loadedAlerts.isEmpty()) {
                    showMessage(
                        "\u2705 No active alerts for your area right now.\n\n" +
                                "We checked weather warnings, forecast risks, air quality " +
                                "and earthquakes nearby."
                    )
                } else {
                    listAdapter.setData(
                        loadedAlerts.mapIndexed { index, alert -> toListItem(index, alert) }
                    )
                    binding.recyclerView.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun toListItem(index: Int, alert: SafetyAlert): ListItem {
        val details = if (alert.whenText.isBlank()) alert.message else "${alert.whenText} - ${alert.message}"
        return ListItem(
            id = index,
            title = "${alert.severity.emoji()} ${alert.title}",
            subtitle = details,
            accentColorRes = alert.severity.colorRes()
        )
    }

    private fun showMessage(text: String) {
        binding.textEmpty.text = text
        binding.textEmpty.visibility = View.VISIBLE
    }

    private fun showAlertDetails(index: Int) {
        val alert = alerts.getOrNull(index) ?: return
        val guide = SafetyGuides.forAlertType(alert.type)

        val whenLine = if (alert.whenText.isBlank()) "" else "\nWhen: ${alert.whenText}"
        val builder = AlertDialog.Builder(requireContext())
            .setTitle("${alert.severity.emoji()} ${alert.title}")
            .setMessage("${alert.message}\n\nWhat to do:\n${alert.advice}\n\nSource: ${alert.source}$whenLine")
            .setPositiveButton("Close", null)

        // If we have a matching offline guide, offer a button to open it.
        if (guide != null) {
            builder.setNeutralButton("Safety guide") { _, _ ->
                GuideDialog.show(requireContext(), guide)
            }
        }
        builder.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
