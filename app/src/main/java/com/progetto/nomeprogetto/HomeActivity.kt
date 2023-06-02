package com.progetto.nomeprogetto

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.progetto.nomeprogetto.Fragments.AccountFragment
import com.progetto.nomeprogetto.Fragments.CartFragment
import com.progetto.nomeprogetto.Fragments.SettingsFragment
import com.progetto.nomeprogetto.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomNavigationSetUp()

        binding.logoutButton.setOnClickListener{
            val sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putBoolean("IS_LOGGED_IN", false)
            editor.apply()
            val i = Intent(this, SplashScreenActivity::class.java)
            startActivity(i)
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

    private fun bottomNavigationSetUp(){
        // Controllare se ci sia già in un fragment?

        val bottomNavigationView = binding.bottomNavigation
        bottomNavigationView.setOnItemSelectedListener{ item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    closeFragment()
                    true
                }
                R.id.navigation_cart -> {
                    openFragment(CartFragment())
                    true
                }
                R.id.navigation_account -> {
                    openFragment(AccountFragment())
                    true
                }
                R.id.navigation_settings -> {
                    openFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
    }
}