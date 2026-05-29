package com.example.mentor.domain.usecase

import com.example.mentor.domain.model.Emotion
import com.example.mentor.domain.repository.EmotionRepository
import javax.inject.Inject

class SaveEmotionUseCase @Inject constructor(
    private val emotionRepository: EmotionRepository
) {
    suspend operator fun invoke(emotionType: String, intensity: Int): Result<Emotion> {
        return emotionRepository.saveEmotion(emotionType, intensity)
    }
}
