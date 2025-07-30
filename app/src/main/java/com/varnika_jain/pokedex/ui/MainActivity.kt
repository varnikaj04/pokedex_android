package com.varnika_jain.pokedex.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.varnika_jain.pokedex.R
import com.varnika_jain.pokedex.databinding.ActivityMainBinding
import com.varnika_jain.pokedex.ui.home.HomeFragment
import com.varnika_jain.pokedex.utils.replaceFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)
        if (savedInstanceState == null) {
            supportFragmentManager.replaceFragment(HomeFragment(), binding.fragmentContainer.id)
        }
    }
}