package com.example.InventarioApp.data

import com.example.InventarioApp.data.entity.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FirebaseProductService {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val productsCollection = db.collection("products")

    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("Usuario no autenticado")
    }

    suspend fun addProduct(product: Product): Result<String> {
        return try {
            val userId = getCurrentUserId()
            val productMap = mapOf<String, Any>(
                
                "userId" to userId,
                "name" to product.name,
                "description" to product.description,
                "quantity" to product.quantity,
                "price" to product.price,
                "category" to product.category,
                "barcode" to (product.barcode ?: ""),
                "minimumStock" to product.minimumStock,
                "lastUpdated" to product.lastUpdated
            )
            
            val docRef = productsCollection.add(productMap).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAllProducts(): Flow<List<Product>> = flow {
        try {
            val userId = getCurrentUserId()
            val snapshot = productsCollection
                .whereEqualTo("userId", userId)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .await()
            
            val products = snapshot.documents.mapNotNull { doc ->
                doc.toProduct()
            }
            emit(products)
        } catch (e: Exception) {
            throw e
        }
    }

    fun getLowStockProducts(): Flow<List<Product>> = flow {
        try {
            val userId = getCurrentUserId()
            val snapshot = productsCollection
                .whereEqualTo("userId", userId)
                .whereLessThanOrEqualTo("quantity", "minimumStock")
                .get()
                .await()
            
            val products = snapshot.documents.mapNotNull { doc ->
                doc.toProduct()
            }
            emit(products)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun updateProduct(product: Product): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            val productMap = mapOf<String, Any>(
                "userId" to userId,
                "name" to product.name,
                "description" to product.description,
                "quantity" to product.quantity,
                "price" to product.price,
                "category" to product.category,
                "barcode" to (product.barcode ?: ""),
                "minimumStock" to product.minimumStock,
                "lastUpdated" to System.currentTimeMillis()
            )
            

            val doc = productsCollection.document(product.id.toString()).get().await()
            if (doc.getString("userId") != userId) {
                return Result.failure(IllegalStateException("No tienes permiso para modificar este producto"))
            }
            
            productsCollection.document(product.id.toString())
                .update(productMap)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(product: Product): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            
            // Verificar que el producto pertenece al usuario actual
            val doc = productsCollection.document(product.id.toString()).get().await()
            if (doc.getString("userId") != userId) {
                return Result.failure(IllegalStateException("No tienes permiso para eliminar este producto"))
            }
            
            productsCollection.document(product.id.toString())
                .delete()
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getProductsByCategory(category: String): Flow<List<Product>> = flow {
        try {
            val userId = getCurrentUserId()
            val snapshot = productsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("category", category)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .await()
            
            val products = snapshot.documents.mapNotNull { doc ->
                doc.toProduct()
            }
            emit(products)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun updateStock(productId: Long, amount: Int): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            
            // Verificar que el producto pertenece al usuario actual
            val doc = productsCollection.document(productId.toString()).get().await()
            if (doc.getString("userId") != userId) {
                return Result.failure(IllegalStateException("No tienes permiso para modificar este producto"))
            }

            // Obtener la cantidad actual
            val currentQuantity = doc.getLong("quantity")?.toInt() ?: 0
            val newQuantity = currentQuantity + amount

            // Actualizar la cantidad
            productsCollection.document(productId.toString())
                .update(
                    mapOf(
                        "quantity" to newQuantity,
                        "lastUpdated" to System.currentTimeMillis()
                    )
                )
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toProduct(): Product? {
        return try {
            Product(
                id = id.toLong(),
                name = getString("name") ?: return null,
                description = getString("description") ?: "",
                quantity = getLong("quantity")?.toInt() ?: 0,
                price = getDouble("price") ?: 0.0,
                category = getString("category") ?: "",
                barcode = getString("barcode"),
                minimumStock = getLong("minimumStock")?.toInt() ?: 0,
                lastUpdated = getLong("lastUpdated") ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }
} 