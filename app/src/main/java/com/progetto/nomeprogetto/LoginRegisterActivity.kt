package com.progetto.nomeprogetto

import android.content.Context
import android.content.Intent
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

       openFragment(LoginFragment())
    }

    private fun openFragment(fragment: Fragment){
        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        transaction.replace(binding.fragmentContainer.id,fragment)
        transaction.commit()
    }
}