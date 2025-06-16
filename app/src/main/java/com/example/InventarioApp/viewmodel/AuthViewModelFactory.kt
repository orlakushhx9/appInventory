package com.example.InventarioApp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.InventarioApp.data.GoogleAuthManager

class AuthViewModelFactory(
    private val googleAuthManager: GoogleAuthManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(googleAuthManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 