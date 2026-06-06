package com.example.data.repository

import android.content.ContentResolver
import android.content.Context
import android.provider.MediaStore
import com.example.data.FavoriteSong
import com.example.data.Song
import com.example.data.database.SongDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SongRepository(
    private val context: Context,
    private val songDao: SongDao
) {
    val favoritesFlow: Flow<List<FavoriteSong>> = songDao.getAllFavorites()

    suspend fun insertFavorite(songId: String) {
        songDao.insertFavorite(FavoriteSong(songId))
    }

    suspend fun removeFavorite(songId: String) {
        songDao.deleteFavorite(songId)
    }

    fun isFavorite(songId: String): Flow<Boolean> {
        return songDao.isFavoriteFlow(songId)
    }

    // High fidelity sample tracks to make the application immediately fully usable!
    val sampleSongs = listOf(
        Song(
            id = "sample_1",
            title = "سمفونية الأمل",
            artist = "معزوفات هادئة",
            duration = 302000, // 5:02
            path = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            isSample = true,
            coverAsset = "1"
        ),
        Song(
            id = "sample_2",
            title = "لحن الغروب",
            artist = "نبض المساء",
            duration = 425000, // 7:05
            path = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            isSample = true,
            coverAsset = "2"
        ),
        Song(
            id = "sample_3",
            title = "نبض الطبيعة",
            artist = "صدى الغاب",
            duration = 302000, // 5:02
            path = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
            isSample = true,
            coverAsset = "3"
        ),
        Song(
            id = "sample_4",
            title = "صدى الفضاء",
            artist = "عازف النجوم",
            duration = 318000, // 5:18
            path = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
            isSample = true,
            coverAsset = "4"
        )
    )

    suspend fun loadDeviceSongs(): List<Song> = withContext(Dispatchers.IO) {
        val songsList = mutableListOf<Song>()
        val contentResolver: ContentResolver = context.contentResolver
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
        
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA
        )

        try {
            contentResolver.query(uri, projection, selection, null, sortOrder)?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn).toString()
                    val title = cursor.getString(titleColumn) ?: "أغنية غير معروفة"
                    val artist = cursor.getString(artistColumn) ?: "فنان غير معروف"
                    val duration = cursor.getLong(durationColumn)
                    val path = cursor.getString(dataColumn) ?: ""

                    songsList.add(
                        Song(
                            id = id,
                            title = title,
                            artist = artist,
                            duration = if (duration > 0) duration else 180000,
                            path = path,
                            isSample = false
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        songsList
    }
}
