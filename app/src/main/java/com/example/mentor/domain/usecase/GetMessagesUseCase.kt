package com.example.mentor.domain.usecase

import com.example.mentor.domain.model.Message
import com.example.mentor.domain.repository.ChatRepository
import javax.inject.Inject

class GetMessagesUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(sessionId: String): Result<List<Message>> {
        return chatRepository.getMessages(sessionId)
    }
}
