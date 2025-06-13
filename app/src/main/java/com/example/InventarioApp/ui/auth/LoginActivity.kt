package com.example.InventarioApp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.InventarioApp.MainActivity
import com.example.InventarioApp.R
import com.example.InventarioApp.data.AppDatabase
import com.example.InventarioApp.data.GoogleAuthManager
import com.example.InventarioApp.databinding.ActivityLoginBinding
import com.example.InventarioApp.repository.UserRepository
import com.example.InventarioApp.viewmodel.AuthViewModel
import com.example.InventarioApp.viewmodel.AuthViewModelFactory

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: AuthViewModel
    private lateinit var googleAuthManager: GoogleAuthManager

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            android.util.Log.d("LoginActivity", "Google Sign-In intent result OK")
            viewModel.signInWithGoogle(result.data)
        } else {
            android.util.Log.e("LoginActivity", "Google Sign-In failed: resultCode=${result.resultCode}")
            Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGoogleAuth()
        setupViewModel()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupGoogleAuth() {
        googleAuthManager = GoogleAuthManager(this)
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = UserRepository(database.userDao())
        viewModel = ViewModelProvider(
            this, 
            AuthViewModelFactory(repository, googleAuthManager)
        )[AuthViewModel::class.java]
    }

    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener {
            val username = binding.usernameEdit.text.toString()
            val password = binding.passwordEdit.text.toString()

            if (validateInput(username, password)) {
                viewModel.login(username, password)
            }
        }

        binding.googleSignInButton.setOnClickListener {
            val signInIntent = viewModel.getGoogleSignInIntent()
            googleSignInLauncher.launch(signInIntent)
        }

        binding.registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.loginResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }.onFailure { exception ->
                Toast.makeText(this, exception.message ?: getString(R.string.login_failed), Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.googleSignInResult.observe(this) { result ->
            result.onSuccess { firebaseUser ->
                Toast.makeText(this, "Google Sign-In successful: ${firebaseUser.email}", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }.onFailure { exception ->
                Toast.makeText(this, "Google Sign-In failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateInput(username: String, password: String): Boolean {
        var isValid = true

        if (username.isBlank()) {
            binding.usernameLayout.error = "Username is required"
            isValid = false
        } else {
            binding.usernameLayout.error = null
        }

        if (password.isBlank()) {
            binding.passwordLayout.error = "Password is required"
            isValid = false
        } else {
            binding.passwordLayout.error = null
        }

        return isValid
    }
} 