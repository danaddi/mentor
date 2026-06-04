package com.example.mentor.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gratitudes")
data class GratitudeEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val content: String,
    val createdAt: String
)
