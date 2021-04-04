package com.android.ajay.demoproductapp.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.android.ajay.demoproductapp.databinding.ActivitySplashBinding
import kotlinx.coroutines.Runnable

class SplashActivity : BaseActivity() {
    private var binding: ActivitySplashBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            startActivity(Intent(this, HomeActivity::class.java))
        }, 2000)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}

