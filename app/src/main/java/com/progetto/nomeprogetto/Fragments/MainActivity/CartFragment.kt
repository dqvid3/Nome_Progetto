package com.progetto.nomeprogetto.Fragments.MainActivity

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.tabs.TabLayout
import com.google.gson.JsonObject
import com.progetto.nomeprogetto.Adapters.CustomAdapter
import com.progetto.nomeprogetto.Adapters.ProductAdapter
import com.progetto.nomeprogetto.ClientNetwork
import com.progetto.nomeprogetto.Fragments.MainActivity.Home.ProductDetailFragment
import com.progetto.nomeprogetto.Objects.ItemViewModel
import com.progetto.nomeprogetto.Objects.Product
import com.progetto.nomeprogetto.R
import com.progetto.nomeprogetto.databinding.FragmentCartBinding
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CartFragment : Fragment() {

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

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Handle tab reselect
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Handle tab unselect
            }
        })

        return binding.root
    }

    private fun loadCart(){
        val sharedPref = requireActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val _id = sharedPref.getInt("ID", 0)

        val itemDecoration = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        binding.recyclerView.addItemDecoration(itemDecoration)
        val productList = ArrayList<Product>()
        val adapter = ProductAdapter(productList)
        binding.recyclerView.adapter = adapter
        setCartProducts(_id,productList)

        adapter.setOnClickListener(object: ProductAdapter.OnClickListener{
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

    private fun setCartProducts(cartId: Int, productList: ArrayList<Product>){
        val query = "SELECT product_id,quantity,color,price FROM cart_items WHERE cart_id = $cartId;"

        ClientNetwork.retrofit.select(query).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    var loadedProducts = 0
                    val productsArray = response.body()?.getAsJsonArray("queryset")
                    if (productsArray != null && productsArray.size() > 0) {
                        for (i in 0 until productsArray.size()) {
                            val productObject = productsArray[i].asJsonObject
                            val id = productObject.get("product_id").asInt

                        }
                    } else Toast.makeText(requireContext(), "Non è stato trovato nulla relativo al testo inserito", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed request: " + t.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun loadLastOrders(){
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val data = ArrayList<ItemViewModel>()

        for(i in 16..25){
            data.add(ItemViewModel(R.drawable.ic_launcher_foreground,"item " + i))
        }
        val adapter = CustomAdapter(data)
        binding.recyclerView.adapter = adapter

        adapter.setOnClickListener(object: CustomAdapter.OnClickListener {
            override fun onClick(position: Int, model: ItemViewModel) {

            }
        })
    }
}