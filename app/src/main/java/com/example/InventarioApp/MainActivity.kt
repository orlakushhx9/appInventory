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
import com.example.InventarioApp.utils.SessionManager
import com.example.InventarioApp.utils.SecurityTestUtils

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private val auth = FirebaseAuth.getInstance()
    private lateinit var sessionManager: SessionManager

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
        
        // Inicializar y configurar el gestor de sesión
        setupSessionManager()
        
        // Ejecutar pruebas de seguridad (solo en desarrollo)
        SecurityTestUtils.runAllSecurityTests()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_logout -> {
                // Detener el timeout de sesión antes de hacer logout
                sessionManager.stopSession()
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
    
    private fun setupSessionManager() {
        sessionManager = SessionManager.getInstance(this)
        sessionManager.setupActivityMonitoring(this)
        android.util.Log.d("MainActivity", "SessionManager configurado")
        
        // Iniciar timeout solo cuando el usuario ya está logueado
        // MainActivity solo se abre después del login exitoso
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            sessionManager.startSession()
            android.util.Log.d("MainActivity", "Timeout de sesión iniciado - usuario logueado")
        }, 1000) // 1 segundo después de configurar
    }
    
    override fun onResume() {
        super.onResume()
        // Reiniciar el temporizador cuando la actividad se reanuda
        sessionManager.resetSessionTimer()
    }
    
    override fun onPause() {
        super.onPause()
        // No detener la sesión aquí para permitir navegación entre actividades
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Detener la sesión cuando la actividad se destruye
        sessionManager.stopSession()
    }
}
