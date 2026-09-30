package com.weatherwise.fragments.contacts

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.weatherwise.data.EmergencyContact
import com.weatherwise.databinding.DialogAddContactBinding
import com.weatherwise.databinding.FragmentSimpleListBinding
import com.weatherwise.fragments.common.ListItem
import com.weatherwise.fragments.common.ListItemAdapter
import com.weatherwise.storage.SharedPreferencesManager
import org.koin.android.ext.android.inject

// Screen where the user adds or removes the people who receive the SOS message.
class ContactsFragment : Fragment() {

    private companion object {
        const val MAX_CONTACTS = 5
        // Optional "+", then 7 to 15 digits
        val PHONE_PATTERN = Regex("^\\+?[0-9]{7,15}$")
    }

    private var _binding: FragmentSimpleListBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val sharedPreferencesManager: SharedPreferencesManager by inject()

    private val listAdapter = ListItemAdapter(
        onItemClicked = { item -> confirmRemoveContact(item.id) }
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
            textTitle.text = "Emergency Contacts"
            textSubtitle.text = "These people receive your SOS message. Tap a contact to remove it."
            textSubtitle.visibility = View.VISIBLE
            imageClose.setOnClickListener { findNavController().popBackStack() }
            recyclerView.adapter = listAdapter
            buttonAction.text = "Add contact"
            buttonAction.visibility = View.VISIBLE
            buttonAction.setOnClickListener { showAddContactDialog() }
        }
        refreshList()
    }

    // Reads the saved contacts and shows them on screen.
    private fun refreshList() {
        val contacts = sharedPreferencesManager.getEmergencyContacts()
        listAdapter.setData(
            contacts.mapIndexed { index, contact ->
                ListItem(id = index, title = contact.name, subtitle = contact.phone)
            }
        )
        if (contacts.isEmpty()) {
            binding.textEmpty.text = "No emergency contacts yet.\nAdd at least one so SOS can work."
            binding.textEmpty.visibility = View.VISIBLE
        } else {
            binding.textEmpty.visibility = View.GONE
        }
    }

    private fun showAddContactDialog() {
        if (sharedPreferencesManager.getEmergencyContacts().size >= MAX_CONTACTS) {
            Toast.makeText(requireContext(), "You can add up to $MAX_CONTACTS contacts", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogBinding = DialogAddContactBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add emergency contact")
            .setView(dialogBinding.root)
            .setPositiveButton("Save", null)   // real click handling is set below
            .setNegativeButton("Cancel", null)
            .create()

        // We set the Save click ourselves, so the dialog stays open if the input is wrong.
        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                val name = dialogBinding.inputName.editText?.text?.toString()?.trim().orEmpty()
                val rawPhone = dialogBinding.inputPhone.editText?.text?.toString().orEmpty()
                // Remove spaces, dashes and brackets: "98765-43210" -> "9876543210"
                val phone = rawPhone.replace(Regex("[\\s\\-()]"), "")

                dialogBinding.inputName.error = null
                dialogBinding.inputPhone.error = null

                if (name.isEmpty()) {
                    dialogBinding.inputName.error = "Please enter a name"
                } else if (!PHONE_PATTERN.matches(phone)) {
                    dialogBinding.inputPhone.error = "Please enter a valid phone number"
                } else {
                    addContact(EmergencyContact(name = name, phone = phone))
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    private fun addContact(contact: EmergencyContact) {
        val contacts = sharedPreferencesManager.getEmergencyContacts().toMutableList()
        contacts.add(contact)
        sharedPreferencesManager.saveEmergencyContacts(contacts)
        refreshList()
    }

    private fun confirmRemoveContact(index: Int) {
        val contacts = sharedPreferencesManager.getEmergencyContacts()
        val contact = contacts.getOrNull(index) ?: return
        AlertDialog.Builder(requireContext())
            .setTitle("Remove contact?")
            .setMessage("${contact.name} (${contact.phone}) will no longer receive your SOS messages.")
            .setPositiveButton("Remove") { _, _ ->
                val updated = contacts.toMutableList()
                updated.removeAt(index)
                sharedPreferencesManager.saveEmergencyContacts(updated)
                refreshList()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
