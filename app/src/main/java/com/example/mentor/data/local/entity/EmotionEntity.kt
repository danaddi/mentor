package com.example.mentor.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emotions")
data class EmotionEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val emotionType: String,
    val intensity: Int,
    val createdAt: String
)
