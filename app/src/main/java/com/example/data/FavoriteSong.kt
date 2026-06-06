package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteSong(
    @PrimaryKey val songId: String,
    val addedAt: Long = System.currentTimeMillis()
)
