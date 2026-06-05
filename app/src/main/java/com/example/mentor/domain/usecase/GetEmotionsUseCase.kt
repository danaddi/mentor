package com.example.mentor.domain.usecase

import com.example.mentor.domain.model.Emotion
import com.example.mentor.domain.repository.EmotionRepository
import javax.inject.Inject

class GetEmotionsUseCase @Inject constructor(
    private val emotionRepository: EmotionRepository
) {
    suspend operator fun invoke(): Result<List<Emotion>> {
        return emotionRepository.getEmotions()
    }

    suspend fun getByDateRange(startDate: String, endDate: String): List<Emotion> {
        return emotionRepository.getEmotionsByDateRange(startDate, endDate)
    }
}
