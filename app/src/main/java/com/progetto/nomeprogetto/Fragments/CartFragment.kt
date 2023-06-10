package com.progetto.nomeprogetto.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.progetto.nomeprogetto.Adapters.CustomAdapter
import com.progetto.nomeprogetto.Objects.ItemViewModel
import com.progetto.nomeprogetto.R
import com.progetto.nomeprogetto.databinding.FragmentCartBinding

class CartFragment : Fragment() {

    val TAG = "CartFragment"
    private lateinit var binding: FragmentCartBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCartBinding.inflate(inflater)

        loadCart()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position
                if(position==0){ // 0 -> Cart
                    loadCart()
                }else if(position==1){ // 1 -> Last orders
                    loadLastOrders()
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Handle tab reselect
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Handle tab unselect
            }
        })

        return binding.root
    }

    private fun loadCart(){
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val data = ArrayList<ItemViewModel>()

        for(i in 1..15){
            data.add(ItemViewModel(R.drawable.ic_launcher_foreground,"item " + i))
        }

        val adapter = CustomAdapter(data)
        binding.recyclerView.adapter = adapter

        adapter.setOnClickListener(object: CustomAdapter.OnClickListener {
            override fun onClick(position: Int, model: ItemViewModel) {
                Log.i(TAG,"index ${position+1}")
            }
        })
    }

    private fun loadLastOrders(){
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val data = ArrayList<ItemViewModel>()

        for(i in 16..25){
            data.add(ItemViewModel(R.drawable.ic_launcher_foreground,"item " + i))
        }
        val adapter = CustomAdapter(data)
        binding.recyclerView.adapter = adapter

        adapter.setOnClickListener(object: CustomAdapter.OnClickListener {
            override fun onClick(position: Int, model: ItemViewModel) {
                Log.i(TAG,"index ${position+1}")
            }
        })
    }
}