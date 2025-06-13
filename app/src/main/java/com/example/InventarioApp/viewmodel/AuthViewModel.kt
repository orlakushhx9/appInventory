package com.example.InventarioApp.viewmodel

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.InventarioApp.data.GoogleAuthManager
import com.example.InventarioApp.data.entity.User
import com.example.InventarioApp.repository.UserRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: UserRepository,
    private val googleAuthManager: GoogleAuthManager
) : ViewModel() {
    private val _loginResult = MutableLiveData<Result<User>>()
    val loginResult: LiveData<Result<User>> = _loginResult

    private val _googleSignInResult = MutableLiveData<Result<FirebaseUser>>()
    val googleSignInResult: LiveData<Result<FirebaseUser>> = _googleSignInResult

    private val _registerResult = MutableLiveData<Result<Long>>()
    val registerResult: LiveData<Result<Long>> = _registerResult

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                val user = repository.login(username, password)
                if (user != null) {
                    _loginResult.value = Result.success(user)
                } else {
                    _loginResult.value = Result.failure(Exception("Invalid credentials"))
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
                            android.util.Log.d("AuthViewModel", "Firebase user signed in: ${firebaseUser.email}")
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
                val user = User(
                    username = username,
                    email = email,
                    password = password,
                    fullName = fullName
                )
                _registerResult.value = repository.register(user)
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
    }
} 