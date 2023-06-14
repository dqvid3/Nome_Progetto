package com.progetto.nomeprogetto.Adapters

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.progetto.nomeprogetto.Objects.ProductColor
import com.progetto.nomeprogetto.databinding.ProductColorViewDesignBinding

class ProductColorAdapter(private var colorList: ArrayList<ProductColor>) : RecyclerView.Adapter<ProductColorAdapter.ViewHolder>() {

    private var colorSelected: Int = -1
    private var positionSelected: Int = 0
    private var onClickListener: OnClickListener? = null

    fun setColor(colorSelected: Int){
        this.colorSelected = colorSelected
    }

    fun getPosition(): Int{
        return positionSelected
    }

    class ViewHolder(binding: ProductColorViewDesignBinding) : RecyclerView.ViewHolder(binding.root) {
        val colorView = binding.colorView
        val colorName = binding.colorName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ProductColorViewDesignBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(view)
    }
//
    override fun getItemCount(): Int {
        return colorList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val color = colorList.get(position)
        if (position==0 && colorSelected==-1)  colorSelected = color.color_id
        holder.colorName.text = color.name
        holder.colorView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(color.hex))
        if(color.color_id == colorSelected) {
            positionSelected = position
            holder.colorView.strokeColor = Color.BLUE
        }else
            holder.colorView.strokeColor = Color.TRANSPARENT

        holder.itemView.setOnClickListener {
            onClickListener?.onClick(position)
        }
    }

    interface OnClickListener {
        fun onClick(position: Int)
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }
}