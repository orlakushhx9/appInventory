package com.example.InventarioApp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.InventarioApp.data.dao.UserDao
import com.example.InventarioApp.data.dao.ProductDao
import com.example.InventarioApp.data.entity.User
import com.example.InventarioApp.data.entity.Product

@Database(entities = [User::class, Product::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inventory_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
} 