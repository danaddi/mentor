package com.example.mentor.data.remote.api

import com.example.mentor.data.remote.dto.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.delete
import io.ktor.client.request.*
import io.ktor.http.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MentorApiService @Inject constructor(
    private val client: HttpClient
) {
    companion object {
        const val BASE_URL = "http://10.0.2.2:8080"
    }

    // Auth
    suspend fun register(request: RegisterRequest): AuthResponse =
        client.post("$BASE_URL/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun login(request: LoginRequest): AuthResponse =
        client.post("$BASE_URL/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun getMe(token: String): MeResponse =
        client.get("$BASE_URL/auth/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()

    // Chat
    suspend fun createSession(token: String, request: CreateSessionRequest): CreateSessionResponse =
        client.post("$BASE_URL/chat/sessions") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun getSessions(token: String): List<SessionResponse> =
        client.get("$BASE_URL/chat/sessions") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()

    suspend fun sendMessage(token: String, sessionId: String, request: SendMessageRequest): SendMessageResponse =
        client.post("$BASE_URL/chat/sessions/$sessionId/messages") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    // Notes
    suspend fun getNotes(token: String): List<NoteResponse> =
        client.get("$BASE_URL/notes") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()

    suspend fun createNote(token: String, request: CreateNoteRequest): NoteResponse =
        client.post("$BASE_URL/notes") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    // Emotions
    suspend fun saveEmotion(token: String, request: SaveEmotionRequest): EmotionResponse =
        client.post("$BASE_URL/emotions") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    // Gratitude
    suspend fun saveGratitude(token: String, request: SaveGratitudeRequest): GratitudeResponse =
        client.post("$BASE_URL/gratitude") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    // Delete Account
    suspend fun deleteAccount(token: String) {
        client.delete("$BASE_URL/auth/account") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }
}
