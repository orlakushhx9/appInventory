package com.example.InventarioApp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.InventarioApp.data.FirebaseProductService

class ProductViewModelFactory(
    private val firebaseService: FirebaseProductService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductViewModel(firebaseService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 