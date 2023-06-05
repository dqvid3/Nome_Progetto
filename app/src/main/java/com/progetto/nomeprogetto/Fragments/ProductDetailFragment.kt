package com.progetto.nomeprogetto.Fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.gson.JsonObject
import com.progetto.nomeprogetto.Adapters.ImageSliderAdapter
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProductDetailBinding.inflate(inflater)

        val product : Product? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("product", Product::class.java)
        } else
            arguments?.getParcelable("product")

        val images = ArrayList<Bitmap>().apply {
            product?.main_picture?.let { add(it) }
        }

        val adapter = ImageSliderAdapter(requireContext(), images)
        binding.productImages.adapter = adapter
        setImages(product?.id,adapter,images)

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

    private fun setImages(productId: Int?, adapter: ImageSliderAdapter, images: ArrayList<Bitmap>){
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
                                                    images.add(picture)
                                                    if(i==picturesArray.size()-1)
                                                        adapter.updateImages(images)
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