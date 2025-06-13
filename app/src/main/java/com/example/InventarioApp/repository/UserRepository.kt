package com.example.InventarioApp.repository

import com.example.InventarioApp.data.dao.UserDao
import com.example.InventarioApp.data.entity.User

class UserRepository(private val userDao: UserDao) {
    suspend fun login(username: String, password: String): User? {
        return userDao.login(username, password)
    }

    suspend fun register(user: User): Result<Long> {
        return try {
            if (userDao.getUserByUsername(user.username) != null) {
                Result.failure(Exception("Username already exists"))
            } else if (userDao.getUserByEmail(user.email) != null) {
                Result.failure(Exception("Email already exists"))
            } else {
                Result.success(userDao.insert(user))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(user: User) {
        userDao.update(user)
    }
} 