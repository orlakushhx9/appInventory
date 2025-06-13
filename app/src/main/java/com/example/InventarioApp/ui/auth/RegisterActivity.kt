package com.example.InventarioApp.ui.auth

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.InventarioApp.R
import com.example.InventarioApp.data.AppDatabase
import com.example.InventarioApp.data.GoogleAuthManager
import com.example.InventarioApp.databinding.ActivityRegisterBinding
import com.example.InventarioApp.repository.UserRepository
import com.example.InventarioApp.viewmodel.AuthViewModel
import com.example.InventarioApp.viewmodel.AuthViewModelFactory

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = UserRepository(database.userDao())
        val googleAuthManager = GoogleAuthManager(this)
        viewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(repository, googleAuthManager)
        )[AuthViewModel::class.java]
    }

    private fun setupClickListeners() {
        binding.registerButton.setOnClickListener {
            val fullName = binding.fullNameEdit.text.toString()
            val email = binding.emailEdit.text.toString()
            val username = binding.usernameEdit.text.toString()
            val password = binding.passwordEdit.text.toString()
            val confirmPassword = binding.confirmPasswordEdit.text.toString()

            if (validateInput(fullName, email, username, password, confirmPassword)) {
                viewModel.register(username, email, password, fullName)
            }
        }

        binding.backToLoginButton.setOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.registerResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, R.string.register_success, Toast.LENGTH_SHORT).show()
                finish()
            }.onFailure { exception ->
                Toast.makeText(this, exception.message ?: getString(R.string.register_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateInput(
        fullName: String,
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true

        if (fullName.isBlank()) {
            binding.fullNameLayout.error = "Full name is required"
            isValid = false
        } else {
            binding.fullNameLayout.error = null
        }

        if (email.isBlank()) {
            binding.emailLayout.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = "Invalid email format"
            isValid = false
        } else {
            binding.emailLayout.error = null
        }

        if (username.isBlank()) {
            binding.usernameLayout.error = "Username is required"
            isValid = false
        } else {
            binding.usernameLayout.error = null
        }

        if (password.isBlank()) {
            binding.passwordLayout.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            binding.passwordLayout.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            binding.passwordLayout.error = null
        }

        if (confirmPassword.isBlank()) {
            binding.confirmPasswordLayout.error = "Confirm password is required"
            isValid = false
        } else if (password != confirmPassword) {
            binding.confirmPasswordLayout.error = "Passwords do not match"
            isValid = false
        } else {
            binding.confirmPasswordLayout.error = null
        }

        return isValid
    }
} 