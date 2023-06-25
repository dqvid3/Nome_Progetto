package com.progetto.nomeprogetto.Fragments.MainActivity.Home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.progetto.nomeprogetto.R

class AddShippingAddressFragment : Fragment() {

    private lateinit var etFullName: EditText
    private lateinit var etAddress: EditText
    private lateinit var etCity: EditText
    private lateinit var etPostalCode: EditText
    private lateinit var btnSave: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_add_shipping_address, container, false)

        etAddress = view.findViewById(R.id.et_address)
        etCity = view.findViewById(R.id.et_city)
        etPostalCode = view.findViewById(R.id.et_postal_code)
        btnSave = view.findViewById(R.id.btn_save)

        btnSave.setOnClickListener {
            saveShippingAddress()
        }

        return view
    }

    private fun saveShippingAddress() {
        val address = etAddress.text.toString()
        val city = etCity.text.toString()
        val postalCode = etPostalCode.text.toString()

        // Esegui qui la logica per salvare l'indirizzo di spedizione nel tuo sistema

        Toast.makeText(activity, "Indirizzo di spedizione salvato con successo", Toast.LENGTH_SHORT).show()
        clearFields()
    }

    private fun clearFields() {
        etFullName.text.clear()
        etAddress.text.clear()
        etCity.text.clear()
        etPostalCode.text.clear()
    }
}
