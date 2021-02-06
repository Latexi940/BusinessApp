package com.example.businessapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import org.json.JSONException
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private lateinit var queue: RequestQueue
    private val url = "https://avoindata.prh.fi/bis/v1/"
    private val addressList = mutableListOf<Address>()
    private val contactList = mutableListOf<Contact>()
    private lateinit var addressAdapter: Adapter
    private lateinit var contactAdapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val addressListView: ListView = findViewById(R.id.AddressListView)
        addressAdapter = AddressListAdapter(this, addressList)
        addressListView.adapter = addressAdapter as AddressListAdapter

        val contactListView: ListView = findViewById(R.id.ContactListView)
        contactAdapter = ContactListAdapter(this, contactList)
        contactListView.adapter = contactAdapter as ContactListAdapter

        queue = Volley.newRequestQueue(this)
        val button: Button = findViewById(R.id.getInfoButton)
        val editText: TextView = findViewById(R.id.editText)
        button.setOnClickListener {
            if (editText.text.length == 9) {
                getInfo(editText.text.toString())
            } else {
                Log.d("DEVOCA", "Invalid businessID")
                Toast.makeText(this, getString(R.string.invalidID_warning), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        addressListView.setOnItemClickListener { _, _, pos, _ ->
            val item = addressAdapter.getItem(pos)
            onAddressListItemClick(item.toString())
        }

        contactListView.setOnItemClickListener { _, _, pos, _ ->
            val item = contactAdapter.getItem(pos) as Contact
            onContactListItemClick(item)
        }
    }

    private fun onContactListItemClick(item: Contact) {
        Log.d("DEVOCA", "Tapped $item")

        if (item.isWebsite) {
            val alert = AlertDialog.Builder(this).create()
            alert.setTitle("Open web browser?")
            alert.setButton(
                AlertDialog.BUTTON_POSITIVE,
                getString(R.string.dialog_yes)
            ) { dialog, _ ->
                dialog.dismiss()
                try {
                    val uri: Uri = Uri.parse("https://${item.value}")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.d("Devoca", "Error: $e")
                    Toast.makeText(this, "Cannot open web page", Toast.LENGTH_SHORT).show()
                }
            }
            alert.setButton(
                AlertDialog.BUTTON_NEGATIVE,
                getString(R.string.dialog_no)
            ) { dialog, _ ->
                dialog.dismiss()
            }
            alert.show()

        }

        if (item.isPhoneNumber) {
            val alert = AlertDialog.Builder(this).create()
            alert.setTitle(item.value)
            alert.setButton(
                AlertDialog.BUTTON_NEUTRAL,
                getString(R.string.contactDialog_call)
            ) { dialog, _ ->
                callContact(item.value)
                dialog.dismiss()
            }
            alert.setButton(
                AlertDialog.BUTTON_NEGATIVE,
                getString(R.string.contactDialog_add)
            ) { dialog, _ ->
                addContact(item.value)
                dialog.dismiss()
            }
            alert.setButton(
                AlertDialog.BUTTON_POSITIVE,
                getString(R.string.contactDialog_cancel)
            ) { dialog, _ ->
                Log.d("DEVOCA", "Canceling")
                dialog.dismiss()
            }
            alert.show()

            val buttonNegative = alert.getButton(AlertDialog.BUTTON_NEGATIVE)
            val buttonPositive = alert.getButton(AlertDialog.BUTTON_POSITIVE)
            val buttonNeutral = alert.getButton(AlertDialog.BUTTON_NEUTRAL)
            val layout = buttonNegative.layoutParams as LinearLayout.LayoutParams
            layout.weight = 10f
            buttonNegative.layoutParams = layout
            buttonPositive.layoutParams = layout
            buttonNeutral.layoutParams = layout
        }
    }

    private fun onAddressListItemClick(address: String) {
        Log.d("DEVOCA", "Tapped $address")
        val intentUri = Uri.parse("geo:0,0?q=$address")
        val mapIntent = Intent(Intent.ACTION_VIEW, intentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        val alert = AlertDialog.Builder(this).create()
        alert.setTitle("Open Google Maps?")
        alert.setButton(
            AlertDialog.BUTTON_POSITIVE,
            getString(R.string.dialog_yes)
        ) { dialog, _ ->
            dialog.dismiss()
            try {
                Log.d("DEVOCA", "Opening Maps")
                startActivity(mapIntent)
            } catch (e: Exception) {
                Log.d("DEVOCA", "Error: $e")
                Toast.makeText(this, "Cannot open map application", Toast.LENGTH_SHORT).show()
            }
        }
        alert.setButton(
            AlertDialog.BUTTON_NEGATIVE,
            getString(R.string.dialog_no)
        ) { dialog, _ ->
            dialog.dismiss()
        }
        alert.show()
    }

    private fun callContact(contactNumber: String) {
        Log.d("DEVOCA", "RingRing $contactNumber")
        val callIntent = Intent(Intent.ACTION_DIAL)
        callIntent.data = Uri.parse("tel:$contactNumber")
        startActivity(callIntent)
    }

    private fun addContact(contactNumber: String) {
        Log.d("DEVOCA", "Adding contact $contactNumber")

        val contactIntent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
            type = ContactsContract.RawContacts.CONTENT_TYPE
        }
        contactIntent.putExtra(ContactsContract.Intents.Insert.PHONE, contactNumber)
        startActivity(contactIntent)
    }

    private fun getInfo(businessID: String) {
        Log.d("DEVOCA", "Fetching info for $businessID")

        val fetchUrl = url + businessID
        val request = JsonObjectRequest(Request.Method.GET, fetchUrl, null, { response ->
            Log.d("DEVOCA", "Fetched something...")
            showData(response)
        }, {
            Toast.makeText(this, "Error fetching the data: $it", Toast.LENGTH_SHORT).show()
        })
        queue.add(request)
    }

    private fun showData(response: JSONObject) {
        try {
            val resultsArray = response.getJSONArray("results")
            val results = resultsArray.getJSONObject(0)
            val name = results.getString("name")
            val businessID = results.getString("businessId")
            val allAddresses = results.getJSONArray("addresses")
            val allContacts = results.getJSONArray("contactDetails")
            Log.d("DEVOCA", "Found company called: $name")

            addressList.clear()
            for (i in 0 until allAddresses.length()) {
                val address = allAddresses.getJSONObject(i)
                if (address.getString("language") == "FI") {

                    val newAddress = Address(
                        address.getString("street"),
                        address.getString("postCode"),
                        address.getString("city")
                    )
                    var isAddressAlreadyInList = false
                    addressList.forEach {
                        if (it.street == newAddress.street && it.postCode == newAddress.postCode) {
                            isAddressAlreadyInList = true
                        }
                    }
                    if (!isAddressAlreadyInList) {
                        addressList.add(newAddress)
                    }
                }
            }

            contactList.clear()
            for (i in 0 until allContacts.length()) {
                val contact = allContacts.getJSONObject(i)
                if (contact.getString("language") == "FI") {
                    var isWebpage = false
                    var isPhoneNumber = false
                    if (contact.getString("type") == "Kotisivun www-osoite") {
                        isWebpage = true
                    }
                    if (contact.getString("type") == "Matkapuhelin" || contact.getString("type") == "Puhelin") {
                        isPhoneNumber = true
                    }
                    val newContact = Contact(
                        contact.getString("type"),
                        contact.getString("value"),
                        isWebpage,
                        isPhoneNumber
                    )
                    if (newContact.value != "") {
                        contactList.add(newContact)
                    }
                }
            }

            val nameLabel: TextView = findViewById(R.id.NameLabel)
            nameLabel.text = name
            val idLabel: TextView = findViewById(R.id.IDLabel)
            idLabel.text = businessID

            (addressAdapter as AddressListAdapter).notifyDataSetChanged()
            (contactAdapter as ContactListAdapter).notifyDataSetChanged()

        } catch (e: JSONException) {
            Log.d("DEVOCA", "Error: $e")
        }
    }
}