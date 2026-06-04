package com.example.mentor.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val chatSessionId: String?,
    val content: String,
    val createdAt: String
)
