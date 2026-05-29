package com.example.mentor.domain.usecase

import com.example.mentor.domain.model.ChatSession
import com.example.mentor.domain.repository.ChatRepository
import javax.inject.Inject

class CreateChatSessionUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(sectionType: String): Result<ChatSession> {
        return chatRepository.createSession(sectionType)
    }
}
