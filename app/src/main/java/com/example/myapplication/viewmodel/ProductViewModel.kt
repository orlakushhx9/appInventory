package com.example.myapplication.viewmodel

import androidx.lifecycle.*
import com.example.myapplication.data.entity.Product
import com.example.myapplication.repository.ProductRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {
    private val _productOperationResult = MutableLiveData<Result<Unit>>()
    val productOperationResult: LiveData<Result<Unit>> = _productOperationResult

    val allProducts = repository.getAllProducts()
        .asLiveData(viewModelScope.coroutineContext)

    val lowStockProducts = repository.getLowStockProducts()
        .asLiveData(viewModelScope.coroutineContext)

    fun getProductsByCategory(category: String) = repository.getProductsByCategory(category)
        .asLiveData(viewModelScope.coroutineContext)

    fun addProduct(product: Product) {
        viewModelScope.launch {
            try {
                repository.insertProduct(product)
                _productOperationResult.value = Result.success(Unit)
            } catch (e: Exception) {
                _productOperationResult.value = Result.failure(e)
            }
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            try {
                repository.updateProduct(product)
                _productOperationResult.value = Result.success(Unit)
            } catch (e: Exception) {
                _productOperationResult.value = Result.failure(e)
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            try {
                repository.deleteProduct(product)
                _productOperationResult.value = Result.success(Unit)
            } catch (e: Exception) {
                _productOperationResult.value = Result.failure(e)
            }
        }
    }

    fun updateStock(productId: Long, amount: Int) {
        viewModelScope.launch {
            try {
                repository.updateStock(productId, amount)
                _productOperationResult.value = Result.success(Unit)
            } catch (e: Exception) {
                _productOperationResult.value = Result.failure(e)
            }
        }
    }
} 