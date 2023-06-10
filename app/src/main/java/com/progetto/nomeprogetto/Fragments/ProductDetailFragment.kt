package com.progetto.nomeprogetto.Fragments

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.google.gson.JsonObject
import com.progetto.nomeprogetto.Adapters.ProductImageAdapter
import com.progetto.nomeprogetto.Adapters.ProductReviewAdapter
import com.progetto.nomeprogetto.ClientNetwork
import com.progetto.nomeprogetto.Objects.Product
import com.progetto.nomeprogetto.Objects.ProductReview
import com.progetto.nomeprogetto.R
import com.progetto.nomeprogetto.databinding.FragmentProductDetailBinding
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.recyclerImageView)
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerImageView.layoutManager = layoutManager
        binding.recyclerImageView.setOnFlingListener(null)
        layoutManager.isSmoothScrollbarEnabled = true

        val imageList = HashMap<Int,Bitmap>()
        product?.main_picture?.let { imageList.put(0,it) }
        setImages(product?.id,imageList)
        val imageAdapter = ProductImageAdapter(imageList)
        binding.recyclerImageView.adapter = imageAdapter
        imageAdapter.setOnClickListener(object: ProductImageAdapter.OnClickListener{
            override fun onClick() {
                val layoutParams = binding.recyclerImageView.layoutParams
                if(!imageSelected) {
                    layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                }else{
                    layoutParams.height = resources.getDimensionPixelSize(R.dimen.recycler_view_height)
                    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                }
                imageSelected = !imageSelected
                binding.recyclerImageView.layoutParams = layoutParams
            }
        })

        binding.recyclerReviewsView.layoutManager = LinearLayoutManager(requireContext())
        val reviewList = ArrayList<ProductReview>()
        setReviews(product?.id,reviewList)
        val reviewAdapter = ProductReviewAdapter(reviewList)
        binding.recyclerReviewsView.adapter = reviewAdapter

        val quantityOptions = (1..product!!.stock).toList()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, quantityOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerQty.adapter = adapter

        binding.backButton.setOnClickListener{
            parentFragmentManager.findFragmentByTag("HomeFragment")?.let { it ->
                parentFragmentManager.beginTransaction()
                    .remove(this)
                    .show(it)
                    .commit()
            }
        }

        binding.productName.text = product.name
        binding.productDescription.text = product.description
        binding.productPrice.text = product.price.toString() + " €"

        return binding.root
    }

    private fun setImages(productId: Int?,imageList: HashMap<Int,Bitmap>){
        val query = "SELECT picture_path,picture_index FROM product_pictures WHERE product_id = $productId;"

        ClientNetwork.retrofit.select(query).enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    if (response.isSuccessful) {
                        val picturesArray = response.body()?.getAsJsonArray("queryset")
                        if (picturesArray != null && picturesArray.size() > 0) {
                            var loadedImages = 0
                            for (i in 0 until picturesArray.size()) {
                                val pictureObject = picturesArray[i].asJsonObject
                                val picture_path = pictureObject.get("picture_path").asString
                                val pictureIndex = pictureObject.get("picture_index").asInt
                                ClientNetwork.retrofit.image(picture_path).enqueue(object : Callback<ResponseBody> {
                                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                            if(response.isSuccessful) {
                                                if (response.body()!=null) {
                                                    val picture = BitmapFactory.decodeStream(response.body()?.byteStream())
                                                    imageList.put(pictureIndex,picture)
                                                    loadedImages++
                                                    if (loadedImages == picturesArray.size())
                                                        binding.recyclerImageView.adapter?.notifyDataSetChanged()
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

    private fun setReviews(productId: Int?, reviewList: ArrayList<ProductReview>){
        val query = "SELECT pr.rating, pr.comment, pr.review_date, u.username\n" +
                "FROM product_reviews pr, users u\n" +
                "WHERE pr.user_id = u.id and pr.product_id = $productId;"

        ClientNetwork.retrofit.select(query).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    val reviewsArray = response.body()?.getAsJsonArray("queryset")
                    if (reviewsArray != null && reviewsArray.size() > 0) {
                        var loadedReviews = 0
                        for (i in 0 until reviewsArray.size()) {
                            val reviewObject = reviewsArray[i].asJsonObject
                            val username = reviewObject.get("username").asString
                            val rating = reviewObject.get("rating").asInt
                            val comment = reviewObject.get("comment").asString
                            val date = reviewObject.get("review_date").asString
                            val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                            val reviewDate = LocalDateTime.parse(date, formatter)
                            val productReview = ProductReview(username,rating,comment,reviewDate)
                            reviewList.add(productReview)
                            loadedReviews++
                            if(loadedReviews==reviewsArray.size())
                                binding.recyclerReviewsView.adapter?.notifyDataSetChanged()
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