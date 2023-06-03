package com.progetto.nomeprogetto

import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.progetto.nomeprogetto.Fragments.AccountFragment
import com.progetto.nomeprogetto.Fragments.CartFragment
import com.progetto.nomeprogetto.Fragments.HomeFragment
import com.progetto.nomeprogetto.Fragments.SettingsFragment
import com.progetto.nomeprogetto.databinding.ActivityHomeBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        openFragment(HomeFragment())

        bottomNavigationSetUp()
    }

    private fun openFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .replace(binding.homeFragmentContainer.id, fragment)
            .commit();
    }

    private fun bottomNavigationSetUp(){
        val bottomNavigationView = binding.bottomNavigation
        bottomNavigationView.setOnItemSelectedListener{ item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    openFragment(HomeFragment())
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

    private var backPressedOnce = false

    override fun onBackPressed() {
        if (backPressedOnce) {
            moveTaskToBack(true)
            return
        }
        backPressedOnce = true
        Handler().postDelayed({
            backPressedOnce = false
        }, 2000)
    }
}