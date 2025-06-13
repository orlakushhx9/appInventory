package com.example.InventarioApp.repository

import com.example.InventarioApp.data.dao.ProductDao
import com.example.InventarioApp.data.entity.Product
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {
    fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts()
    }

    fun getLowStockProducts(): Flow<List<Product>> {
        return productDao.getLowStockProducts()
    }

    fun getProductsByCategory(category: String): Flow<List<Product>> {
        return productDao.getProductsByCategory(category)
    }

    suspend fun getProductById(id: Long): Product? {
        return productDao.getProductById(id)
    }

    suspend fun insertProduct(product: Product): Result<Long> {
        return try {
            Result.success(productDao.insert(product))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProduct(product: Product) {
        productDao.update(product)
    }

    suspend fun deleteProduct(product: Product) {
        productDao.delete(product)
    }

    suspend fun updateStock(productId: Long, amount: Int) {
        productDao.updateStock(productId, amount)
    }
} 