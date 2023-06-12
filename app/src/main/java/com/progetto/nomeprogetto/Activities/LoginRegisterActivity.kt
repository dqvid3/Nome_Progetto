package com.progetto.nomeprogetto.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.progetto.nomeprogetto.Fragments.LoginRegister.LoginFragment
import com.progetto.nomeprogetto.databinding.ActivityLoginRegisterBinding

class LoginRegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentLoginContainer.id, LoginFragment())
            .commit()
    }
}