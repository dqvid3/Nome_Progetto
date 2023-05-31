package com.progetto.nomeprogetto.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.progetto.nomeprogetto.HomeActivity
import com.progetto.nomeprogetto.R
import com.progetto.nomeprogetto.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater)

        binding.loginButton.setOnClickListener{
            val i = Intent(requireContext(),HomeActivity::class.java)
            startActivity(i)
        }

        return binding.root
    }
}
