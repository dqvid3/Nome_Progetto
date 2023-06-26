package com.progetto.nomeprogetto.Fragments.MainActivity.Account

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.progetto.nomeprogetto.R
import com.progetto.nomeprogetto.databinding.FragmentAccountBinding

class AccountFragment : Fragment() {

    private lateinit var binding: FragmentAccountBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAccountBinding.inflate(inflater)

        binding.addPayment.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .add(R.id.home_fragment_container, AddCardFragment())
                .hide(this)
                .commit()
        }

        binding.addAddress.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .add(R.id.home_fragment_container, AddAddressFragment())
                .hide(this)
                .commit()
        }

        return binding.root
    }
}
