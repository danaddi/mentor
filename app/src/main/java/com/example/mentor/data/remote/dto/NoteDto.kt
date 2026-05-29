package com.example.mentor.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateNoteRequest(
    val content: String,
    val chatSessionId: String? = null
)

@Serializable
data class NoteResponse(
    val id: String,
    val userId: String,
    val chatSessionId: String? = null,
    val content: String,
    val createdAt: String
)
