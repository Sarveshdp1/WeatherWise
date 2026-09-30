package com.weatherwise.fragments.helplines

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.weatherwise.R
import com.weatherwise.databinding.FragmentSimpleListBinding
import com.weatherwise.fragments.common.ListItem
import com.weatherwise.fragments.common.ListItemAdapter
import com.weatherwise.safety.Helplines
import com.weatherwise.safety.SosHelper

// Emergency phone numbers. Tapping one opens the dialer with the number ready.
class HelplinesFragment : Fragment() {

    private var _binding: FragmentSimpleListBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val listAdapter = ListItemAdapter(
        onItemClicked = { item ->
            Helplines.all.getOrNull(item.id)?.let { helpline ->
                val opened = SosHelper.openDialer(requireContext(), helpline.number)
                if (!opened) {
                    Toast.makeText(requireContext(), "No phone app found", Toast.LENGTH_SHORT).show()
                }
            }
        }
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
            textTitle.text = "Emergency Helplines"
            textSubtitle.text = "Numbers for India. Tap a number to open the dialer."
            textSubtitle.visibility = View.VISIBLE
            imageClose.setOnClickListener { findNavController().popBackStack() }
            recyclerView.adapter = listAdapter
        }
        listAdapter.setData(
            Helplines.all.mapIndexed { index, helpline ->
                ListItem(
                    id = index,
                    title = "${helpline.number}  -  ${helpline.name}",
                    subtitle = helpline.description,
                    accentColorRes = R.color.alert_danger
                )
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
