package com.example.mentor.data.repository

import com.example.mentor.data.local.MentorDatabase
import com.example.mentor.data.local.TokenDataStore
import com.example.mentor.data.remote.api.MentorApiService
import com.example.mentor.data.remote.dto.LoginRequest
import com.example.mentor.data.remote.dto.RegisterRequest
import com.example.mentor.domain.model.User
import com.example.mentor.domain.repository.AuthRepository
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class AuthRepositoryImpl(
    private val apiService: MentorApiService,
    private val tokenDataStore: TokenDataStore,
    private val database: MentorDatabase
) : AuthRepository {

    override suspend fun register(name: String, email: String, password: String): Result<User> {
        return try {
            val request = RegisterRequest(name, email, password, agreedToDisclaimer = true)
            val response = apiService.register(request)
            tokenDataStore.saveToken(response.token, response.userId, name = name, email = email)
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
            tokenDataStore.saveToken(response.token, response.userId, email = email)
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

    override fun getUserName(): Flow<String?> {
        return tokenDataStore.userName
    }

    override fun getUserEmail(): Flow<String?> {
        return tokenDataStore.userEmail
    }

    override suspend fun logout() {
        // Clear all local cached data
        try {
            database.noteDao().deleteAllNotes()
            database.emotionDao().deleteAllEmotions()
            database.gratitudeDao().deleteAllGratitudes()
        } catch (_: Exception) { }
        tokenDataStore.clearToken()
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            val token = tokenDataStore.token.first() ?: throw Exception("Not authenticated")
            apiService.deleteAccount(token)
            // Clear all local data
            database.noteDao().deleteAllNotes()
            database.emotionDao().deleteAllEmotions()
            database.gratitudeDao().deleteAllGratitudes()
            tokenDataStore.clearToken()
            Result.success(Unit)
        } catch (e: ClientRequestException) {
            Result.failure(Exception("Ошибка удаления аккаунта"))
        } catch (e: ServerResponseException) {
            Result.failure(Exception("Ошибка сервера. Попробуйте позже"))
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка сети. Проверьте подключение к интернету"))
        }
    }
}
