package com.progetto.nomeprogetto.Fragments.MainActivity.Account

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.progetto.nomeprogetto.ClientNetwork
import com.progetto.nomeprogetto.Objects.UserAddress
import com.progetto.nomeprogetto.R
import com.progetto.nomeprogetto.databinding.FragmentAddAddressBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddAddressFragment : Fragment() {

    private lateinit var binding: FragmentAddAddressBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddAddressBinding.inflate(inflater)

        binding.saveButton.setOnClickListener {
            val name = binding.name.text.toString()
            val state = binding.state.text.toString()
            val address_line1 = binding.addressLine1.text.toString()
            val address_line2 = binding.addressLine2.text.toString()
            val cap = binding.cap.text.toString().trim()
            val city = binding.city.text.toString()
            val county = binding.county.text.toString()
            if(name.isEmpty() || state.isEmpty() || address_line1.isEmpty() ||  cap.isEmpty() || city.isEmpty() || county.isEmpty())
                Toast.makeText(requireContext(), "Perfavore riempi tutti i campi", Toast.LENGTH_SHORT).show()
            else {
                val sharedPref = requireActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
                val userId = sharedPref.getInt("ID", 0)
                val address = UserAddress(name,state,address_line1,address_line2,cap,city,county)
                addressExists(address,userId) { exists ->
                    if(exists)
                        Toast.makeText(requireContext(), "Indirizzo già esistente", Toast.LENGTH_SHORT).show()
                    else
                        addAddress(address,userId)
                }
            }
        }

        binding.backButton.setOnClickListener{
            closeFragment()
        }

        return binding.root
    }

    private fun addressExists(address: UserAddress,userId: Int, callback: (Boolean) -> Unit){
        var query = "SELECT id FROM user_addresses WHERE user_id = $userId AND name = '${address.name}' " +
                "AND address_line1 = '${address.address_line1}' AND city = '${address.city}' AND state = '${address.state}' " +
                "AND postal_code = ${address.cap} AND county = '${address.county}';"
        println(query)

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

    private fun addAddress(a: UserAddress,userId: Int) {
        val query = "INSERT INTO user_addresses (user_id, address_line1, address_line2, city, state, postal_code," +
                " county, name) " +
                "VALUES ($userId,'${a.address_line1}', '${a.address_line2}', '${a.city}', '${a.state}', '${a.cap}', " +
                "'${a.county}', '${a.name}');"
        println(query)
        ClientNetwork.retrofit.insert(query).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if(response.isSuccessful) {
                    Toast.makeText(requireContext(), "Indirizzo salvato con successo",Toast.LENGTH_SHORT).show()
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
    }
}
