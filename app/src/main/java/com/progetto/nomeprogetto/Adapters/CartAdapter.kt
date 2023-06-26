package com.progetto.nomeprogetto.Adapters

import android.R
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.progetto.nomeprogetto.ClientNetwork
import com.progetto.nomeprogetto.Objects.Product
import com.progetto.nomeprogetto.databinding.CartViewDesignBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CartAdapter(private val productList: List<Product>,private val listener: CartAdapterListener,private val userId: Int?) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    class ViewHolder(binding: CartViewDesignBinding) : RecyclerView.ViewHolder(binding.root) {
        val imageView = binding.imageView
        val productName = binding.productName
        val price = binding.price
        val spinnerQty = binding.spinnerQty
        val colorName = binding.colorName
        val colorView = binding.colorView
        val removeProduct = binding.removeProduct
        val addToWish = binding.addToWish
        val qtyLayout = binding.qtyLayout
        val noStock = binding.noStock
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = CartViewDesignBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]

        holder.imageView.setImageBitmap(product.picture)
        holder.productName.text = product.name
        holder.price.text = product.price.toString() + " €"
        holder.colorName.text = product.colorName
        holder.colorView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(product.color_hex))

        if (product.stock==0){
            holder.qtyLayout.visibility = View.GONE
            holder.noStock.visibility = View.VISIBLE
        }
        val quantityOptions = (1..product.stock!!).toList()
        val adapter = ArrayAdapter(holder.itemView.context, R.layout.simple_spinner_item, quantityOptions)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        holder.spinnerQty.adapter = adapter
        holder.spinnerQty.setSelection(product.quantity!!-1)
        holder.spinnerQty.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            var isInitialSelection = true
            override fun onItemSelected(parent: AdapterView<*>, view: View?, p: Int, id: Long) {
                if(isInitialSelection){
                    isInitialSelection = false
                    return
                }
                val selectedQuantity = quantityOptions[p]
                updateCart(position,selectedQuantity,holder.itemView.context)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        holder.removeProduct.setOnClickListener{
            product.itemId?.let { removeFromCart(it,position,holder.itemView.context) }
        }

        holder.addToWish.setOnClickListener{
            product.colorId?.let { addToWish(it,holder.itemView.context) }
        }

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

    private fun removeItem(position: Int) {
        productList.toMutableList().apply { removeAt(position) }
        notifyItemRemoved(position)
        if (productList.isEmpty())
            listener.restoreCart()
        else
            notifyItemRangeChanged(position, productList.size)
    }

    private fun updateCart(position: Int,quantity: Int,context: Context){
        val product = productList[position]
        var query = "UPDATE cart_items set quantity=$quantity where id=${product.itemId};"

        ClientNetwork.retrofit.update(query).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                product.quantity = quantity
                Toast.makeText(context, "Quantità aggiornata", Toast.LENGTH_LONG).show()
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) =
                Toast.makeText(context, "Failed request: " + t.message, Toast.LENGTH_LONG).show()
        })
    }

    private fun removeFromCart(itemId: Int,position: Int,context: Context){
        var query = "DELETE FROM cart_items WHERE id=$itemId;"

        ClientNetwork.retrofit.remove(query).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) = removeItem(position)
            override fun onFailure(call: Call<JsonObject>, t: Throwable) =
                Toast.makeText(context, "Failed request: " + t.message, Toast.LENGTH_LONG).show()
        })
    }

    private fun addToWish(colorId: Int,context: Context){
        var query = "SELECT id from wishlist_items where user_id=$userId and color_id=$colorId;"

        ClientNetwork.retrofit.select(query).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    val itemsArray = response.body()?.getAsJsonArray("queryset")
                    if (itemsArray != null && itemsArray.size() > 0) {
                        Toast.makeText(context, "Articolo già presente nella wishlist", Toast.LENGTH_LONG).show()
                    }else{
                        query = "INSERT INTO wishlist_items (user_id,color_id) VALUES ($userId,$colorId);"

                        ClientNetwork.retrofit.insert(query).enqueue(object : Callback<JsonObject> {
                            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                                if (response.isSuccessful)
                                    Toast.makeText(context, "Articolo aggiunto alla wishlist", Toast.LENGTH_SHORT).show()
                                else
                                    Toast.makeText(context, "Errore nell'inserimento dell'articolo, riprova", Toast.LENGTH_SHORT).show()
                            }

                            override fun onFailure(call: Call<JsonObject>, t: Throwable) =
                                Toast.makeText(context, "Failed request: " + t.message, Toast.LENGTH_LONG).show()
                        })
                    }
                }
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) =
                Toast.makeText(context, "Failed request: " + t.message, Toast.LENGTH_LONG).show()
        })
    }
}