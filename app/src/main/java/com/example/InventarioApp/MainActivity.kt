package com.example.InventarioApp

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.InventarioApp.databinding.ActivityMainBinding
import com.example.InventarioApp.ui.auth.LoginActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        drawerToggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        binding.navigationView.setNavigationItemSelectedListener(this)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_logout -> {
                auth.signOut()
                Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                return true
            }
            //
        }
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
