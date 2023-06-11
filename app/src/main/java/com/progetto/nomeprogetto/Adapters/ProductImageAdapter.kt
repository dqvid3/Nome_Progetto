package com.progetto.nomeprogetto.Adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.progetto.nomeprogetto.databinding.ImageViewDesignBinding

class ProductImageAdapter(private var imageList: HashMap<Int,Bitmap>) : RecyclerView.Adapter<ProductImageAdapter.ViewHolder>() {

    private var onClickListener: OnClickListener? = null
    private var imageWidth : Int = ViewGroup.LayoutParams.MATCH_PARENT
    constructor(imageList: HashMap<Int, Bitmap>, imageWidth: Int) : this(imageList) {
        this.imageWidth = imageWidth
    }

    class ViewHolder(binding: ImageViewDesignBinding) : RecyclerView.ViewHolder(binding.root) {
        val imageView = binding.imageView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ImageViewDesignBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val image = imageList[position]

        holder.imageView.setImageBitmap(image)
        holder.imageView.layoutParams.width = imageWidth

        holder.itemView.setOnClickListener {
            onClickListener?.onClick()
        }
    }

    interface OnClickListener {
        fun onClick()
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }
}