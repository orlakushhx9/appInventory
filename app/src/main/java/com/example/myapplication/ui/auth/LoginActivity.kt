package com.example.myapplication.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.databinding.ActivityLoginBinding
import com.example.myapplication.repository.UserRepository
import com.example.myapplication.viewmodel.AuthViewModel
import com.example.myapplication.viewmodel.AuthViewModelFactory

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = UserRepository(database.userDao())
        viewModel = ViewModelProvider(this, AuthViewModelFactory(repository))[AuthViewModel::class.java]
    }

    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener {
            val username = binding.usernameEdit.text.toString()
            val password = binding.passwordEdit.text.toString()

            if (validateInput(username, password)) {
                viewModel.login(username, password)
            }
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