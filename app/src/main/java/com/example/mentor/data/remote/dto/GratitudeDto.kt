package com.example.mentor.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SaveGratitudeRequest(
    val content: String
)

@Serializable
data class GratitudeResponse(
    val id: String,
    val userId: String,
    val content: String,
    val createdAt: String
)
