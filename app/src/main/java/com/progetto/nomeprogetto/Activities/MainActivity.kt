package com.progetto.nomeprogetto.Activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.EditText
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.progetto.nomeprogetto.Fragments.MainActivity.*
import com.progetto.nomeprogetto.Fragments.MainActivity.Account.AccountFragment
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

        val searchEditText = binding.searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(ContextCompat.getColor(this, R.color.white))

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

                    binding.bottomNavigation.selectedItemId = R.id.navigation_home
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

    private fun openFragment(fragment: Fragment,fragmentTag: String){
        if (fragmentTag.equals("AccountFragment"))
            binding.searchView.visibility = View.GONE
        else binding.searchView.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .replace(binding.homeFragmentContainer.id, fragment, fragmentTag)
            .commit()
        supportFragmentManager.findFragmentById(binding.homeFragmentHomeContainer.id)?.let {
            supportFragmentManager.beginTransaction()
                .remove(it)
                .commit()
        }
    }

    private fun bottomNavigationSetUp(){
        val bottomNavigationView = binding.bottomNavigation
        bottomNavigationView.setOnItemSelectedListener{ item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(binding.homeFragmentHomeContainer.id,HomeFragment())
                        .commit()
                    supportFragmentManager.findFragmentById(binding.homeFragmentContainer.id)?.let {
                        supportFragmentManager.beginTransaction()
                            .remove(it)
                            .commit()
                    }
                    true
                }
                R.id.navigation_cart -> {
                    openFragment(CartFragment(),"CartFragment")
                    true
                }
                R.id.navigation_account -> {
                    openFragment(AccountFragment(),"AccountFragment")
                    true
                }
                R.id.navigation_settings -> {
                    openFragment(SettingsFragment(),"SettingsFragment")
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