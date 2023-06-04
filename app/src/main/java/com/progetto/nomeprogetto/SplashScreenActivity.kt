package com.progetto.nomeprogetto

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.progetto.nomeprogetto.Activities.LoginRegisterActivity
import com.progetto.nomeprogetto.Activities.MainActivity
import com.progetto.nomeprogetto.databinding.ActivitySplashScreenBinding


class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            val i: Intent
            val sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
            val isLoggedIn = sharedPref.getBoolean("IS_LOGGED_IN", false)
            if(isLoggedIn)
                i = Intent(this@SplashScreenActivity, MainActivity::class.java)
            else
                i = Intent(this@SplashScreenActivity, LoginRegisterActivity::class.java)
            startActivity(i)
            finish()
        }, 1500)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(androidx.constraintlayout.widget.R.anim.abc_fade_in,
            androidx.appcompat.R.anim.abc_fade_out)
    }
}