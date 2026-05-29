package com.example.mentor.domain.usecase

import com.example.mentor.domain.model.Message
import com.example.mentor.domain.repository.ChatRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(sessionId: String, content: String): Result<Pair<Message, Message>> {
        return chatRepository.sendMessage(sessionId, content)
    }
}
