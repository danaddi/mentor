package com.example.mentor.domain.repository

import com.example.mentor.domain.model.Emotion

interface EmotionRepository {
    suspend fun saveEmotion(emotionType: String, intensity: Int): Result<Emotion>
}
