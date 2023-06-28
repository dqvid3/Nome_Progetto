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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.progetto.nomeprogetto.Activities.BuyActivity
import com.progetto.nomeprogetto.ClientNetwork
import com.progetto.nomeprogetto.Fragments.MainActivity.Account.AddCardFragment
import com.progetto.nomeprogetto.Objects.UserCard
import com.progetto.nomeprogetto.R
import com.progetto.nomeprogetto.databinding.CardViewDesignBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CardAdapter(private var cardList: ArrayList<UserCard>) : RecyclerView.Adapter<CardAdapter.ViewHolder>() {

    private var lastSelectedPosition = RecyclerView.NO_POSITION

    class ViewHolder(binding: CardViewDesignBinding) : RecyclerView.ViewHolder(binding.root) {
        val cardholderName = binding.cardholderName
        val cardNumber = binding.cardNumber
        val expirationDate = binding.expirationDate
        val layout = binding.layout
        val selectButton = binding.selectButton
        val modifyCard = binding.modifyCard
        val removeCard = binding.removeCard
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = CardViewDesignBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return cardList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val card = cardList[position]
        val context = holder.itemView.context

        holder.cardholderName.text = card.name
        holder.cardNumber.text = card.card_number
        holder.expirationDate.text = card.expiration_date
        if (card.selected) {
            updateCurrentAddress(context,card.id)
            holder.layout.setBackgroundColor(Color.parseColor("#D9EBFA"))
            lastSelectedPosition = position
            holder.selectButton.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.redPart))
        }else
            holder.selectButton.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.notSelected))
        holder.selectButton.setOnClickListener{
            if (lastSelectedPosition != RecyclerView.NO_POSITION) {
                cardList[lastSelectedPosition].selected = false
                notifyItemChanged(lastSelectedPosition)
            }
            card.selected = true
            notifyItemChanged(position)
            lastSelectedPosition = position
            updateCurrentAddress(context, card.id)
        }

        holder.itemView.setOnClickListener{
            holder.selectButton.performClick()
        }

        holder.modifyCard.setOnClickListener{
            val fragment = AddCardFragment()
            val bundle = Bundle()
            bundle.putParcelable("card", card)
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

        holder.removeCard.setOnClickListener{
            if (cardList.size>1)
                removeCard(context,card.id)
            else
                Toast.makeText(context, "Non puoi eliminare la carda se ne possiedi soltanto una", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateCurrentAddress(context: Context, cardId: Int){
        val sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("ID", 0)
        var query = "UPDATE users SET current_card_id = $cardId WHERE id=$userId;"

        ClientNetwork.retrofit.update(query).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {}
            override fun onFailure(call: Call<JsonObject>, t: Throwable) =
                Toast.makeText(context, "Failed request: " + t.message, Toast.LENGTH_LONG).show()
        })
    }

    private fun removeCard(context: Context, cardId: Int){
        var query = "DELETE FROM user_payments WHERE id=$cardId;"

        ClientNetwork.retrofit.remove(query).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    val index = cardList.indexOfFirst { it.id == cardId }
                    if (index != -1) {
                        val wasSelected = cardList[index].selected
                        cardList.removeAt(index)
                        notifyItemRemoved(index)

                        if (wasSelected) {
                            lastSelectedPosition = RecyclerView.NO_POSITION
                            if (cardList.isNotEmpty()) {
                                cardList[0].selected = true
                                lastSelectedPosition = 0
                                updateCurrentAddress(context, cardList[0].id)
                                notifyItemChanged(0)
                            }else  updateCurrentAddress(context,-1)
                        }
                    }
                } else
                    Toast.makeText(context, "Failed to remove card", Toast.LENGTH_LONG).show()
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) =
                Toast.makeText(context, "Failed request: " + t.message, Toast.LENGTH_LONG).show()
        })
    }
}