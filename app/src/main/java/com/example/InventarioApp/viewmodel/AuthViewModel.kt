package com.example.InventarioApp.viewmodel

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.InventarioApp.data.GoogleAuthManager
import com.example.InventarioApp.utils.EncryptionUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class AuthViewModel(
    private val googleAuthManager: GoogleAuthManager
) : ViewModel() {
    private val _loginResult = MutableLiveData<Result<FirebaseUser>>()
    val loginResult: LiveData<Result<FirebaseUser>> = _loginResult

    private val _googleSignInResult = MutableLiveData<Result<FirebaseUser>>()
    val googleSignInResult: LiveData<Result<FirebaseUser>> = _googleSignInResult

    private val _registerResult = MutableLiveData<Result<FirebaseUser>>()
    val registerResult: LiveData<Result<FirebaseUser>> = _registerResult

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            if (user != null) {
                                _loginResult.value = Result.success(user)
                            } else {
                                _loginResult.value = Result.failure(Exception("Usuario no encontrado"))
                            }
                        } else {
                            _loginResult.value = Result.failure(task.exception ?: Exception("Error de autenticación"))
                        }
                    }
            } catch (e: Exception) {
                _loginResult.value = Result.failure(e)
            }
        }
    }

    fun signInWithGoogle(data: Intent?) {
        viewModelScope.launch {
            try {
                android.util.Log.d("AuthViewModel", "Handling Google Sign-In result")
                val accountResult = googleAuthManager.handleSignInResult(data)
                accountResult.onSuccess { account ->
                    android.util.Log.d("AuthViewModel", "Google account obtained: ${account.email}")
                    val firebaseResult = googleAuthManager.firebaseAuthWithGoogle(account)
                    firebaseResult.onSuccess {
                        val firebaseUser = googleAuthManager.getCurrentUser()
                        if (firebaseUser != null) {
                            // Guardar datos en Firestore si es nuevo
                            val userMap = hashMapOf(
                                "uid" to firebaseUser.uid,
                                "email" to firebaseUser.email,
                                "displayName" to (firebaseUser.displayName ?: "")
                            )
                            db.collection("users").document(firebaseUser.uid).set(userMap)
                            _googleSignInResult.value = Result.success(firebaseUser)
                        } else {
                            android.util.Log.e("AuthViewModel", "Firebase user is null after sign-in")
                            _googleSignInResult.value = Result.failure(Exception("Firebase user is null"))
                        }
                    }.onFailure { exception ->
                        android.util.Log.e("AuthViewModel", "Firebase auth with Google failed", exception)
                        _googleSignInResult.value = Result.failure(exception)
                    }
                }.onFailure { exception ->
                    android.util.Log.e("AuthViewModel", "Google sign in failed", exception)
                    _googleSignInResult.value = Result.failure(exception)
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Exception in signInWithGoogle", e)
                _googleSignInResult.value = Result.failure(e)
            }
        }
    }

    fun register(username: String, email: String, password: String, fullName: String) {
        viewModelScope.launch {
            try {
                // Encriptar datos antes de enviar a Firebase
                val encryptedData = EncryptionUtils.createEncryptedPayload(
                    mapOf(
                        "username" to username,
                        "email" to email,
                        "password" to password,
                        "fullName" to fullName,
                        "timestamp" to System.currentTimeMillis(),
                        "deviceInfo" to "Android_${android.os.Build.VERSION.RELEASE}"
                    )
                )
                

                
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            if (user != null) {
                                // Guardar datos adicionales encriptados en Firestore
                                val userMap = hashMapOf(
                                    "uid" to user.uid,
                                    "email" to user.email,
                                    "username" to username,
                                    "fullName" to fullName,
                                    "encryptedData" to encryptedData,
                                    "registrationTimestamp" to System.currentTimeMillis()
                                )
                                db.collection("users").document(user.uid).set(userMap)
                                _registerResult.value = Result.success(user)
                            } else {
                                _registerResult.value = Result.failure(Exception("Usuario no encontrado tras registro"))
                            }
                        } else {
                            _registerResult.value = Result.failure(task.exception ?: Exception("Error al registrar usuario"))
                        }
                    }
            } catch (e: Exception) {
                _registerResult.value = Result.failure(e)
            }
        }
    }

    fun getGoogleSignInIntent(): Intent {
        return googleAuthManager.getSignInIntent()
    }

    fun signOut() {
        googleAuthManager.signOut()
        auth.signOut()
    }
} 