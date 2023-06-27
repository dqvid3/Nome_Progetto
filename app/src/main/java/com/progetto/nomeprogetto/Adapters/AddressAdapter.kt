package com.progetto.nomeprogetto.Adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.progetto.nomeprogetto.Objects.UserAddress
import com.progetto.nomeprogetto.R
import com.progetto.nomeprogetto.databinding.AddressViewDesignBinding

class AddressAdapter(private var addressList: ArrayList<UserAddress>) : RecyclerView.Adapter<AddressAdapter.ViewHolder>() {

    class ViewHolder(binding: AddressViewDesignBinding) : RecyclerView.ViewHolder(binding.root) {
        val name = binding.name
        val addressLine1 = binding.addressLine1
        val addressLine2 = binding.addressLine2
        val city = binding.city
        val county = binding.county
        val cap = binding.cap
        val state = binding.state
        val selectButton = binding.selectButton
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = AddressViewDesignBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return addressList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val address = addressList[position]

        holder.name.text = address.name
        holder.addressLine1.text = address.address_line1
        if(address.address_line2.isBlank())
            holder.addressLine2.visibility = View.GONE
        else
            holder.addressLine2.text = " , " + address.address_line2
        holder.city.text = address.city
        holder.county.text = " , " + address.county
        holder.cap.text = address.cap
        holder.state.text = " , "  + address.state
        if (address.selected)
            holder.selectButton.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.redPart))
        else
            holder.selectButton.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.notSelected))
    }
}