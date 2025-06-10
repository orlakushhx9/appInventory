package com.example.myapplication.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val quantity: Int,
    val price: Double,
    val category: String,
    val barcode: String?,
    val minimumStock: Int,
    val lastUpdated: Long = System.currentTimeMillis()
) 