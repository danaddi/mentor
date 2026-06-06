package com.example.mentor.data.repository

import com.example.mentor.data.local.TokenDataStore
import com.example.mentor.data.local.dao.MessageDao
import com.example.mentor.data.local.entity.MessageEntity
import com.example.mentor.data.remote.api.MentorApiService
import com.example.mentor.data.remote.dto.CreateSessionRequest
import com.example.mentor.data.remote.dto.SendMessageRequest
import com.example.mentor.domain.model.ChatSession
import com.example.mentor.domain.model.Message
import com.example.mentor.domain.repository.ChatRepository
import kotlinx.coroutines.flow.first

class ChatRepositoryImpl(
    private val apiService: MentorApiService,
    private val tokenDataStore: TokenDataStore,
    private val messageDao: MessageDao
) : ChatRepository {

    override suspend fun createSession(sectionType: String): Result<ChatSession> {
        return try {
            val token = tokenDataStore.token.first() ?: throw Exception("Not authenticated")
            val request = CreateSessionRequest(sectionType)
            val response = apiService.createSession(token, request)
            
            val session = ChatSession(
                id = response.sessionId,
                sectionType = response.sectionType,
                createdAt = response.createdAt
            )
            
            Result.success(session)
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
            
            // Validate response content
            if (response.userMessage.content.isBlank()) {
                throw Exception("User message content is empty")
            }
            if (response.aiMessage.content.isBlank()) {
                throw Exception("AI message content is empty")
            }
            
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
            
            // Cache messages in local database
            try {
                val userMessageEntity = MessageEntity(
                    id = userMessage.id,
                    sessionId = sessionId,
                    role = userMessage.role,
                    content = userMessage.content,
                    createdAt = userMessage.createdAt
                )
                val aiMessageEntity = MessageEntity(
                    id = aiMessage.id,
                    sessionId = sessionId,
                    role = aiMessage.role,
                    content = aiMessage.content,
                    createdAt = aiMessage.createdAt
                )
                messageDao.insertMessages(listOf(userMessageEntity, aiMessageEntity))
            } catch (e: Exception) {
                // Log caching error but don't fail the operation
                println("Failed to cache messages: ${e.message}")
            }
            
            Result.success(Pair(userMessage, aiMessage))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMessages(sessionId: String): Result<List<Message>> {
        return try {
            val cachedMessages = messageDao.getMessagesBySessionIdSync(sessionId)
            val messages = cachedMessages.map { entity ->
                Message(
                    id = entity.id,
                    role = entity.role,
                    content = entity.content,
                    createdAt = entity.createdAt
                )
            }
            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
