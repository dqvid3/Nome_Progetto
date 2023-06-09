package com.progetto.nomeprogetto.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.progetto.nomeprogetto.Activities.BuyActivity
import com.progetto.nomeprogetto.ClientNetwork
import com.progetto.nomeprogetto.Fragments.MainActivity.Account.AddAddressFragment
import com.progetto.nomeprogetto.Objects.Product
import com.progetto.nomeprogetto.Objects.UserAddress
import com.progetto.nomeprogetto.R
import com.progetto.nomeprogetto.databinding.AddressViewDesignBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddressAdapter(private var addressList: ArrayList<UserAddress>) : RecyclerView.Adapter<AddressAdapter.ViewHolder>() {

    private var lastSelectedPosition = RecyclerView.NO_POSITION
    class ViewHolder(binding: AddressViewDesignBinding) : RecyclerView.ViewHolder(binding.root) {
        val name = binding.name
        val addressLine1 = binding.addressLine1
        val addressLine2 = binding.addressLine2
        val city = binding.city
        val county = binding.county
        val cap = binding.cap
        val state = binding.state
        val selectButton = binding.selectButton
        val layout = binding.layout
        val modifyAddress = binding.modifyAddress
        val removeAddress = binding.removeAddress
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

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val address = addressList[position]
        val context = holder.itemView.context

        holder.name.text = address.name
        holder.addressLine1.text = address.address_line1
        holder.addressLine2.text = address.address_line2
        holder.city.text = address.city
        holder.county.text = address.county
        holder.cap.text = address.cap
        holder.state.text = address.state
        if (address.selected) {
            updateCurrentAddress(context,address.id)
            holder.layout.setBackgroundColor(Color.parseColor("#D9EBFA"))
            lastSelectedPosition = position
            holder.selectButton.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.redPart))
        }else
            holder.selectButton.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.notSelected))
        holder.selectButton.setOnClickListener{
            if (lastSelectedPosition != RecyclerView.NO_POSITION) {
                addressList[lastSelectedPosition].selected = false
                notifyItemChanged(lastSelectedPosition)
            }
            address.selected = true
            notifyItemChanged(position)
            lastSelectedPosition = position
            updateCurrentAddress(context, address.id)
        }

        holder.itemView.setOnClickListener{
            holder.selectButton.performClick()
        }

        holder.modifyAddress.setOnClickListener{
            val fragment = AddAddressFragment()
            val bundle = Bundle()
            bundle.putParcelable("address", address)
            fragment.arguments = bundle
            val activity = context as? BuyActivity
            if (activity != null) {
                activity.supportFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit()
                activity.findViewById<FrameLayout>(R.id.fragment_container)
                    .visibility = View.VISIBLE
            }
        }

        holder.removeAddress.setOnClickListener{
            if (addressList.size>1)
                removeAddress(context,address.id)
            else
                Toast.makeText(context, "Non puoi eliminare l'indirizzo se ne possiedi solo uno", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateCurrentAddress(context: Context,addressId: Int){
        val sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("ID", 0)
        var query = "UPDATE users SET current_address_id = $addressId WHERE id=$userId;"

        ClientNetwork.retrofit.update(query).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {}
            override fun onFailure(call: Call<JsonObject>, t: Throwable) =
                Toast.makeText(context, "Failed request: " + t.message, Toast.LENGTH_LONG).show()
        })
    }

    private fun removeAddress(context: Context,addressId: Int){
        var query = "DELETE FROM user_addresses WHERE id=$addressId;"

        ClientNetwork.retrofit.remove(query).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    val index = addressList.indexOfFirst { it.id == addressId }
                    if (index != -1) {
                        val wasSelected = addressList[index].selected
                        addressList.removeAt(index)
                        notifyItemRemoved(index)

                        if (wasSelected) {
                            lastSelectedPosition = RecyclerView.NO_POSITION
                            if (addressList.isNotEmpty()) {
                                addressList[0].selected = true
                                lastSelectedPosition = 0
                                updateCurrentAddress(context, addressList[0].id)
                                notifyItemChanged(0)
                            }else  updateCurrentAddress(context,-1)
                        }
                    }
                } else
                    Toast.makeText(context, "Failed to remove address", Toast.LENGTH_LONG).show()
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) =
                Toast.makeText(context, "Failed request: " + t.message, Toast.LENGTH_LONG).show()
        })
    }
}