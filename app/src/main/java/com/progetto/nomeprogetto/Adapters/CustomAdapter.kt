package com.progetto.nomeprogetto.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.progetto.nomeprogetto.Objects.ItemViewModel
import com.progetto.nomeprogetto.databinding.CardViewDesignBinding

class CustomAdapter(private var mList: List<ItemViewModel>) : RecyclerView.Adapter<CustomAdapter.ViewHolder>(){

    private var onClickListener: OnClickListener? = null

    class ViewHolder(binding: CardViewDesignBinding) : RecyclerView.ViewHolder(binding.root){
        val imageView = binding.imageView
        val textView = binding.textView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = CardViewDesignBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ItemViewModel = mList[position]

        holder.imageView.setImageResource(ItemViewModel.image)
        holder.textView.text = ItemViewModel.text

        holder.itemView.setOnClickListener{
            onClickListener?.onClick(position, ItemViewModel)
        }
    }

    interface OnClickListener{
        fun onClick(position: Int, model: ItemViewModel)
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }
}