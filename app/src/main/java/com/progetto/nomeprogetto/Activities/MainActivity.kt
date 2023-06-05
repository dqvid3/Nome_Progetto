package com.progetto.nomeprogetto.Activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.progetto.nomeprogetto.Fragments.AccountFragment
import com.progetto.nomeprogetto.Fragments.CartFragment
import com.progetto.nomeprogetto.Fragments.HomeFragment
import com.progetto.nomeprogetto.Fragments.SettingsFragment
import com.progetto.nomeprogetto.R
import com.progetto.nomeprogetto.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .replace(binding.homeFragmentHomeContainer.id, HomeFragment(),"HomeFragment")
            .commit()

        bottomNavigationSetUp()
    }

    private fun openFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .replace(binding.homeFragmentContainer.id, fragment)
            .commit()
        binding.homeFragmentHomeContainer.visibility = View.GONE
    }

    private fun bottomNavigationSetUp(){
        val bottomNavigationView = binding.bottomNavigation
        bottomNavigationView.setOnItemSelectedListener{ item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    supportFragmentManager.findFragmentById(binding.homeFragmentContainer.id)?.let {
                        supportFragmentManager.beginTransaction()
                            .remove(it)
                            .commit()
                    }
                    binding.homeFragmentHomeContainer.visibility = View.VISIBLE
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
        Handler(Looper.getMainLooper()).postDelayed({
            backPressedOnce = false
        }, 2000)
    }
}