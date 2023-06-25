package com.progetto.nomeprogetto.Fragments.MainActivity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import com.progetto.nomeprogetto.Activities.LoginRegisterActivity
import com.progetto.nomeprogetto.Fragments.MainActivity.Home.AddPaymentMethodFragment
import com.progetto.nomeprogetto.Fragments.MainActivity.Home.AddShippingAddressFragment
import com.progetto.nomeprogetto.R
import com.progetto.nomeprogetto.databinding.FragmentAccountBinding

class AccountFragment : Fragment() {

    private lateinit var binding: FragmentAccountBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnAddPaymentMethod: Button = view.findViewById(R.id.button3)
        btnAddPaymentMethod.setOnClickListener {
            val addPaymentFragment = AddPaymentMethodFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.home_fragment_container, addPaymentFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        val btnAddShippingAddress: Button = view.findViewById(R.id.button4)
        btnAddShippingAddress.setOnClickListener {
            val addShippingAddressFragment = AddShippingAddressFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.home_fragment_container, addShippingAddressFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAccountBinding.inflate(inflater)
        return binding.root
    }
}
