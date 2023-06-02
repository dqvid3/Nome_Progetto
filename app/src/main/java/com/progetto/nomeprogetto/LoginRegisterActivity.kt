package com.progetto.nomeprogetto

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.progetto.nomeprogetto.Fragments.RegisterFragment
import com.progetto.nomeprogetto.databinding.ActivityLoginRegisterBinding

class LoginRegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener{
            //se il login va a buon fine :
            val sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putBoolean("IS_LOGGED_IN", true)
            editor.apply()

            val i = Intent(this,HomeActivity::class.java)
            startActivity(i)
        }

        binding.registerTextView.setOnClickListener{
            openFragment(RegisterFragment())
            binding.constraintLayout.visibility = View.GONE
        }
    }

    private fun openFragment(fragment: Fragment){
        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        transaction.replace(binding.fragmentContainer.id,fragment)
        transaction.commit()
        binding.fragmentContainer.bringToFront()
    }
}