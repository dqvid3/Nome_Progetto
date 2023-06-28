package com.progetto.nomeprogetto.Fragments.MainActivity.Account

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.progetto.nomeprogetto.Activities.BuyActivity
import com.progetto.nomeprogetto.ClientNetwork
import com.progetto.nomeprogetto.Objects.User
import com.progetto.nomeprogetto.Objects.UserAddress
import com.progetto.nomeprogetto.Objects.UserCard
import com.progetto.nomeprogetto.R
import com.progetto.nomeprogetto.databinding.FragmentAddCardBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class AddCardFragment : Fragment() {

    private lateinit var binding: FragmentAddCardBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddCardBinding.inflate(inflater)

        val card: UserCard? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("card", UserCard::class.java)
        } else
            arguments?.getParcelable("card")

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val months = arrayOf("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12")
        val years = Array(21) { (currentYear + it).toString() }
        val monthSpinner = binding.month
        val yearSpinner = binding.year

        var modify = false

        var cardMonth = ""
        var cardYear = ""
        if (card != null){
            binding.cardholderName.setText(card.name)
            binding.cardNumber.setText(card.card_number)
            val date = card.expiration_date.split("/")
            cardMonth = date[0]
            val monthIndex = months.indexOf(cardMonth)
            if (monthIndex != -1)
                monthSpinner.setSelection(monthIndex)
            cardYear = date[1]
            val yearIndex = years.indexOf(cardYear)
            if (yearIndex != -1)
                yearSpinner.setSelection(yearIndex)
            modify = true
        }

        val monthAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        monthSpinner.adapter = monthAdapter

        val yearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        yearSpinner.adapter = yearAdapter

        binding.saveButton.setOnClickListener {
            val name = binding.cardholderName.text.toString()
            val card_number = binding.cardNumber.text.toString()
            val month = binding.month.selectedItem
            val year = binding.year.selectedItem
            val cvv = binding.cvv.text.toString().trim()
            if (name.isEmpty() || card_number.isEmpty() || cvv.length != 3) {
                Toast.makeText(requireContext(), "Perfavore riempi tutti i campi correttamente", Toast.LENGTH_SHORT).show()
            } else {
                val sharedPref = requireActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
                val userId = sharedPref.getInt("ID", 0)
                val insertedCard = UserCard(0,name, card_number, "$month/$year", cvv.toInt())
                if (modify){
                    if (card != null) {
                        if (card.name == name && card.card_number == card_number && cardMonth == month &&  cardYear == year && cvv.isBlank())
                            Toast.makeText(requireContext(), "Non hai modificato nulla", Toast.LENGTH_SHORT).show()
                        else {
                            insertedCard.id = card.id
                            updateCard(insertedCard)
                        }
                    }
                }else {
                    cardExists(insertedCard, userId) { exists ->
                        if (exists)
                            Toast.makeText(requireContext(), "Carta già esistente nel tuo account", Toast.LENGTH_SHORT).show()
                        else
                            addCard(insertedCard, userId)
                    }
                }
            }
        }

        binding.backButton.setOnClickListener{
            closeFragment()
        }

        return binding.root
    }

    private fun cardExists(card: UserCard,userId: Int, callback: (Boolean) -> Unit){
        var query = "SELECT id FROM user_payments WHERE user_id = $userId AND card_number = ${card.card_number};"

        ClientNetwork.retrofit.select(query).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    val resultSetSize = (response.body()?.get("queryset") as JsonArray).size()
                    if(resultSetSize==1)
                        callback(true)
                    else
                        callback(false)
                }
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) =
                Toast.makeText(requireContext(), "Failed request: " + t.message, Toast.LENGTH_LONG).show()
        })
    }

    private fun addCard(c: UserCard, userId: Int) {
        val query = "INSERT INTO user_payments (user_id,card_number,cardholder_name,cvv,expiration_date) " +
                "VALUES ($userId,'${c.card_number}','${c.name}',${c.cvv},'${c.expiration_date}');"

        ClientNetwork.retrofit.insert(query).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if(response.isSuccessful) {
                    Toast.makeText(requireContext(), "Carta salvata con successo",Toast.LENGTH_SHORT).show()
                    closeFragment()
                }else
                    Toast.makeText(requireContext(), "Errore nel salvataggio, riprova",Toast.LENGTH_SHORT).show()
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) =
                Toast.makeText(requireContext(), "Failed request: " + t.message, Toast.LENGTH_LONG).show()
        })
    }

    private fun updateCard(c: UserCard) {
        val query = "UPDATE user_payments SET card_number = '${c.card_number}',cardholder_name = '${c.name}', " +
                "expiration_date = '${c.expiration_date}',cvv = '${c.cvv}' " +
                "WHERE id = ${c.id};"

        ClientNetwork.retrofit.update(query).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if(response.isSuccessful) {
                    Toast.makeText(requireContext(), "Carta salvata con successo",Toast.LENGTH_SHORT).show()
                    closeFragment()
                }else
                    Toast.makeText(requireContext(), "Errore nel salvataggio, riprova",Toast.LENGTH_SHORT).show()
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) =
                Toast.makeText(requireContext(), "Failed request: " + t.message, Toast.LENGTH_LONG).show()
        })
    }

    private fun closeFragment(){
        parentFragmentManager.beginTransaction()
            .remove(this)
            .commit()
        parentFragmentManager.findFragmentByTag("AccountFragment")?.let {
            parentFragmentManager.beginTransaction()
                .show(it)
                .commit()
        }
        (activity as? BuyActivity)?.onAddCardFragmentClose()
    }
}
