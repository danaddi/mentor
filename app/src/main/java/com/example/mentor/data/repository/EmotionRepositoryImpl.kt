package com.example.mentor.data.repository

import com.example.mentor.data.local.dao.EmotionDao
import com.example.mentor.data.local.entity.EmotionEntity
import com.example.mentor.data.local.TokenDataStore
import com.example.mentor.data.remote.api.MentorApiService
import com.example.mentor.data.remote.dto.SaveEmotionRequest
import com.example.mentor.domain.model.Emotion
import com.example.mentor.domain.repository.EmotionRepository
import kotlinx.coroutines.flow.first

class EmotionRepositoryImpl(
    private val apiService: MentorApiService,
    private val tokenDataStore: TokenDataStore,
    private val emotionDao: EmotionDao
) : EmotionRepository {

    override suspend fun saveEmotion(emotionType: String, intensity: Int): Result<Emotion> {
        return try {
            val token = tokenDataStore.token.first() ?: throw Exception("Not authenticated")
            val request = SaveEmotionRequest(emotionType, intensity)
            val response = apiService.saveEmotion(token, request)
            val emotion = Emotion(
                id = response.id,
                userId = response.userId,
                emotionType = response.emotionType,
                intensity = response.intensity,
                createdAt = response.createdAt
            )
            // Cache in Room
            emotionDao.insertEmotion(emotion.toEntity())
            Result.success(emotion)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun Emotion.toEntity() = EmotionEntity(
        id = id,
        userId = userId,
        emotionType = emotionType,
        intensity = intensity,
        createdAt = createdAt
    )

    private fun EmotionEntity.toDomain() = Emotion(
        id = id,
        userId = userId,
        emotionType = emotionType,
        intensity = intensity,
        createdAt = createdAt
    )
}
