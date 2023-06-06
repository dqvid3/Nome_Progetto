package com.progetto.nomeprogetto.Fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.google.gson.JsonObject
import com.progetto.nomeprogetto.Adapters.ProductImageAdapter
import com.progetto.nomeprogetto.ClientNetwork
import com.progetto.nomeprogetto.Product
import com.progetto.nomeprogetto.R
import com.progetto.nomeprogetto.databinding.FragmentProductDetailBinding
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductDetailFragment : Fragment() {

    private lateinit var binding: FragmentProductDetailBinding
    private var imageSelected: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProductDetailBinding.inflate(inflater)

        val product : Product? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("product", Product::class.java)
        } else
            arguments?.getParcelable("product")

        val imageList = ArrayList<Bitmap>()
        product?.main_picture?.let { imageList.add(it) }
        setImages(product?.id,imageList)

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.recyclerView)
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.setOnFlingListener(null)
        layoutManager.isSmoothScrollbarEnabled = true

        val adapter = ProductImageAdapter(imageList)
        binding.recyclerView.adapter = adapter

        adapter.setOnClickListener(object: ProductImageAdapter.OnClickListener{
            override fun onClick() {
                val layoutParams = binding.recyclerView.layoutParams
                if(!imageSelected) {
                    layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                }else{
                    layoutParams.height = resources.getDimensionPixelSize(R.dimen.recycler_view_height)
                    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                }
                imageSelected = !imageSelected
                binding.recyclerView.layoutParams = layoutParams
            }
        })

        binding.productName.text = product?.name
        binding.productDescription.text = product?.description

        binding.backButton.setOnClickListener{
            parentFragmentManager.findFragmentByTag("HomeFragment")?.let { it ->
                parentFragmentManager.beginTransaction()
                    .remove(this)
                    .show(it)
                    .commit()
            }
        }

        return binding.root
    }

    private fun setImages(productId: Int?,imageList: ArrayList<Bitmap>){
        val query = "SELECT picture_path FROM product_pictures WHERE product_id = $productId;"

        ClientNetwork.retrofit.select(query).enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    if (response.isSuccessful) {
                        val picturesArray = response.body()?.getAsJsonArray("queryset")
                        if (picturesArray != null && picturesArray.size() > 0) {
                            for (i in 0 until picturesArray.size()) {
                                val pictureObject = picturesArray[i].asJsonObject
                                val picture_path = pictureObject.get("picture_path").asString

                                ClientNetwork.retrofit.image(picture_path).enqueue(object : Callback<ResponseBody> {
                                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                            if(response.isSuccessful) {
                                                if (response.body()!=null) {
                                                    val picture = BitmapFactory.decodeStream(response.body()?.byteStream())
                                                    imageList.add(picture)
                                                    if(i==picturesArray.size()-1) {
                                                        binding.recyclerView.adapter?.notifyDataSetChanged()
                                                    }
                                                }
                                            }
                                        }
                                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
                                    })
                            }
                        }
                    }
                }
                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Toast.makeText(requireContext(), "Failed on product request: " + t.message, Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}