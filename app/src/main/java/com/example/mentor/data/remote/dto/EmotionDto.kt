package com.example.mentor.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SaveEmotionRequest(
    val emotionType: String,
    val intensity: Int
)

@Serializable
data class EmotionResponse(
    val id: String,
    val userId: String,
    val emotionType: String,
    val intensity: Int,
    val createdAt: String
)
