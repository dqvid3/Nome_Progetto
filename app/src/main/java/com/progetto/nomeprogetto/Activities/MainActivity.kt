package com.progetto.nomeprogetto.Activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.progetto.nomeprogetto.Fragments.MainActivity.*
import com.progetto.nomeprogetto.Fragments.MainActivity.Home.HomeFragment
import com.progetto.nomeprogetto.Fragments.MainActivity.Home.ProductFragment
import com.progetto.nomeprogetto.R
import com.progetto.nomeprogetto.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .add(binding.homeFragmentHomeContainer.id, HomeFragment())
            .commit()

        bottomNavigationSetUp()

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if(!query.isNullOrBlank()) {
                    val productFragment = ProductFragment()
                    val bundle = Bundle()
                    bundle.putString("searchQuery",query)
                    productFragment.arguments = bundle
                    supportFragmentManager.beginTransaction()
                        .replace(binding.homeFragmentHomeContainer.id, productFragment,"ProductFragment")
                        .commit()
                    binding.searchView.clearFocus()
                    return true
                }
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean { return true }
        })
    }

    private fun openFragment(fragment: Fragment){
        if(fragment is CartFragment)
            supportFragmentManager.beginTransaction()
                .replace(binding.homeFragmentContainer.id, fragment, "CartFragment")
                .commit()
        else
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
                    if(binding.homeFragmentHomeContainer.visibility==View.VISIBLE){
                        supportFragmentManager.beginTransaction()
                            .replace(binding.homeFragmentHomeContainer.id, HomeFragment())
                            .commit()
                        binding.searchView.setQuery("",false)
                    }else {
                        supportFragmentManager.findFragmentById(binding.homeFragmentContainer.id)
                            ?.let {
                                supportFragmentManager.beginTransaction()
                                    .remove(it)
                                    .commit()
                            }
                        binding.homeFragmentHomeContainer.visibility = View.VISIBLE
                    }
                    //binding.searchView.visibility = View.VISIBLE
                    true
                }
                R.id.navigation_cart -> {
                    openFragment(CartFragment())
                    //binding.searchView.visibility = View.GONE
                    true
                }
                R.id.navigation_account -> {
                    openFragment(AccountFragment())
                    //binding.searchView.visibility = View.VISIBLE
                    true
                }
                R.id.navigation_settings -> {
                    openFragment(SettingsFragment())
                    //binding.searchView.visibility = View.VISIBLE
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