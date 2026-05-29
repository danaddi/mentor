package com.example.mentor.domain.model

data class User(
    val userId: String,
    val token: String
)

data class ChatSession(
    val id: String,
    val sectionType: String,
    val createdAt: String
)

data class Message(
    val id: String,
    val role: String,
    val content: String,
    val createdAt: String
)

data class Note(
    val id: String,
    val userId: String,
    val chatSessionId: String?,
    val content: String,
    val createdAt: String
)

data class Emotion(
    val id: String,
    val userId: String,
    val emotionType: String,
    val intensity: Int,
    val createdAt: String
)

data class GratitudeEntry(
    val id: String,
    val userId: String,
    val content: String,
    val createdAt: String
)

enum class EmotionType(val displayName: String, val apiName: String) {
    JOY("Радость", "joy"),
    SADNESS("Грусть", "sadness"),
    ANGER("Злость", "anger"),
    FEAR("Страх", "fear"),
    SURPRISE("Удивление", "surprise"),
    DISGUST("Отвращение", "disgust"),
    NEUTRAL("Нейтральное", "neutral")
}
