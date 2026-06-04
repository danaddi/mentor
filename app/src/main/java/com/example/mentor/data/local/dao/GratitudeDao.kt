package com.example.mentor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mentor.data.local.entity.GratitudeEntity

@Dao
interface GratitudeDao {
    @Query("SELECT * FROM gratitudes ORDER BY createdAt DESC")
    suspend fun getAllGratitudes(): List<GratitudeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGratitude(gratitude: GratitudeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGratitudes(gratitudes: List<GratitudeEntity>)

    @Query("DELETE FROM gratitudes")
    suspend fun deleteAllGratitudes()
}
