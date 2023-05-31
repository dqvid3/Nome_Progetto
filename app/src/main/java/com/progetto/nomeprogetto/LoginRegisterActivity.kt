package com.progetto.nomeprogetto

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.progetto.nomeprogetto.Fragments.LoginFragment
import com.progetto.nomeprogetto.Fragments.RegisterFragment
import com.progetto.nomeprogetto.databinding.ActivityLoginRegisterBinding

class LoginRegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener{
            openFragment(LoginFragment())
            binding.loginButton.visibility = View.INVISIBLE
            binding.registerButton.visibility = View.INVISIBLE
        }

        binding.registerButton.setOnClickListener{
            openFragment(RegisterFragment())
            binding.loginButton.visibility = View.INVISIBLE
            binding.registerButton.visibility = View.INVISIBLE
        }
    }

    private fun openFragment(fragment: Fragment){
        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        transaction.replace(binding.fragmentContainer.id,fragment)
        transaction.commit()
    }
}