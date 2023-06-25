package com.progetto.nomeprogetto.Fragments.MainActivity

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
import com.google.android.material.tabs.TabLayout
import com.google.gson.JsonObject
import com.progetto.nomeprogetto.Adapters.CartAdapter
import com.progetto.nomeprogetto.Adapters.CartAdapterListener
import com.progetto.nomeprogetto.Adapters.WishlistAdapter
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
    ): View {
        binding = FragmentCartBinding.inflate(inflater)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val itemDecoration = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        binding.recyclerView.addItemDecoration(itemDecoration)
        loadCart()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position
                if(position==0){ // 0 -> Cart
                    binding.emptyCart.text = "Non hai articoli nel carrello"
                    loadCart()
                }else if(position==1){ // 1 -> WishList
                    binding.emptyCart.text = "Non hai articoli nella wishlist"
                    loadWishList()
                }
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
        })

        return binding.root
    }

    private fun loadCart(){
        binding.emptyCart.text = "Non hai articoli nel carrello"
        val sharedPref = requireActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("ID", 0)
        val productList = ArrayList<Product>()
        val adapter = CartAdapter(productList,this,userId)

        binding.recyclerView.adapter = adapter
        setProducts(userId,productList,0)

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

    private fun loadWishList(){
        binding.emptyCart.text = "Non hai articoli nella wishlist"
        val sharedPref = requireActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("ID", 0)
        val productList = ArrayList<Product>()
        val adapter = WishlistAdapter(productList,this,userId)

        binding.recyclerView.adapter = adapter
        setProducts(userId,productList,1)

        adapter.setOnClickListener(object: WishlistAdapter.OnClickListener{
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

    private fun setProducts(userId: Int, productList: ArrayList<Product>,type: Int){
        productList.clear()
        val query: String
        if(type==0) //cart
            query = "SELECT ci.id as itemId,ci.quantity,pc.stock,pc.color,pc.color_hex,p.id,name,description,price," +
                "width,height,length,main_picture_path,upload_date,pp.picture_path,ci.color_id," +
                "IFNULL((SELECT COUNT(*) FROM product_reviews WHERE product_id = p.id),0) AS review_count, " +
                "IFNULL((SELECT AVG(rating) FROM product_reviews WHERE product_id = p.id),0) AS avg_rating " +
                "FROM products p, cart_items ci,product_colors pc,product_pictures pp WHERE ci.user_id=$userId " +
                "and pc.id = ci.color_id and pp.picture_index=0 and pp.color_id=pc.id and p.id = pp.product_id;"
        else query = "SELECT wi.id as itemId,wi.user_id,pc.color,pc.color_hex,pp.picture_path,p.id,name,description,price,width,height," +
                "length,main_picture_path,upload_date,wi.color_id, " +
                "IFNULL((SELECT COUNT(*) FROM product_reviews WHERE product_id = p.id),0) AS review_count," +
                "IFNULL((SELECT AVG(rating) FROM product_reviews WHERE product_id = p.id),0) AS avg_rating " +
                "FROM products p, wishlist_items wi,product_colors pc,product_pictures pp WHERE wi.user_id=$userId " +
                "and pc.id = wi.color_id and pp.picture_index=0 and pp.color_id=pc.id and p.id = pp.product_id;"

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
                            val color = productObject.get("color").asString
                            val itemId = productObject.get("itemId").asInt
                            val color_hex = productObject.get("color_hex").asString
                            val picture_path = productObject.get("picture_path").asString
                            val colorId = productObject.get("color_id").asInt
                            var stock: Int? = null
                            var quantity: Int? = null
                            if(type == 0){
                                stock = productObject.get("stock").asInt
                                quantity = productObject.get("quantity").asInt
                            }
                            var main_picture : Bitmap? = null
                            var picture : Bitmap? = null
                            for(j in 0..1) {
                                ClientNetwork.retrofit.image(if (j==0) main_picture_path else picture_path).enqueue(object : Callback<ResponseBody> {
                                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                            if (response.isSuccessful) {
                                                if (response.body() != null) {
                                                    if(j==0) main_picture = BitmapFactory.decodeStream(response.body()?.byteStream())
                                                    else picture = BitmapFactory.decodeStream(response.body()?.byteStream())
                                                    if(j==1) {
                                                        val product = Product(id, name, description, price, width, height,
                                                            length, main_picture, avgRating, reviewsNumber, uploadDate,
                                                            itemId, color, color_hex, quantity, stock, picture,colorId)
                                                        productList.add(product)
                                                        loadedProducts++
                                                        if (loadedProducts == productsArray.size()) {
                                                            binding.emptyCart.visibility = View.GONE
                                                            binding.recyclerView.visibility = View.VISIBLE
                                                            binding.recyclerView.adapter?.notifyDataSetChanged()
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) =
                                            Toast.makeText(requireContext(), "Failed request: " + t.message, Toast.LENGTH_LONG).show()
                                    })
                            }
                        }
                    }else restoreCart()
                }
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) =
                Toast.makeText(requireContext(), "Failed request: " + t.message, Toast.LENGTH_LONG).show()
        })
    }
}