package com.weatherwise.fragments.guides

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.weatherwise.databinding.FragmentSimpleListBinding
import com.weatherwise.fragments.common.GuideDialog
import com.weatherwise.fragments.common.ListItem
import com.weatherwise.fragments.common.ListItemAdapter
import com.weatherwise.safety.SafetyGuides

// Offline safety guides: what to do before, during and after a disaster.
class GuidesFragment : Fragment() {

    private var _binding: FragmentSimpleListBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val listAdapter = ListItemAdapter(
        onItemClicked = { item ->
            SafetyGuides.all.getOrNull(item.id)?.let { guide ->
                GuideDialog.show(requireContext(), guide)
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
            textTitle.text = "Safety Guides"
            textSubtitle.text = "Works without internet. Tap a guide to read it."
            textSubtitle.visibility = View.VISIBLE
            imageClose.setOnClickListener { findNavController().popBackStack() }
            recyclerView.adapter = listAdapter
        }
        listAdapter.setData(
            SafetyGuides.all.mapIndexed { index, guide ->
                ListItem(
                    id = index,
                    title = "${guide.emoji} ${guide.title}",
                    subtitle = guide.summary
                )
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
