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
import com.example.InventarioApp.data.GoogleAuthManager
import com.example.InventarioApp.databinding.ActivityLoginBinding
import com.example.InventarioApp.viewmodel.AuthViewModel
import com.example.InventarioApp.viewmodel.AuthViewModelFactory
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import android.app.AlertDialog
import android.widget.EditText

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

        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        tvForgotPassword.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Recuperar contraseña")
            builder.setMessage("Ingresa tu correo electrónico para recuperar tu contraseña:")
            val input = EditText(this)
            input.hint = "Correo electrónico"
            builder.setView(input)
            builder.setPositiveButton("Enviar") { dialog, _ ->
                val email = input.text.toString().trim()
                if (email.isNotEmpty()) {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val intent = Intent(this, ForgotPasswordConfirmationActivity::class.java)
                                startActivity(intent)
                            } else {
                                Toast.makeText(this, "No se pudo enviar el correo. Verifica el email.", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Por favor ingresa un correo válido.", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("Cancelar", null)
            builder.show()
        }
    }

    private fun setupGoogleAuth() {
        googleAuthManager = GoogleAuthManager(this)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(googleAuthManager)
        )[AuthViewModel::class.java]
    }

    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.usernameEdit.text.toString()
            val password = binding.passwordEdit.text.toString()

            if (validateInput(email, password)) {
                viewModel.login(email, password)
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

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        if (email.isBlank()) {
            binding.usernameLayout.error = "Email is required"
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