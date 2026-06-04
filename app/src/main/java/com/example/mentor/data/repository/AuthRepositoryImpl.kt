package com.example.mentor.data.repository

import com.example.mentor.data.local.TokenDataStore
import com.example.mentor.data.remote.api.MentorApiService
import com.example.mentor.data.remote.dto.LoginRequest
import com.example.mentor.data.remote.dto.RegisterRequest
import com.example.mentor.domain.model.User
import com.example.mentor.domain.repository.AuthRepository
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.coroutines.flow.Flow

class AuthRepositoryImpl(
    private val apiService: MentorApiService,
    private val tokenDataStore: TokenDataStore
) : AuthRepository {

    override suspend fun register(email: String, password: String): Result<User> {
        return try {
            val request = RegisterRequest(email, password, agreedToDisclaimer = true)
            val response = apiService.register(request)
            tokenDataStore.saveToken(response.token, response.userId)
            Result.success(User(response.userId, response.token))
        } catch (e: ClientRequestException) {
            when (e.response.status.value) {
                409 -> Result.failure(Exception("Пользователь с таким email уже существует"))
                else -> Result.failure(Exception("Ошибка регистрации"))
            }
        } catch (e: ServerResponseException) {
            Result.failure(Exception("Ошибка сервера. Попробуйте позже"))
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка сети. Проверьте подключение к интернету"))
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val request = LoginRequest(email, password)
            val response = apiService.login(request)
            tokenDataStore.saveToken(response.token, response.userId)
            Result.success(User(response.userId, response.token))
        } catch (e: ClientRequestException) {
            when (e.response.status.value) {
                401 -> Result.failure(Exception("Неверный логин или пароль"))
                404 -> Result.failure(Exception("Пользователь не найден"))
                else -> Result.failure(Exception("Ошибка авторизации"))
            }
        } catch (e: ServerResponseException) {
            Result.failure(Exception("Ошибка сервера. Попробуйте позже"))
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка сети. Проверьте подключение к интернету"))
        }
    }

    override fun getToken(): Flow<String?> {
        return tokenDataStore.token
    }

    override fun getUserId(): Flow<String?> {
        return tokenDataStore.userId
    }

    override suspend fun logout() {
        tokenDataStore.clearToken()
    }
}
