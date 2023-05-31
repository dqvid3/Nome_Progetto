package com.progetto.nomeprogetto

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.progetto.nomeprogetto.databinding.ActivityLoginRegisterBinding

class LoginRegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener{
            openFragment(LoginFragment())
        }
    }

    private fun openFragment(fragment: Fragment){
        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        transaction.replace(binding.fragmentContainer.id,fragment)
        transaction.commit()
    }

    private fun closeFragment(){
        val manager = supportFragmentManager
        val fragment = manager.findFragmentById(binding.fragmentContainer.id)
        if(fragment != null){
            val transaction = manager.beginTransaction()
            transaction.remove(fragment)
            transaction.commit()
        }
    }
}