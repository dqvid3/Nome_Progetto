package com.progetto.nomeprogetto.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.progetto.nomeprogetto.ClientNetwork
import com.progetto.nomeprogetto.MainActivity
import com.progetto.nomeprogetto.R
import com.progetto.nomeprogetto.RequestLogin
import com.progetto.nomeprogetto.databinding.FragmentLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater)

        binding.loginButton.setOnClickListener{
            loginUser(RequestLogin(binding.editTextEmail.text.toString(),binding.editTextPassword.text.toString()))
        }

        binding.registerTextView.setOnClickListener{
            openFragment(RegisterFragment())
        }

        return binding.root
    }

    private fun loginUser(requestLogin: RequestLogin){
        val query = "select * from users where email = ${requestLogin.email} and password = ${requestLogin.password}"

        ClientNetwork.retrofit.login(query).enqueue(
            object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    if (response.isSuccessful) {
                        if ((response.body()?.get("queryset") as JsonArray).size() == 1) {
                            val sharedPref = requireActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
                            sharedPref.edit().putBoolean("IS_LOGGED_IN", true).apply()

                            startActivity(Intent(requireContext(), MainActivity::class.java))
                            requireActivity().finish()
                        } else {
                            Toast.makeText(requireContext(), "credenziali errate", Toast.LENGTH_LONG).show()
                        }
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Toast.makeText(requireContext(), "Failed to login: " + t.message, Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    private fun openFragment(fragment: Fragment){
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container,fragment)
            .commit()
    }
}