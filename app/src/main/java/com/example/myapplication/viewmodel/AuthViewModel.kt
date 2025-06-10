package com.example.myapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.entity.User
import com.example.myapplication.repository.UserRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: UserRepository) : ViewModel() {
    private val _loginResult = MutableLiveData<Result<User>>()
    val loginResult: LiveData<Result<User>> = _loginResult

    private val _registerResult = MutableLiveData<Result<Long>>()
    val registerResult: LiveData<Result<Long>> = _registerResult

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                val user = repository.login(username, password)
                if (user != null) {
                    _loginResult.value = Result.success(user)
                } else {
                    _loginResult.value = Result.failure(Exception("Invalid credentials"))
                }
            } catch (e: Exception) {
                _loginResult.value = Result.failure(e)
            }
        }
    }

    fun register(username: String, email: String, password: String, fullName: String) {
        viewModelScope.launch {
            try {
                val user = User(
                    username = username,
                    email = email,
                    password = password,
                    fullName = fullName
                )
                _registerResult.value = repository.register(user)
            } catch (e: Exception) {
                _registerResult.value = Result.failure(e)
            }
        }
    }
} 