package com.progetto.nomeprogetto.Fragments.MainActivity

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.tabs.TabLayout
import com.google.gson.JsonObject
import com.progetto.nomeprogetto.Adapters.CartAdapter
import com.progetto.nomeprogetto.Adapters.CartAdapterListener
import com.progetto.nomeprogetto.Adapters.ProductAdapter
import com.progetto.nomeprogetto.ClientNetwork
import com.progetto.nomeprogetto.Fragments.MainActivity.Home.ProductDetailFragment
import com.progetto.nomeprogetto.Objects.Product
import com.progetto.nomeprogetto.R
import com.progetto.nomeprogetto.databinding.FragmentCartBinding
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CartFragment : Fragment(), CartAdapterListener {

    override fun restoreCart() {
        binding.emptyCart.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
    }

    private lateinit var binding: FragmentCartBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCartBinding.inflate(inflater)
        loadCart()
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position
                if(position==0){ // 0 -> Cart
                    loadCart()
                }else if(position==1){ // 1 -> Last orders
                    loadLastOrders()
                }
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
        })

        return binding.root
    }

    private fun loadCart(){
        val sharedPref = requireActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("ID", 0)
        val productList = ArrayList<Product>()
        val adapter = CartAdapter(productList,this)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val itemDecoration = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        binding.recyclerView.addItemDecoration(itemDecoration)
        binding.recyclerView.adapter = adapter
        setCartProducts(userId,productList)

        adapter.setOnClickListener(object: CartAdapter.OnClickListener{
            override fun onClick(product: Product) {
                val bundle = Bundle()
                bundle.putParcelable("product", product)
                val productDetailFragment = ProductDetailFragment()
                productDetailFragment.arguments = bundle
                parentFragmentManager.beginTransaction().hide(this@CartFragment)
                    .add(R.id.home_fragment_container,productDetailFragment)
                    .commit()
            }
        })
    }

    private fun setCartProducts(userId: Int, productList: ArrayList<Product>){
        var query = "SELECT ci.id as itemId,ci.quantity,pc.stock,pc.color,pc.color_hex,p.id,name,description,price,width,height" +
                ",length,main_picture_path,upload_date, " +
                "IFNULL((SELECT COUNT(*) FROM product_reviews WHERE product_id = p.id),0) AS review_count, " +
                "IFNULL((SELECT AVG(rating) FROM product_reviews WHERE product_id = p.id),0) AS avg_rating " +
                "FROM products p, cart_items ci,product_colors pc WHERE ci.user_id=$userId and pc.id = ci.color_id;"

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
                            val stock = productObject.get("stock").asInt
                            val quantity = productObject.get("quantity").asInt
                            val color = productObject.get("color").asString
                            val itemId = productObject.get("itemId").asInt
                            val color_hex = productObject.get("color_hex").asString
                            ClientNetwork.retrofit.image(main_picture_path).enqueue(object : Callback<ResponseBody> {
                                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                    if(response.isSuccessful) {
                                        if (response.body()!=null) {
                                            val main_picture = BitmapFactory.decodeStream(response.body()?.byteStream())
                                            val product = Product(id, name, description, price,width,height,length,main_picture
                                                ,avgRating,reviewsNumber,uploadDate,itemId,color,color_hex,quantity,stock)
                                            productList.add(product)
                                            loadedProducts++
                                            if(loadedProducts==productsArray.size()) {
                                                binding.emptyCart.visibility = View.GONE
                                                binding.recyclerView.visibility = View.VISIBLE
                                                binding.recyclerView.adapter?.notifyDataSetChanged()
                                            }
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

    private fun loadLastOrders(){

    }
}