package com.progetto.nomeprogetto.Activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.gson.JsonObject
import com.progetto.nomeprogetto.Adapters.AddressAdapter
import com.progetto.nomeprogetto.ClientNetwork
import com.progetto.nomeprogetto.Objects.Product
import com.progetto.nomeprogetto.Objects.UserAddress
import com.progetto.nomeprogetto.R
import com.progetto.nomeprogetto.databinding.ActivityBuyBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BuyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBuyBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("ID", 0)

        binding.backButton.setOnClickListener{
            this.finish()
        }

        //carica tutti gli indirizzi, se non ne esiste uno address_text = "Aggiungi un indirizzo di consegna"
        //altrimenti carica l'indirizzo corrente

        //gestione indirizzi:
        loadAddresses(userId)
        val addressSelectionLayout = binding.addressSelection
        val modifyAddressButton = addressSelectionLayout.modifyAddress
        val addAddressButton = addressSelectionLayout.addAddress
        modifyAddressButton.setOnClickListener{
            if(modifyAddressButton.text.equals(" Modifica")) {
                modifyAddressButton.text = " Chiudi"
                modifyAddressButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_close,0,0,0)
                modifyAddressButton.compoundDrawableTintList = ContextCompat.getColorStateList(this,android.R.color.darker_gray)
                addressSelectionLayout.layoutIndirizzo.visibility = View.GONE
                addressSelectionLayout.addressListLayout.visibility = View.VISIBLE
            }else{
                modifyAddressButton.text = " Modifica"
                modifyAddressButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_settings,0,0,0)
                modifyAddressButton.compoundDrawableTintList = ContextCompat.getColorStateList(this,android.R.color.darker_gray)
                addressSelectionLayout.layoutIndirizzo.visibility = View.VISIBLE
                addressSelectionLayout.addressListLayout.visibility = View.GONE
            }
        }
        addAddressButton.setOnClickListener{

        }

        /*
        var totalAmt = 0.0
        val orderList = ArrayList<Product>()
        for (p in productList) {
            if (p.stock != null && p.stock > 0) {
                orderList.add(p)
                totalAmt += p.price * (p.quantity ?: 0)
            }
        }*/
    }

    private fun loadAddresses(userId: Int){
        val addressList = ArrayList<UserAddress>()
        getAddressId(userId) { selectedId ->
            // Set the addresses with the selected ID
            setAddresses(addressList, userId, selectedId)
        }

        val recyclerView = binding.addressSelection.recyclerAddressView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter =  AddressAdapter(addressList)
    }

    private fun setAddresses(addressList: ArrayList<UserAddress>,userId: Int,selected_id:Int){
        val query = "SELECT id,address_line1,address_line2,name,city,county,state,postal_code " +
                "FROM user_addresses WHERE user_id=$userId;"

        val addressSelectionLayout = binding.addressSelection

        ClientNetwork.retrofit.select(query).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    var loadedAddresses = 0
                    val addressArray = response.body()?.getAsJsonArray("queryset")
                    if (addressArray != null && addressArray.size() > 0) {
                        for (i in 0 until addressArray.size()) {
                            val addressObject = addressArray[i].asJsonObject
                            val id = addressObject.get("id").asInt
                            val address_line1 = addressObject.get("address_line1").asString
                            val address_line2 = addressObject.get("address_line2").asString
                            val name = addressObject.get("name").asString
                            val city = addressObject.get("city").asString
                            val county = addressObject.get("county").asString
                            val postal_code = addressObject.get("postal_code").asString
                            val state = addressObject.get("state").asString
                            val address = UserAddress(name,state,address_line1,address_line2,postal_code,city,county,
                                selected_id==id)
                            if (selected_id==id)
                                setDefaultAddress(address)
                            loadedAddresses++
                            addressList.add(address)
                            if (loadedAddresses==addressArray.size())
                                addressSelectionLayout.recyclerAddressView.adapter?.notifyDataSetChanged()
                        }
                    }else{
                        addressSelectionLayout.addressListLayout.visibility = View.VISIBLE
                        addressSelectionLayout.addressListText.visibility = View.GONE
                        addressSelectionLayout.modifyAddress.visibility = View.GONE
                        addressSelectionLayout.addressText.text = "Aggiungi un indirizzo di consegna"
                    }
                }
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(this@BuyActivity , "Failed request: " + t.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setDefaultAddress(address: UserAddress){
        binding.addressSelection.layoutIndirizzo.visibility = View.VISIBLE
        val addressSelection = binding.addressSelection
        addressSelection.name.text = address.name
        addressSelection.addressLine1.text = address.address_line1
        if(addressSelection.addressLine2.text.isBlank())
            addressSelection.addressLine2.visibility = View.GONE
        else
            addressSelection.addressLine2.text = " , " + address.address_line2
        addressSelection.city.text = address.city
        addressSelection.county.text = " , " + address.county
        addressSelection.cap.text = address.cap
        addressSelection.state.text = " , "  + address.state
    }

    private fun getAddressId(userId: Int, callback: (Int) -> Unit) {
        val query = "SELECT current_address_id FROM users WHERE id=$userId;"
        ClientNetwork.retrofit.select(query).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    val resultSet = response.body()?.getAsJsonArray("queryset")
                    if (resultSet != null && resultSet.size() > 0) {
                        val addressId = resultSet[0].asJsonObject.get("current_address_id").asInt
                        callback.invoke(addressId)
                    } else
                        callback.invoke(1) // No current address
                }
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) =
                Toast.makeText(this@BuyActivity, "Failed request: " + t.message, Toast.LENGTH_LONG).show()
        })
    }

    private fun createOrder(orderList: ArrayList<Product>,totalAmt: Double){
        val sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("ID", 0)
        var query = "INSERT INTO orders (user_id,totalAmount,payment_id,address_id) VALUES ($userId,$totalAmt,,);"

        ClientNetwork.retrofit.insert(query).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    println(response.body()?.asString)
                    for(product in orderList){
                        query = "INSERT INTO order_items (order_id,color_id,quantity,price) " +
                                "VALUES ($userId,$totalAmt);"

                        ClientNetwork.retrofit.insert(query).enqueue(object : Callback<JsonObject> {
                            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {}
                            override fun onFailure(call: Call<JsonObject>, t: Throwable) =
                                Toast.makeText(this@BuyActivity, "Failed request: " + t.message, Toast.LENGTH_LONG).show()
                        })
                    }
                }
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) =
                Toast.makeText(this@BuyActivity, "Failed request: " + t.message, Toast.LENGTH_LONG).show()
        })
    }
}