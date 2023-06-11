package com.progetto.nomeprogetto.Fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.progetto.nomeprogetto.Adapters.CategoryImageAdapter
import com.progetto.nomeprogetto.ClientNetwork
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

        val linearLayoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        binding.recyclerView.layoutManager = linearLayoutManager
        val categoriesList = HashMap<String,Bitmap>()
        setCategories(categoriesList)
        val imageAdapter = CategoryImageAdapter(categoriesList)
        binding.recyclerView.adapter = imageAdapter
        imageAdapter.setOnClickListener(object: CategoryImageAdapter.OnClickListener{
            override fun onClick() {
                //cerca per categoria cliccata
            }
        })

        return binding.root
    }

    private fun setCategories(categoriesList: HashMap<String,Bitmap>){
        val query = "SELECT name,picture_path FROM categories"

        ClientNetwork.retrofit.select(query).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    val categoriesArray = response.body()?.getAsJsonArray("queryset")
                    if (categoriesArray != null && categoriesArray.size() > 0) {
                        var loadedCategories = 0
                        for (i in 0 until categoriesArray.size()) {
                            val categoryObject = categoriesArray[i].asJsonObject
                            val picture_path = categoryObject.get("picture_path").asString
                            val name = categoryObject.get("name").asString
                            ClientNetwork.retrofit.image(picture_path).enqueue(object :
                                Callback<ResponseBody> {
                                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                    if(response.isSuccessful) {
                                        if (response.body()!=null) {
                                            val picture = BitmapFactory.decodeStream(response.body()?.byteStream())
                                            categoriesList.put(name,picture)
                                            loadedCategories++
                                            if (loadedCategories == categoriesArray.size())
                                                binding.recyclerView.adapter?.notifyDataSetChanged()
                                        }
                                    }
                                }
                                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                    Toast.makeText(requireContext(), "Failed request: " + t.message, Toast.LENGTH_LONG).show()
                                }
                            })
                        }
                    }
                }
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed request: " + t.message, Toast.LENGTH_LONG).show()
            }
        }
        )
    }
}