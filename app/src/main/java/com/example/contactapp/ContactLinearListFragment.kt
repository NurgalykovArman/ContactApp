package com.example.contactapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.contactapp.data.Contact
import android.provider.ContactsContract
import android.widget.TextView


class ContactLinearListFragment : Fragment(R.layout.fragment_contact_linear_list) {
    private val contactsList = mutableListOf<Contact>()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getPhoneContacts()
        } else {
            Toast.makeText(requireContext(), "Permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_contact_linear_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (checkPermission()) {
            getPhoneContacts()
        } else {
            requestPermission()
        }
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
    }

    private fun getPhoneContacts() {
        val cursor = requireActivity().contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val nameColumnIndex =
                    it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberColumnIndex =
                    it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val contactName = it.getString(nameColumnIndex)
                val contactNumber = it.getString(numberColumnIndex)

                val contact = Contact(contactName, contactNumber)
                contactsList.add(contact)

                // Создание и добавление TextView для каждого контакта в LinearLayout
                val contactView = LayoutInflater.from(requireContext()).inflate(R.layout.item_contact, null)
                val nameTextView = contactView.findViewById<TextView>(R.id.nameTextView)
                val numberTextView = contactView.findViewById<TextView>(R.id.numberTextView)

                nameTextView.text = contactName
                numberTextView.text = contactNumber
                contactView.setOnClickListener {
                    context?.let {
                        val intent = Intent(Intent.ACTION_DIAL)
                        intent.data = Uri.parse("tel:$contactNumber")
                        context?.startActivity(intent)
                    }
                }

                (view as? ViewGroup)?.addView(contactView)
            }
        }
    }

}