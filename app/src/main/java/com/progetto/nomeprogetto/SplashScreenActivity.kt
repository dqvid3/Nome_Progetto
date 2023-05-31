package com.progetto.nomeprogetto

import android.R
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.progetto.nomeprogetto.databinding.ActivitySplashScreenBinding


class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    private var loggedIn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            val i: Intent
            //prendere loggedIn dalle variabili del telefono?
            if(loggedIn)
                i = Intent(this@SplashScreenActivity, HomeActivity::class.java)
            else
                i = Intent(this@SplashScreenActivity, LoginRegisterActivity::class.java)
            startActivity(i)
            finish()
        }, 1500)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in,R.anim.fade_out)
    }
}