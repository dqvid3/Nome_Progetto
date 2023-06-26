package com.progetto.nomeprogetto.Fragments.MainActivity.Account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.progetto.nomeprogetto.databinding.FragmentAddPaymentMethodBinding

class AddPaymentMethodFragment : Fragment() {

    private lateinit var binding: FragmentAddPaymentMethodBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddPaymentMethodBinding.inflate(inflater)

        binding.saveButton.setOnClickListener {
            savePaymentMethod()
        }

        binding.backButton.setOnClickListener{
            closeFragment()
        }

        return binding.root
    }

    private fun savePaymentMethod() {
        closeFragment()
    }

    private fun closeFragment(){
        parentFragmentManager.beginTransaction()
            .remove(this)
            .commit()
        parentFragmentManager.findFragmentByTag("AccountFragment")?.let {
            parentFragmentManager.beginTransaction()
                .show(it)
                .commit()
        }
    }
}
