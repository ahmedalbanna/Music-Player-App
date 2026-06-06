package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Song
import com.example.data.database.AppDatabase
import com.example.data.repository.SongRepository
import com.example.player.AudioPlayerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MusicPlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = SongRepository(application, database.songDao())
    private val playerManager = AudioPlayerManager.getInstance(application)

    // Core playback states directly reflecting background service ExoPlayer
    val isPlaying = playerManager.isPlaying
    val currentSong = playerManager.currentSong
    val playlist = playerManager.playlist
    val currentPosition = playerManager.currentPosition
    val duration = playerManager.duration
    val isShuffleMode = playerManager.isShuffleMode
    val isRepeatMode = playerManager.isRepeatMode

    // Device songs state
    private val _deviceSongs = MutableStateFlow<List<Song>>(emptyList())
    val deviceSongs = _deviceSongs.asStateFlow()

    // Preloaded samples
    val sampleSongs = repository.sampleSongs

    // Favorites flow synced with database
    val favorites = repository.favoritesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0 for Songs, 1 for Favorites
    val selectedTab = _selectedTab.asStateFlow()

    init {
        loadDeviceMusic()
    }

    fun loadDeviceMusic() {
        viewModelScope.launch {
            val songs = repository.loadDeviceSongs()
            _deviceSongs.value = songs
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            val isAlreadyFav = favorites.value.any { it.songId == song.id }
            if (isAlreadyFav) {
                repository.removeFavorite(song.id)
            } else {
                repository.insertFavorite(song.id)
            }
        }
    }

    fun playSong(songsList: List<Song>, songIndex: Int) {
        playerManager.setPlaylist(songsList, songIndex)
    }

    fun togglePlayPause() {
        if (isPlaying.value) {
            playerManager.pause()
        } else {
            playerManager.play()
        }
    }

    fun next() {
        playerManager.next()
    }

    fun previous() {
        playerManager.previous()
    }

    fun seekTo(positionMs: Long) {
        playerManager.seekTo(positionMs)
    }

    fun toggleShuffle() {
        playerManager.toggleShuffle()
    }

    fun toggleRepeat() {
        playerManager.toggleRepeat()
    }
}
