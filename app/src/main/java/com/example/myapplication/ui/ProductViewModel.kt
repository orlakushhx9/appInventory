package com.example.myapplication.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.data.model.Producto
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {
    private val _productos = MutableLiveData<List<Producto>>()
    val productos: LiveData<List<Producto>> = _productos

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    init {
        loadProductos()
    }

    fun loadProductos() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = RetrofitClient.apiService.getProductos()
                if (response.isSuccessful) {
                    _productos.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Error: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error desconocido"
            } finally {
                _loading.value = false
            }
        }
    }

    fun addProducto(nombre: String, cantidad: Int, precio: Double) {
        viewModelScope.launch {
            try {
                val producto = Producto(
                    nombre = nombre,
                    cantidad = cantidad,
                    precio = precio,
                    stock = cantidad
                )
                val response = RetrofitClient.apiService.createProducto(producto)
                if (response.isSuccessful) {
                    loadProductos()
                } else {
                    _error.value = "Error al crear el producto: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al crear el producto"
            }
        }
    }

    fun updateProducto(id: Int, nombre: String, cantidad: Int, precio: Double) {
        viewModelScope.launch {
            try {
                val producto = Producto(
                    id = id,
                    nombre = nombre,
                    cantidad = cantidad,
                    precio = precio,
                    stock = cantidad
                )
                val response = RetrofitClient.apiService.updateProducto(id, producto)
                if (response.isSuccessful) {
                    loadProductos()
                } else {
                    _error.value = "Error al actualizar el producto: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al actualizar el producto"
            }
        }
    }

    fun deleteProducto(id: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteProducto(id)
                if (response.isSuccessful) {
                    loadProductos()
                } else {
                    _error.value = "Error al eliminar el producto: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al eliminar el producto"
            }
        }
    }
} 