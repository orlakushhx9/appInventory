package com.example.myapplication.data.api

import com.example.myapplication.data.model.Producto
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("api/Productos")
    suspend fun getProductos(): Response<List<Producto>>

    @GET("api/Productos/{id}")
    suspend fun getProducto(@Path("id") id: Int): Response<Producto>

    @POST("api/Productos")
    suspend fun createProducto(@Body producto: Producto): Response<Producto>

    @PUT("api/Productos/{id}")
    suspend fun updateProducto(
        @Path("id") id: Int,
        @Body producto: Producto
    ): Response<Producto>

    @DELETE("api/Productos/{id}")
    suspend fun deleteProducto(@Path("id") id: Int): Response<Unit>
} 