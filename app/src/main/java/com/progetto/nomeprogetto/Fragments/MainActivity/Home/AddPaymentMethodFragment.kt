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

class AddPaymentMethodFragment : Fragment() {

    private lateinit var etCardNumber: EditText
    private lateinit var etExpirationDate: EditText
    private lateinit var etCvv: EditText
    private lateinit var btnSave: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_add_payment_method, container, false)

        etCardNumber = view.findViewById(R.id.et_card_number)
        etExpirationDate = view.findViewById(R.id.et_expiration_date)
        etCvv = view.findViewById(R.id.et_cvv)
        btnSave = view.findViewById(R.id.btn_save)

        btnSave.setOnClickListener {
            savePaymentMethod()
        }

        return view
    }

    private fun savePaymentMethod() {
        val cardNumber = etCardNumber.text.toString()
        val expirationDate = etExpirationDate.text.toString()
        val cvv = etCvv.text.toString()

        // Esegui qui la logica per salvare il metodo di pagamento nel tuo sistema

        Toast.makeText(activity, "Metodo di pagamento salvato con successo", Toast.LENGTH_SHORT).show()
        clearFields()
    }

    private fun clearFields() {
        etCardNumber.text.clear()
        etExpirationDate.text.clear()
        etCvv.text.clear()
    }
}
