package com.example.InventarioApp.ui.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.InventarioApp.R
import com.example.InventarioApp.data.GoogleAuthManager
import com.example.InventarioApp.databinding.ActivityRegisterBinding
import com.example.InventarioApp.utils.PasswordValidator
import com.example.InventarioApp.utils.PasswordStrength
import com.example.InventarioApp.utils.EncryptionUtils
import com.example.InventarioApp.utils.InputValidator
import com.example.InventarioApp.utils.SessionManager
import com.example.InventarioApp.viewmodel.AuthViewModel
import com.example.InventarioApp.viewmodel.AuthViewModelFactory

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: AuthViewModel
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupClickListeners()
        observeViewModel()
        setupSessionManager()
    }

    private fun setupViewModel() {
        val googleAuthManager = GoogleAuthManager(this)
        viewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(googleAuthManager)
        )[AuthViewModel::class.java]
    }

    private fun setupClickListeners() {

        binding.passwordEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updatePasswordStrength(s.toString())
            }
        })

        binding.registerButton.setOnClickListener {
            val fullName = binding.fullNameEdit.text.toString()
            val email = binding.emailEdit.text.toString()
            val username = binding.usernameEdit.text.toString()
            val password = binding.passwordEdit.text.toString()
            val confirmPassword = binding.confirmPasswordEdit.text.toString()

            if (validateInput(fullName, email, username, password, confirmPassword)) {
                // Encriptar datos antes de enviar
                val encryptedData = createEncryptedRegistrationData(username, email, password, fullName)
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

        // Validar nombre completo
        val fullNameValidation = InputValidator.validateFullName(fullName)
        if (!fullNameValidation.isValid) {
            binding.fullNameLayout.error = fullNameValidation.errorMessage
            isValid = false
        } else {
            binding.fullNameLayout.error = null
        }

        // Validar email
        val emailValidation = InputValidator.validateEmail(email)
        if (!emailValidation.isValid) {
            binding.emailLayout.error = emailValidation.errorMessage
            isValid = false
        } else {
            binding.emailLayout.error = null
        }

        // Validar nombre de usuario
        val usernameValidation = InputValidator.validateUsername(username)
        if (!usernameValidation.isValid) {
            binding.usernameLayout.error = usernameValidation.errorMessage
            isValid = false
        } else {
            binding.usernameLayout.error = null
        }

        // Validar contraseña
        val passwordValidation = InputValidator.validatePassword(password)
        if (!passwordValidation.isValid) {
            binding.passwordLayout.error = passwordValidation.errorMessage
            isValid = false
        } else {
            binding.passwordLayout.error = null
        }

        // Validar confirmación de contraseña
        if (confirmPassword.isBlank()) {
            binding.confirmPasswordLayout.error = "Confirmar contraseña es requerido"
            isValid = false
        } else if (password != confirmPassword) {
            binding.confirmPasswordLayout.error = "Las contraseñas no coinciden"
            isValid = false
        } else {
            binding.confirmPasswordLayout.error = null
        }

        return isValid
    }

    private fun updatePasswordStrength(password: String) {
        if (password.isBlank()) {
            binding.passwordStrengthContainer.visibility = View.GONE
            return
        }

        val validation = PasswordValidator.validatePassword(password)
        binding.passwordStrengthContainer.visibility = View.VISIBLE

        // Actualizar texto de fortaleza
        binding.passwordStrengthText.text = validation.strengthMessage

        // Actualizar color del texto según fortaleza
        val textColor = when (validation.strength) {
            PasswordStrength.WEAK -> ContextCompat.getColor(this, android.R.color.holo_red_dark)
            PasswordStrength.MEDIUM -> ContextCompat.getColor(this, android.R.color.holo_orange_dark)
            PasswordStrength.STRONG -> ContextCompat.getColor(this, android.R.color.holo_green_dark)
        }
        binding.passwordStrengthText.setTextColor(textColor)

        // Actualizar barra de progreso
        val progressWeight = when (validation.strength) {
            PasswordStrength.WEAK -> 0.33f
            PasswordStrength.MEDIUM -> 0.66f
            PasswordStrength.STRONG -> 1.0f
        }

        val layoutParams = binding.passwordStrengthIndicator.layoutParams as LinearLayout.LayoutParams
        layoutParams.width = 0
        layoutParams.weight = progressWeight
        binding.passwordStrengthIndicator.layoutParams = layoutParams

        // Actualizar color de la barra
        val barColor = when (validation.strength) {
            PasswordStrength.WEAK -> R.drawable.password_strength_weak
            PasswordStrength.MEDIUM -> R.drawable.password_strength_medium
            PasswordStrength.STRONG -> R.drawable.password_strength_strong
        }
        binding.passwordStrengthIndicator.setBackgroundResource(barColor)

        // Mostrar requisitos faltantes
        if (validation.missingRequirements.isNotEmpty()) {
            binding.passwordRequirementsText.text = "Faltan: ${validation.missingRequirements.joinToString(", ")}"
            binding.passwordRequirementsText.visibility = View.VISIBLE
        } else {
            binding.passwordRequirementsText.visibility = View.GONE
        }
    }

    private fun createEncryptedRegistrationData(
        username: String,
        email: String,
        password: String,
        fullName: String
    ): String {
        val registrationData = mapOf(
            "username" to username,
            "email" to email,
            "password" to password,
            "fullName" to fullName,
            "timestamp" to System.currentTimeMillis(),
            "deviceInfo" to "Android_${android.os.Build.VERSION.RELEASE}"
        )

        return EncryptionUtils.createEncryptedPayload(registrationData)
    }
    
    private fun setupSessionManager() {
        sessionManager = SessionManager.getInstance(this)
        sessionManager.setupActivityMonitoring(this)
        // NO iniciar timeout aquí - solo configurar monitoreo
        android.util.Log.d("RegisterActivity", "SessionManager configurado - sin timeout (usuario no logueado)")
    }
    
    override fun onResume() {
        super.onResume()
        // NO reiniciar timer en RegisterActivity - usuario no está logueado
        android.util.Log.d("RegisterActivity", "onResume - sin reiniciar timer (usuario no logueado)")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        sessionManager.stopSession()
    }
} 