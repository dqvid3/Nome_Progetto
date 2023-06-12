package com.progetto.nomeprogetto.Fragments.MainActivity.Home

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.gson.JsonObject
import com.progetto.nomeprogetto.Adapters.ProductAdapter
import com.progetto.nomeprogetto.ClientNetwork
import com.progetto.nomeprogetto.Objects.Product
import com.progetto.nomeprogetto.R
import com.progetto.nomeprogetto.databinding.FragmentProductBinding
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ProductFragment : Fragment() {

    private lateinit var binding: FragmentProductBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProductBinding.inflate(inflater)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val itemDecoration = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        binding.recyclerView.addItemDecoration(itemDecoration)
        val productList = ArrayList<Product>()
        val adapter = ProductAdapter(productList)
        binding.recyclerView.adapter = adapter
        arguments?.getString("searchQuery")?.let { setProducts(it,productList) }

        adapter.setOnClickListener(object: ProductAdapter.OnClickListener{
            override fun onClick(product: Product) {
                val bundle = Bundle()
                bundle.putParcelable("product", product)
                val productDetailFragment = ProductDetailFragment()
                productDetailFragment.arguments = bundle
                parentFragmentManager.beginTransaction().hide(this@ProductFragment)
                    .add(R.id.home_fragment_home_container,productDetailFragment)
                    .commit()
            }
        })

        binding.sortButton.setOnClickListener{
            showSortOptions(binding.sortButton,productList)
        }

        binding.backButton.setOnClickListener{
            parentFragmentManager.beginTransaction()
                .replace(R.id.home_fragment_home_container,HomeFragment())
                .commit()
        }

        return binding.root
    }

    private fun setProducts(productSearched: String, productList: ArrayList<Product>){
        val query = "SELECT id,name,description,price,width,height,length,stock,main_picture_path,upload_date,\n" +
                "IFNULL((SELECT COUNT(*) FROM product_reviews WHERE product_id = p.id),0) AS review_count,\n" +
                "IFNULL((SELECT AVG(rating) FROM product_reviews WHERE product_id = p.id),0) AS avg_rating\n" +
                "FROM products p WHERE REPLACE(LOWER(p.name), ' ', '') LIKE REPLACE(LOWER('%$productSearched%'), ' ', '');"

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
                            val stock = productObject.get("stock").asInt
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
                                            val product = Product(id, name, description, price,width,height,length,stock,main_picture,avgRating,reviewsNumber,uploadDate)
                                            productList.add(product)
                                            loadedProducts++
                                            if(loadedProducts==productsArray.size())
                                                binding.recyclerView.adapter?.notifyDataSetChanged()
                                        }
                                    }
                                }
                                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                    Toast.makeText(requireContext(), "Failed request: " + t.message, Toast.LENGTH_LONG).show()
                                }
                            })
                        }
                    } else Toast.makeText(requireContext(), "Non è stato trovato nulla relativo al testo inserito", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed request: " + t.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun showSortOptions(anchorView: View,productList: ArrayList<Product>) {
        val popupMenu = PopupMenu(requireContext(), anchorView)
        popupMenu.menuInflater.inflate(R.menu.sort_options, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.lowest_price -> {
                    productList.sortBy { it.price }
                    binding.recyclerView.adapter?.notifyDataSetChanged()
                    true
                }
                R.id.highest_price -> {
                    productList.sortByDescending { it.price }
                    binding.recyclerView.adapter?.notifyDataSetChanged()
                    true
                }
                R.id.most_recent -> {
                    productList.sortByDescending { it.uploadDate }
                    binding.recyclerView.adapter?.notifyDataSetChanged()
                    true
                }
                R.id.least_recent -> {
                    productList.sortBy { it.uploadDate }
                    binding.recyclerView.adapter?.notifyDataSetChanged()
                    true
                }
                R.id.highest_rating -> {
                    productList.sortByDescending { it.avgRating }
                    binding.recyclerView.adapter?.notifyDataSetChanged()
                    true
                }
                R.id.lowest_rating -> {
                    productList.sortBy { it.avgRating }
                    binding.recyclerView.adapter?.notifyDataSetChanged()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }
}