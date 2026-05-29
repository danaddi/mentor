package com.example.mentor.domain.repository

import com.example.mentor.domain.model.ChatSession
import com.example.mentor.domain.model.Message

interface ChatRepository {
    suspend fun createSession(sectionType: String): Result<ChatSession>
    suspend fun getSessions(): Result<List<ChatSession>>
    suspend fun sendMessage(sessionId: String, content: String): Result<Pair<Message, Message>>
}
