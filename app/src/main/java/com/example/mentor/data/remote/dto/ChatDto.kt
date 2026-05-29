package com.example.mentor.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateSessionRequest(
    val sectionType: String
)

@Serializable
data class CreateSessionResponse(
    val sessionId: String,
    val sectionType: String,
    val createdAt: String
)

@Serializable
data class SessionResponse(
    val id: String,
    val sectionType: String,
    val createdAt: String
)

@Serializable
data class SendMessageRequest(
    val content: String
)

@Serializable
data class MessageDto(
    val id: String,
    val role: String,
    val content: String,
    val createdAt: String
)

@Serializable
data class SendMessageResponse(
    val userMessage: MessageDto,
    val aiMessage: MessageDto
)
