package com.example.mentor.domain.repository

import com.example.mentor.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun register(name: String, email: String, password: String): Result<User>
    suspend fun login(email: String, password: String): Result<User>
    fun getToken(): Flow<String?>
    fun getUserId(): Flow<String?>
    fun getUserName(): Flow<String?>
    fun getUserEmail(): Flow<String?>
    suspend fun logout()
    suspend fun deleteAccount(): Result<Unit>
}
