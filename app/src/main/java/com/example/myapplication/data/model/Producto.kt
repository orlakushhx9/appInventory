package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class Producto(
    @SerializedName("id")
    val id: Int = 0,
    
    @SerializedName("nombre")
    val nombre: String,
    
    @SerializedName("cantidad")
    val cantidad: Int,
    
    @SerializedName("precio")
    val precio: Double,
    
    @SerializedName("stock")
    val stock: Int
) 