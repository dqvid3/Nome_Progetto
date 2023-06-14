package com.progetto.nomeprogetto.Fragments.MainActivity.Home

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.gson.JsonObject
import com.progetto.nomeprogetto.Adapters.CategoryImageAdapter
import com.progetto.nomeprogetto.Adapters.ProductAdapter
import com.progetto.nomeprogetto.ClientNetwork
import com.progetto.nomeprogetto.Objects.Product
import com.progetto.nomeprogetto.R
import com.progetto.nomeprogetto.databinding.FragmentHomeBinding
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater)

        binding.recyclerViewCategory.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        val categoriesList = HashMap<String,Bitmap>()
        setCategories(categoriesList)
        val imageAdapter = CategoryImageAdapter(categoriesList)
        binding.recyclerViewCategory.adapter = imageAdapter
        imageAdapter.setOnClickListener(object: CategoryImageAdapter.OnClickListener{
            override fun onClick(categoryName: String) {
                val bundle = Bundle()
                bundle.putString("category_name", categoryName)
                val productFragment = ProductFragment()
                productFragment.arguments = bundle
                parentFragmentManager.beginTransaction().hide(this@HomeFragment)
                    .add(R.id.home_fragment_home_container,productFragment)
                    .commit()
            }
        })

        binding.recyclerViewNews.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        val newProducts = ArrayList<Product>()
        setProducts(newProducts)
        val productAdapter = ProductAdapter(newProducts)
        binding.recyclerViewNews.adapter = productAdapter
        productAdapter.setOnClickListener(object: ProductAdapter.OnClickListener{
            override fun onClick(product: Product) {
                val bundle = Bundle()
                bundle.putParcelable("product", product)
                val productDetailFragment = ProductDetailFragment()
                productDetailFragment.arguments = bundle
                parentFragmentManager.beginTransaction().hide(this@HomeFragment)
                    .add(R.id.home_fragment_home_container,productDetailFragment)
                    .commit()
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
                                                binding.recyclerViewCategory.adapter?.notifyDataSetChanged()
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
        })
    }
    private fun setProducts(productList: ArrayList<Product>){

        val query = "SELECT id,name,description,price,width,height,length,main_picture_path,upload_date,\n" +
                "IFNULL((SELECT COUNT(*) FROM product_reviews WHERE product_id = p.id),0) AS review_count,\n" +
                "IFNULL((SELECT AVG(rating) FROM product_reviews WHERE product_id = p.id),0) AS avg_rating\n" +
                "FROM products p WHERE upload_date >= DATE_SUB(NOW(), INTERVAL 7 DAY) ORDER BY upload_date DESC LIMIT 20;"

        ClientNetwork.retrofit.select(query).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    var loadedProducts = 0
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
                            val avgRating = productObject.get("avg_rating").asDouble
                            val reviewsNumber = productObject.get("review_count").asInt
                            val date = productObject.get("upload_date").asString
                            val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                            val uploadDate = LocalDateTime.parse(date, formatter)
                            val main_picture_path = productObject.get("main_picture_path").asString
                            ClientNetwork.retrofit.image(main_picture_path).enqueue(object :
                                Callback<ResponseBody> {
                                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                    if(response.isSuccessful) {
                                        if (response.body()!=null) {
                                            val main_picture = BitmapFactory.decodeStream(response.body()?.byteStream())
                                            val product = Product(id, name, description, price,width,height,length,main_picture,avgRating,reviewsNumber,uploadDate)
                                            productList.add(product)
                                            loadedProducts++
                                            if(loadedProducts==productsArray.size())
                                                binding.recyclerViewNews.adapter?.notifyDataSetChanged()
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
        })
    }
}