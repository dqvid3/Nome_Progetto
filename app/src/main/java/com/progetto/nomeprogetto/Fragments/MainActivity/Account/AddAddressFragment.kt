package com.progetto.nomeprogetto.Fragments.MainActivity.Account

import android.animation.Animator
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.progetto.nomeprogetto.Activities.BuyActivity
import com.progetto.nomeprogetto.ClientNetwork
import com.progetto.nomeprogetto.Objects.Product
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

        val address: UserAddress? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("address", UserAddress::class.java)
        } else
            arguments?.getParcelable("address")

        var modify = false

        if (address != null){
            binding.name.setText(address.name)
            binding.state.setText(address.state)
            binding.addressLine1.setText(address.address_line1)
            binding.addressLine2.setText(address.address_line2)
            binding.cap.setText(address.cap)
            binding.city.setText(address.city)
            binding.county.setText(address.county)
            modify = true
        }

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
                val insertedAddress = UserAddress(0,name,state,address_line1,address_line2,cap,city,county)
                if (modify){
                    if (address != null) {
                        if (address.name == name && address.state == state && address.address_line1 == address_line1
                            && address.cap == cap && address.city == city && address.county == county)
                            Toast.makeText(requireContext(), "Non hai modificato nulla", Toast.LENGTH_SHORT).show()
                        else {
                            insertedAddress.id = address.id
                            updateAddress(insertedAddress)
                        }
                    }
                }else {
                    addressExists(insertedAddress, userId) { exists ->
                        if (exists)
                            Toast.makeText(requireContext(), "Indirizzo già esistente", Toast.LENGTH_SHORT).show()
                        else
                            addAddress(insertedAddress, userId)
                    }
                }
            }
        }

        binding.backButton.setOnClickListener{
            closeFragment()
        }

        binding.animationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {}
            override fun onAnimationEnd(p0: Animator) {
                closeFragment()
            }
            override fun onAnimationCancel(p0: Animator) {}
            override fun onAnimationRepeat(p0: Animator) {}
        })


        return binding.root
    }

    private fun addressExists(address: UserAddress,userId: Int, callback: (Boolean) -> Unit){
        var query = "SELECT id FROM user_addresses WHERE user_id = $userId AND name = '${address.name}' " +
                "AND address_line1 = '${address.address_line1}' AND city = '${address.city}' AND state = '${address.state}' " +
                "AND postal_code = ${address.cap} AND county = '${address.county}';"

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

        ClientNetwork.retrofit.insert(query).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if(response.isSuccessful) {
                    Toast.makeText(requireContext(), "Indirizzo salvato con successo",Toast.LENGTH_SHORT).show()
                    startAnimation()
                }else
                    Toast.makeText(requireContext(), "Errore nel salvataggio, riprova",Toast.LENGTH_SHORT).show()
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) =
                Toast.makeText(requireContext(), "Failed request: " + t.message, Toast.LENGTH_LONG).show()
        })
    }

    private fun updateAddress(a: UserAddress) {
        val query = "UPDATE user_addresses SET address_line1 = '${a.address_line1}',address_line2 = '${a.address_line2}', " +
                "city = '${a.city}',state = '${a.state}',postal_code = '${a.cap}',county = '${a.county}',name = '${a.name}' " +
                "WHERE id = ${a.id};"

        ClientNetwork.retrofit.update(query).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if(response.isSuccessful) {
                    Toast.makeText(requireContext(), "Indirizzo salvato con successo",Toast.LENGTH_SHORT).show()
                    startAnimation()
                }else
                    Toast.makeText(requireContext(), "Errore nel salvataggio, riprova",Toast.LENGTH_SHORT).show()
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) =
                Toast.makeText(requireContext(), "Failed request: " + t.message, Toast.LENGTH_LONG).show()
        })
    }

    private fun startAnimation(){
        val checkAnimationView: LottieAnimationView = binding.animationView
        binding.animationView.speed = 3.0f
        checkAnimationView.playAnimation()
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
        (activity as? BuyActivity)?.onAddAddressFragmentClose()
    }
}
