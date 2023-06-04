package com.progetto.nomeprogetto.Fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.progetto.nomeprogetto.*
import com.progetto.nomeprogetto.databinding.FragmentHomeBinding
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        binding.recyclerView.adapter = ProductAdapter(ArrayList<Product>())

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if(!query.isNullOrBlank()) {
                    binding.searchView.clearFocus()
                    return true
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrBlank()) {
                    updateProducts(newText)
                }
                return true
            }
        })

        return binding.root
    }

    private fun updateProducts(productSearched: String){
        val products = ArrayList<Product>()

        val query = "SELECT p.id, p.name, p.description, p.price, p.width, p.height, p.length, p.stock, p.main_picture_path,\n" +
                "IFNULL((SELECT COUNT(*) FROM product_reviews WHERE product_id = p.id),0) AS review_count,\n" +
                "IFNULL((SELECT AVG(rating) FROM product_reviews WHERE product_id = p.id),0) AS avg_rating\n" +
                "FROM products p WHERE REPLACE(LOWER(p.name), ' ', '') LIKE REPLACE(LOWER('%$productSearched%'), ' ', '') " +
                "ORDER BY CASE WHEN LOWER(p.name) LIKE LOWER('$productSearched%') THEN 0 ELSE 1 END, p.name ASC;"

        ClientNetwork.retrofit.sendQuery(query).enqueue(
            object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    if (response.isSuccessful) {
                        val productsArray = response.body()?.getAsJsonArray("queryset")
                        if (productsArray != null && productsArray.size() > 0) {
                            for (i in 0 until productsArray.size()) {
                                val productObject = productsArray[i].asJsonObject
                                val id = productObject.get("id").asInt
                                val name = productObject.get("name").asString
                                val description = productObject.get("description").asString
                                val price = productObject.get("price").asDouble
                                val width = productObject.get("width").asDouble
                                val height = productObject.get("height").asDouble
                                val length = productObject.get("length").asDouble
                                val stock = productObject.get("stock").asInt
                                val avgRating = productObject.get("avg_rating").asDouble
                                val reviewsNumber = productObject.get("review_count").asInt
                                val main_picture_path = productObject.get("main_picture_path").asString

                                var main_picture: Bitmap? = null
                                ClientNetwork.retrofit.getImage(main_picture_path).enqueue(
                                    object : Callback<ResponseBody> {
                                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                            if(response.isSuccessful) {
                                                if (response.body()!=null) {
                                                    main_picture = BitmapFactory.decodeStream(response.body()?.byteStream())
                                                    val product = Product(id, name, description, price,width,height,length,stock,main_picture,avgRating,reviewsNumber)
                                                    products.add(product)
                                                }else{
                                                    val product = Product(id, name, description, price,width,height,length,stock,null,avgRating,reviewsNumber)
                                                    products.add(product)
                                                }
                                            }
                                        }
                                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
                                    }
                                )
                            }
                        } else {
                            Toast.makeText(requireContext(), "Non esistono prodotti con questo nome", Toast.LENGTH_LONG).show()
                        }
                        binding.recyclerView.adapter = ProductAdapter(products)
                        binding.recyclerView.adapter?.notifyDataSetChanged()
                    }
                }
                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Toast.makeText(requireContext(), "Failed on product request: " + t.message, Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}