package com.example.InventarioApp.viewmodel

import androidx.lifecycle.*
import com.example.InventarioApp.data.FirebaseProductService
import com.example.InventarioApp.data.entity.Product
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ProductViewModel(
    private val firebaseService: FirebaseProductService
) : ViewModel() {
    private val _productOperationResult = MutableLiveData<Result<Unit>>()
    val productOperationResult: LiveData<Result<Unit>> = _productOperationResult

    private val _operationMessage = MutableLiveData<String>()
    val operationMessage: LiveData<String> = _operationMessage

    val allProducts = firebaseService.getAllProducts()
        .catch { e -> 
            _productOperationResult.value = Result.failure(e)
        }
        .asLiveData(viewModelScope.coroutineContext)

    val lowStockProducts = firebaseService.getLowStockProducts()
        .catch { e -> 
            _productOperationResult.value = Result.failure(e)
        }
        .asLiveData(viewModelScope.coroutineContext)

    fun getProductsByCategory(category: String) = firebaseService.getProductsByCategory(category)
        .catch { e -> 
            _productOperationResult.value = Result.failure(e)
        }
        .asLiveData(viewModelScope.coroutineContext)

    fun addProduct(product: Product) {
        viewModelScope.launch {
            try {
                firebaseService.addProduct(product)
                _productOperationResult.value = Result.success(Unit)
                _operationMessage.value = "Producto agregado correctamente"
            } catch (e: Exception) {
                _productOperationResult.value = Result.failure(e)
                _operationMessage.value = "Error al agregar el producto: ${e.message}"
            }
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            try {
                firebaseService.updateProduct(product)
                _productOperationResult.value = Result.success(Unit)
                _operationMessage.value = "Producto actualizado correctamente"
            } catch (e: Exception) {
                _productOperationResult.value = Result.failure(e)
                _operationMessage.value = "Error al actualizar el producto: ${e.message}"
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            try {
                firebaseService.deleteProduct(product)
                _productOperationResult.value = Result.success(Unit)
                _operationMessage.value = "Producto eliminado correctamente"
            } catch (e: Exception) {
                _productOperationResult.value = Result.failure(e)
                _operationMessage.value = "Error al eliminar el producto: ${e.message}"
            }
        }
    }

    fun updateStock(productId: Long, amount: Int) {
        viewModelScope.launch {
            try {
                firebaseService.updateStock(productId, amount)
                _productOperationResult.value = Result.success(Unit)
                _operationMessage.value = if (amount > 0) {
                    "Stock aumentado correctamente"
                } else {
                    "Stock reducido correctamente"
                }
            } catch (e: Exception) {
                _productOperationResult.value = Result.failure(e)
                _operationMessage.value = "Error al actualizar el stock: ${e.message}"
            }
        }
    }
} 