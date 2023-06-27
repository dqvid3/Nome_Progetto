package com.progetto.nomeprogetto.Activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.gson.JsonObject
import com.progetto.nomeprogetto.ClientNetwork
import com.progetto.nomeprogetto.Objects.Product
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

        binding.backButton.setOnClickListener{
            this.finish()
        }

        //carica tutti gli indirizzi, se non ne esiste uno address_text = "Aggiungi un indirizzo di consegna"
        //altrimenti carica l'indirizzo corrente

        //gestione indirizzi:
        val addressSelectionLayout = binding.addressSelection
        val modifyAddressButton = addressSelectionLayout.modifyAddress
        modifyAddressButton.setOnClickListener{
            if(modifyAddressButton.text.equals("Modifica")) {
                modifyAddressButton.text = "Chiudi"
                addressSelectionLayout.layoutIndirizzo.visibility = View.GONE
                addressSelectionLayout.addressListLayout.visibility = View.VISIBLE
            }else{
                modifyAddressButton.text = "Modifica"
                addressSelectionLayout.layoutIndirizzo.visibility = View.VISIBLE
                addressSelectionLayout.addressListLayout.visibility = View.GONE
            }
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