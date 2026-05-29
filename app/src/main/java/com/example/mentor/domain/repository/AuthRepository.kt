package com.example.mentor.domain.repository

import com.example.mentor.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun register(email: String, password: String): Result<User>
    suspend fun login(email: String, password: String): Result<User>
    fun getToken(): Flow<String?>
    fun getUserId(): Flow<String?>
    suspend fun logout()
}
