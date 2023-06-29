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
import com.google.android.material.chip.Chip
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
        arguments?.getString("searchQuery")?.let { setProducts(it,productList,0) } // 0 query normale
        arguments?.getString("category_name")?.let { setProducts(it,productList,1) } // 1 query per categoria

        setProductAdapter(productList)

        binding.sortButton.setOnClickListener{
            showSortOptions(binding.sortButton,productList)
        }

        binding.backButton.setOnClickListener{
            parentFragmentManager.beginTransaction()
                .replace(R.id.home_fragment_home_container,HomeFragment())
                .commit()
        }

        binding.chipGroup.setOnCheckedStateChangeListener { chipGroup, _ ->
            val selectedCategories = mutableListOf<String>()
            for (i in 0 until chipGroup.childCount) {
                val chip = chipGroup.getChildAt(i) as Chip
                if (chip.isChecked) {
                    selectedCategories.add(chip.text.toString())
                }
            }
            filterProductsByCategory(selectedCategories,productList)
        }

        return binding.root
    }

    private fun setProductAdapter(productList: ArrayList<Product>){
        val adapter = ProductAdapter(productList)
        binding.recyclerView.adapter = adapter
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

    }
    private fun filterProductsByCategory(selectedCategories: List<String>,productList: ArrayList<Product>) {
        val filteredList = if (selectedCategories.isEmpty())
            productList
        else
            productList.filter { selectedCategories.contains(it.category) }
        setProductAdapter(filteredList as ArrayList<Product>)
    }

    private fun setProducts(productSearched: String, productList: ArrayList<Product>,searchType: Int){
        val query: String
        if(searchType==0)
            query = "SELECT c.name as category,p.id,p.name,description,price,width,height,length,main_picture_path,upload_date," +
                    "IF NULL((SELECT COUNT(*) FROM product_reviews WHERE product_id = p.id),0) AS review_count," +
                    "IF NULL((SELECT AVG(rating) FROM product_reviews WHERE product_id = p.id),0) AS avg_rating " +
                    "FROM products p,categories c WHERE c.id=p.category_id and LOWER(p.name) LIKE LOWER('%$productSearched%');"
        else query = "SELECT p.id,p.name,p.description,price,width,height,length,main_picture_path,upload_date," +
                "IF NULL((SELECT COUNT(*) FROM product_reviews WHERE product_id = p.id),0) AS review_count," +
                "IF NULL((SELECT AVG(rating) FROM product_reviews WHERE product_id = p.id),0) AS avg_rating " +
                "FROM products p,categories c WHERE p.category_id = c.id and c.name='$productSearched';"
        val context = requireContext()

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
                            var category: String? =
                                if(searchType==0)
                                    productObject.get("category").asString
                                else
                                    null
                            ClientNetwork.retrofit.image(main_picture_path).enqueue(object : Callback<ResponseBody> {
                                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                    if(response.isSuccessful) {
                                        if (response.body()!=null) {
                                            val main_picture = BitmapFactory.decodeStream(response.body()?.byteStream())
                                            val product = Product(id, name, description, price,width,height,length,main_picture
                                                ,avgRating,reviewsNumber,uploadDate, category = category)
                                            productList.add(product)
                                            loadedProducts++
                                            if(loadedProducts==productsArray.size()) {
                                                binding.recyclerView.adapter?.notifyDataSetChanged()
                                                for(p in productList) {
                                                    if (p.category != null) {
                                                        val chip = Chip(context)
                                                        chip.text = p.category
                                                        chip.isCheckable = true
                                                        binding.chipGroup.addView(chip)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                    Toast.makeText(context, "Failed request: " + t.message, Toast.LENGTH_LONG).show()
                                }
                            })
                        }
                    } else{
                        if(searchType==0)
                            Toast.makeText(context, "Non è stato trovato nulla relativo al testo inserito", Toast.LENGTH_LONG).show()
                        else
                            Toast.makeText(context, "Non è stato trovato nulla relativo alla categoria selezionata", Toast.LENGTH_LONG).show()
                    }
                }
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(context, "Failed request: " + t.message, Toast.LENGTH_LONG).show()
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