package com.progetto.nomeprogetto.Fragments.LoginRegister

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
import com.progetto.nomeprogetto.Activities.MainActivity
import com.progetto.nomeprogetto.ClientNetwork
import com.progetto.nomeprogetto.Objects.User
import com.progetto.nomeprogetto.R
import com.progetto.nomeprogetto.databinding.FragmentRegisterBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater)

        binding.backButton.setOnClickListener{
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_login_container, LoginFragment())
                .commit()
        }

        binding.registerButton.setOnClickListener{
            val username = binding.username.text.toString().trim()
            val name = binding.name.text.toString().trim()
            val surname = binding.surname.text.toString().trim()
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()
            val address = binding.address.text.toString().trim()
            if(name.isEmpty() || surname.isEmpty() || username.isEmpty() || address.isEmpty() ||email.isEmpty() || password.isEmpty())
                Toast.makeText(requireContext(), "Perfavore riempi tutti i campi", Toast.LENGTH_SHORT).show()
            else{
                userExists(email, username) {exists ->
                    if(exists==1)
                        Toast.makeText(requireContext(), "Username inserito già esistente", Toast.LENGTH_SHORT).show()
                    else if(exists==2)
                        Toast.makeText(requireContext(), "Email inserita già esistente", Toast.LENGTH_SHORT).show()
                    else if(exists==0){
                        val  user = User(username,name,surname,email,address,password)
                        registerUser(user)
                    }
                }
            }
        }

        return binding.root
    }

    private fun userExists(email: String, username: String, callback: (Int) -> Unit){
        var query = "SELECT id FROM users WHERE username = '$username';"

        ClientNetwork.retrofit.select(query).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    val resultSetSize = (response.body()?.get("queryset") as JsonArray).size()
                    if(resultSetSize==1) callback(1)
                    else{ //se non esiste un utente con quell'username
                        query = "SELECT id FROM users WHERE email = '$email';"
                        ClientNetwork.retrofit.select(query).enqueue(object : Callback<JsonObject> {
                            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                                if (response.isSuccessful) {
                                    val resultSetSize = (response.body()?.get("queryset") as JsonArray).size()
                                    if(resultSetSize==1) callback(2)
                                    else callback(0)
                                } else
                                    callback(-1)
                            }
                            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                                Toast.makeText(requireContext(), "Failed request: " + t.message, Toast.LENGTH_LONG).show()
                                callback(-1)
                            }
                        })
                    }
                } else
                    callback(-1)
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed request: " + t.message, Toast.LENGTH_LONG).show()
                callback(-1)
            }
        })
    }

    private fun registerUser(user: User){
        val query = "INSERT INTO users (username, name, surname, email, password, salt, picture_path, address)\n" +
        "VALUES ('${user.username}', '${user.name}', '${user.surname}'," +
                " '${user.email}', '${user.password}', '', '', '${user.address}');"

        ClientNetwork.retrofit.insert(query).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if(response.isSuccessful) {
                    Toast.makeText(requireContext(), "Registrazione avvenuta con successo",Toast.LENGTH_SHORT).show()
                    val sharedPref = requireActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
                    sharedPref.edit().putBoolean("IS_LOGGED_IN", true).apply()
                    //createCart(user.email)
                    startActivity(Intent(requireContext(), MainActivity::class.java))
                    requireActivity().finish()
                }else Toast.makeText(requireContext(), "Errore nell'effettuare la registrazione, riprova",Toast.LENGTH_SHORT).show()
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed request: " + t.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    /*private fun createCart(email: String){
        var query = "SELECT id FROM users WHERE email = '$email';"

        ClientNetwork.retrofit.select(query).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if(response.isSuccessful) {
                    val user = response.body()?.getAsJsonArray("queryset")
                    if(user != null) {
                        val _id = user[0].asJsonObject.get("id").asInt
                        val sharedPref = requireActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
                        sharedPref.edit().putInt("ID", _id).apply()

                        query = "INSERT INTO carts (user_id) VALUES ($_id);"
                        ClientNetwork.retrofit.insert(query).enqueue(object : Callback<JsonObject> {
                            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {}
                            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                                Toast.makeText(requireContext(), "Failed request: " + t.message, Toast.LENGTH_LONG).show()
                            }
                        })
                    }
                }else Toast.makeText(requireContext(), "Errore nella creazione del carrello",Toast.LENGTH_SHORT).show()
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed request: " + t.message, Toast.LENGTH_LONG).show()
            }
        })
    }*/
}