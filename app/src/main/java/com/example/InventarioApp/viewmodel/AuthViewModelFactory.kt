package com.example.InventarioApp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.InventarioApp.data.GoogleAuthManager
import com.example.InventarioApp.repository.UserRepository

class AuthViewModelFactory(
    private val repository: UserRepository,
    private val googleAuthManager: GoogleAuthManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository, googleAuthManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 