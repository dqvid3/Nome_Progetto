package com.progetto.nomeprogetto.Fragments.MainActivity.Home

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
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.gson.JsonObject
import com.progetto.nomeprogetto.Adapters.ProductColorAdapter
import com.progetto.nomeprogetto.Adapters.ProductImageAdapter
import com.progetto.nomeprogetto.Adapters.ProductReviewAdapter
import com.progetto.nomeprogetto.ClientNetwork
import com.progetto.nomeprogetto.Objects.Product
import com.progetto.nomeprogetto.Objects.ProductColor
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
    private var colorSelected: Int = -1
    private var qty: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProductDetailBinding.inflate(inflater)

        val product: Product? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("product", Product::class.java)
        } else
            arguments?.getParcelable("product")
        val colorList = ArrayList<ProductColor>()
        val imageList = HashMap<Int, Bitmap>()
        val reviewList = ArrayList<ProductReview>()
        val colorAdapter = ProductColorAdapter(colorList)
        val imageAdapter = ProductImageAdapter(imageList)
        val reviewAdapter = ProductReviewAdapter(reviewList)
        binding.recyclerColorView.adapter = colorAdapter
        binding.recyclerImageView.adapter = imageAdapter
        binding.recyclerReviewsView.adapter = reviewAdapter

        //lista immagini
        LinearSnapHelper().attachToRecyclerView(binding.recyclerImageView)
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerImageView.layoutManager = layoutManager
        binding.recyclerImageView.setOnFlingListener(null)
        layoutManager.isSmoothScrollbarEnabled = true

        //lista colori
        binding.recyclerColorView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        setColors(product?.id, colorList, imageList)

        //lista recensioni
        binding.recyclerReviewsView.layoutManager = LinearLayoutManager(requireContext())
        val itemDecoration = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        binding.recyclerReviewsView.addItemDecoration(itemDecoration)
        setReviews(product?.id, reviewList)

        imageAdapter.setOnClickListener(object : ProductImageAdapter.OnClickListener {
            override fun onClick() {
                val layoutParams = binding.recyclerImageView.layoutParams
                if (!imageSelected) {
                    layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                } else {
                    layoutParams.height = resources.getDimensionPixelSize(R.dimen.recycler_view_height)
                    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                }
                imageSelected = !imageSelected
                binding.recyclerImageView.layoutParams = layoutParams
            }
        })

        colorAdapter.setOnClickListener(object : ProductColorAdapter.OnClickListener {
            override fun onClick(position: Int) {
                val prevPos = colorAdapter.getPosition()
                colorAdapter.setColor(colorList.get(position).color_id)
                colorAdapter.notifyItemChanged(position)
                colorAdapter.notifyItemChanged(prevPos)
                colorSelected = colorList.get(position).color_id
                setImages(product?.id, imageList, colorSelected)
            }
        })

        val quantityOptions = (1..qty).toList()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, quantityOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerQty.adapter = adapter

        binding.backButton.setOnClickListener {
            val productFragment = parentFragmentManager.findFragmentByTag("ProductFragment")
            if (productFragment != null)
                parentFragmentManager.beginTransaction().remove(this).show(productFragment)
                    .commit()
            else // sto aprendo il prodotto dalle novità
                parentFragmentManager.beginTransaction().replace(R.id.home_fragment_home_container, HomeFragment())
                    .commit()
        }

        binding.addToCart.setOnClickListener {
            println(binding.spinnerQty.selectedItem.toString())
        }

        binding.productName.text = product?.name
        binding.productDescription.text = product?.description
        binding.productPrice.text = product?.price.toString() + " €"

        return binding.root
    }

    private fun setColors(productId: Int?,colorList: ArrayList<ProductColor>,imageList: HashMap<Int, Bitmap>){
        val query = "SELECT id,pc.color AS color_name,stock, pc.color_hex AS color_hex FROM product_colors pc " +
                "WHERE pc.product_id = $productId;"
        ClientNetwork.retrofit.select(query).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    val colorsArray = response.body()?.getAsJsonArray("queryset")
                    if (colorsArray != null && colorsArray.size() > 0) {
                        binding.recyclerColorView.visibility = View.VISIBLE
                        var loadedColors = 0
                        for (i in 0 until colorsArray.size()) {
                            val colorObject = colorsArray[i].asJsonObject
                            val color_id = colorObject.get("id").asInt
                            val colorName = colorObject.get("color_name").asString
                            val colorHex = colorObject.get("color_hex").asString
                            val stock = colorObject.get("stock").asInt
                            colorList.add(ProductColor(colorName,color_id,colorHex,stock))
                            if(i==0){
                                setImages(productId, imageList,color_id)
                                qty = stock
                            }//
                            loadedColors++
                            if(loadedColors==colorsArray.size())
                                binding.recyclerColorView.adapter?.notifyDataSetChanged()
                        }
                    }
                }
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed request: " + t.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setImages(productId: Int?, imageList: HashMap<Int, Bitmap>, colorSelected: Int) {
        val query = "SELECT picture_path,picture_index FROM product_pictures WHERE product_id = $productId" +
                    " and color_id=$colorSelected;"

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
                                    override fun onResponse(
                                        call: Call<ResponseBody>,
                                        response: Response<ResponseBody>
                                    ) {
                                        if (response.isSuccessful) {
                                            if (response.body() != null) {
                                                val picture = BitmapFactory.decodeStream(
                                                    response.body()?.byteStream()
                                                )
                                                imageList[pictureIndex] = picture
                                                loadedImages++
                                                if (loadedImages == picturesArray.size())
                                                    binding.recyclerImageView.adapter?.notifyDataSetChanged()
                                            }
                                        }
                                    }

                                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                        Toast.makeText(
                                            requireContext(),
                                            "Failed request: " + t.message,
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                })
                        }
                    }
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed request: " + t.message, Toast.LENGTH_LONG)
                    .show()
            }
        })
    }

    private fun setReviews(productId: Int?, reviewList: ArrayList<ProductReview>) {
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
                            val productReview = ProductReview(username, rating, comment, reviewDate)
                            reviewList.add(productReview)
                            loadedReviews++
                            if (loadedReviews == reviewsArray.size())
                                binding.recyclerReviewsView.adapter?.notifyDataSetChanged()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed request: " + t.message, Toast.LENGTH_LONG)
                    .show()
            }
        })
    }
}