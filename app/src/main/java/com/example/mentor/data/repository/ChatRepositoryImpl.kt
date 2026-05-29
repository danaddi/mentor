package com.example.mentor.data.repository

import com.example.mentor.data.local.TokenDataStore
import com.example.mentor.data.remote.api.MentorApiService
import com.example.mentor.data.remote.dto.CreateSessionRequest
import com.example.mentor.data.remote.dto.SendMessageRequest
import com.example.mentor.domain.model.ChatSession
import com.example.mentor.domain.model.Message
import com.example.mentor.domain.repository.ChatRepository
import kotlinx.coroutines.flow.first

class ChatRepositoryImpl(
    private val apiService: MentorApiService,
    private val tokenDataStore: TokenDataStore
) : ChatRepository {

    override suspend fun createSession(sectionType: String): Result<ChatSession> {
        return try {
            val token = tokenDataStore.token.first() ?: throw Exception("Not authenticated")
            val request = CreateSessionRequest(sectionType)
            val response = apiService.createSession(token, request)
            Result.success(
                ChatSession(
                    id = response.sessionId,
                    sectionType = response.sectionType,
                    createdAt = response.createdAt
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSessions(): Result<List<ChatSession>> {
        return try {
            val token = tokenDataStore.token.first() ?: throw Exception("Not authenticated")
            val response = apiService.getSessions(token)
            Result.success(
                response.map { session ->
                    ChatSession(
                        id = session.id,
                        sectionType = session.sectionType,
                        createdAt = session.createdAt
                    )
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendMessage(sessionId: String, content: String): Result<Pair<Message, Message>> {
        return try {
            val token = tokenDataStore.token.first() ?: throw Exception("Not authenticated")
            val request = SendMessageRequest(content)
            val response = apiService.sendMessage(token, sessionId, request)
            
            val userMessage = Message(
                id = response.userMessage.id,
                role = response.userMessage.role,
                content = response.userMessage.content,
                createdAt = response.userMessage.createdAt
            )
            
            val aiMessage = Message(
                id = response.aiMessage.id,
                role = response.aiMessage.role,
                content = response.aiMessage.content,
                createdAt = response.aiMessage.createdAt
            )
            
            Result.success(Pair(userMessage, aiMessage))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
