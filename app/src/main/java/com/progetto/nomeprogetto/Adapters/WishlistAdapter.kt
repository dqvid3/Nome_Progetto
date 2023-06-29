package com.progetto.nomeprogetto.Adapters

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.progetto.nomeprogetto.ClientNetwork
import com.progetto.nomeprogetto.Objects.Product
import com.progetto.nomeprogetto.databinding.WishlistViewDesignBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WishlistAdapter(private var productList: List<Product>,private val listener: CartAdapterListener,private val userId: Int?) : RecyclerView.Adapter<WishlistAdapter.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    class ViewHolder(binding: WishlistViewDesignBinding) : RecyclerView.ViewHolder(binding.root) {
        val imageView = binding.imageView
        val productName = binding.productName
        val price = binding.price
        val colorName = binding.colorName
        val colorView = binding.colorView
        val removeProduct = binding.removeProduct
        val addToCart = binding.addToCart
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = WishlistViewDesignBinding.inflate(
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

        holder.removeProduct.setOnClickListener{
            product.itemId?.let { removeFromWishlist(it,position,holder.itemView.context) }
        }

        holder.addToCart.setOnClickListener{
            product.colorId?.let { addToWish(it, holder.itemView.context) }
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
        productList = productList.toMutableList().apply { removeAt(position) }
        notifyItemRemoved(position)
        if (productList.isEmpty())
            listener.restoreCart()
        else
            notifyItemRangeChanged(position, productList.size)
    }

    private fun removeFromWishlist(itemId: Int,position: Int,context: Context){
        var query = "DELETE FROM wishlist_items WHERE id=$itemId;"

        ClientNetwork.retrofit.remove(query).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) = removeItem(position)
            override fun onFailure(call: Call<JsonObject>, t: Throwable) =
                Toast.makeText(context, "Failed request: " + t.message, Toast.LENGTH_LONG).show()
        })
    }

    private fun addToWish(colorId: Int,context: Context){
        var query = "SELECT pc.stock, ci.id AS item_id " +
                "FROM product_colors pc LEFT JOIN cart_items ci ON pc.id = ci.color_id AND ci.user_id = $userId " +
                "WHERE pc.id = $colorId;"

        println(query)
        ClientNetwork.retrofit.select(query).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    val itemsArray = response.body()?.getAsJsonArray("queryset")
                    if (itemsArray != null && itemsArray.size() > 0) {
                        val itemObject = itemsArray.get(0).asJsonObject
                        if (!itemObject.get("item_id").isJsonNull)
                            Toast.makeText(context, "Articolo già presente nel carrello", Toast.LENGTH_LONG).show()
                        else{
                            val stock = itemObject.get("stock")?.asInt
                            if (stock == 0)
                                Toast.makeText(context, "Articolo non disponibile al momento", Toast.LENGTH_LONG).show()
                            else {
                                query = "INSERT INTO cart_items (user_id,color_id,quantity) VALUES ($userId,$colorId,1);"

                                ClientNetwork.retrofit.insert(query).enqueue(object : Callback<JsonObject> {
                                        override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                                            if (response.isSuccessful)
                                                Toast.makeText(context, "Articolo aggiunto al carrello", Toast.LENGTH_SHORT).show()
                                            else
                                                Toast.makeText(context, "Errore nell'inserimento dell'articolo, riprova", Toast.LENGTH_SHORT).show()
                                        }

                                        override fun onFailure(call: Call<JsonObject>, t: Throwable) =
                                            Toast.makeText(context, "Failed request: " + t.message, Toast.LENGTH_LONG).show()
                                    })
                            }
                        }
                    }
                }
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) =
                Toast.makeText(context, "Failed request: " + t.message, Toast.LENGTH_LONG).show()
        })
    }
}