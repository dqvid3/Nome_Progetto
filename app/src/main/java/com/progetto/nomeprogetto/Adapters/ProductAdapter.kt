package com.progetto.nomeprogetto.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.progetto.nomeprogetto.Objects.Product
import com.progetto.nomeprogetto.databinding.ProductViewDesignBinding

class ProductAdapter(private var productList: List<Product>) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    class ViewHolder(binding: ProductViewDesignBinding) : RecyclerView.ViewHolder(binding.root) {
        val imageView = binding.imageView
        val productName = binding.productName
        val avgRating = binding.avgRating
        val ratingBar = binding.ratingBar
        val price = binding.price
        val reviewsNumber = binding.reviewsNumber
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ProductViewDesignBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]

        holder.imageView.setImageBitmap(product.main_picture)
        holder.productName.text = product.name
        holder.avgRating.text = product.avgRating.toString()
        holder.ratingBar.rating = product.avgRating.toFloat()
        holder.price.text = product.price.toString() + " €"
        holder.reviewsNumber.text = "(" + product.reviewsNumber.toString() + ")"

        holder.itemView.setOnClickListener {
            onClickListener?.onClick(product)
        }
    }

    interface OnClickListener {
        fun onClick(product: Product)
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }
}