package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val duration: Long, // in milliseconds
    val path: String, // file path, content URI, or remote sample URL
    val isSample: Boolean = false,
    val coverAsset: String? = null // For custom drawable index or remote imageUrl
) : Serializable {
    val durationText: String
        get() {
            val totalSeconds = duration / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
}
