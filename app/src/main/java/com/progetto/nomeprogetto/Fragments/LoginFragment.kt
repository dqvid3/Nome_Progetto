package com.progetto.nomeprogetto.Fragments

import android.content.Context
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
            //se il login va a buon fine :
            val sharedPref = requireActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putBoolean("IS_LOGGED_IN", true)
            editor.apply()

            val i = Intent(requireContext(), HomeActivity::class.java)
            startActivity(i)
        }

        binding.registerTextView.setOnClickListener{
            openFragment(RegisterFragment())
        }

        return binding.root
    }

    private fun openFragment(fragment: Fragment){
        val manager = parentFragmentManager
        val transaction = manager.beginTransaction()
        transaction.replace(R.id.fragment_container,fragment)
        transaction.commit()
    }
}