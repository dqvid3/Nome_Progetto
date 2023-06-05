package com.progetto.nomeprogetto.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.progetto.nomeprogetto.R
import com.progetto.nomeprogetto.databinding.FragmentRegisterBinding


class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater)

        binding.button.setOnClickListener{
            //se registrazione avvenuta con successo mostra la pagina di login dinuovo
            //altrimenti mostra toast con errore
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_login_container,LoginFragment())
                .commit()
        }

        return binding.root
    }
}