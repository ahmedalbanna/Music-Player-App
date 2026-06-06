package com.example.player

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@OptIn(UnstableApi::class)
class AudioPlayerManager(private val context: Context) : Player.Listener {

    private var exoPlayer: ExoPlayer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _playlist = MutableStateFlow<List<Song>>(emptyList())
    val playlist: StateFlow<List<Song>> = _playlist.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _isShuffleMode = MutableStateFlow(false)
    val isShuffleMode: StateFlow<Boolean> = _isShuffleMode.asStateFlow()

    private val _isRepeatMode = MutableStateFlow(false)
    val isRepeatMode: StateFlow<Boolean> = _isRepeatMode.asStateFlow()

    private var audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    private val progressRunnable = object : Runnable {
        override fun run() {
            exoPlayer?.let {
                _currentPosition.value = it.currentPosition
                _duration.value = it.duration.coerceAtLeast(0L)
                if (it.isPlaying) {
                    mainHandler.postDelayed(this, 1000)
                }
            }
        }
    }

    init {
        mainHandler.post {
            initializePlayer()
        }
    }

    private fun initializePlayer() {
        if (exoPlayer != null) return
        exoPlayer = ExoPlayer.Builder(context).build().apply {
            addListener(this@AudioPlayerManager)
            repeatMode = Player.REPEAT_MODE_OFF
            shuffleModeEnabled = false
        }
    }

    fun setPlaylist(songs: List<Song>, startIndex: Int = 0) {
        _playlist.value = songs
        mainHandler.post {
            initializePlayer()
            exoPlayer?.let { player ->
                player.clearMediaItems()
                val mediaItems = songs.map { song ->
                    MediaItem.fromUri(song.path)
                }
                player.addMediaItems(mediaItems)
                if (startIndex in songs.indices) {
                    _currentSong.value = songs[startIndex]
                    player.seekTo(startIndex, 0L)
                    play()
                }
            }
        }
    }

    fun play() {
        if (requestAudioFocus()) {
            mainHandler.post {
                initializePlayer()
                exoPlayer?.let {
                    it.prepare()
                    it.play()
                    _isPlaying.value = true
                    startService()
                    mainHandler.post(progressRunnable)
                }
            }
        }
    }

    fun pause() {
        mainHandler.post {
            exoPlayer?.let {
                it.pause()
                _isPlaying.value = false
                mainHandler.removeCallbacks(progressRunnable)
                updateServiceNotification()
            }
        }
    }

    fun stop() {
        mainHandler.post {
            exoPlayer?.let {
                it.stop()
                _isPlaying.value = false
                mainHandler.removeCallbacks(progressRunnable)
                stopService()
            }
        }
    }

    fun next() {
        mainHandler.post {
            exoPlayer?.let {
                if (it.hasNextMediaItem()) {
                    it.seekToNextMediaItem()
                    val nextIndex = it.currentMediaItemIndex
                    if (nextIndex in _playlist.value.indices) {
                        _currentSong.value = _playlist.value[nextIndex]
                    }
                    play()
                } else if (_isRepeatMode.value) {
                    it.seekTo(0, 0L)
                    _currentSong.value = _playlist.value.firstOrNull()
                    play()
                }
            }
        }
    }

    fun previous() {
        mainHandler.post {
            exoPlayer?.let {
                if (it.hasPreviousMediaItem()) {
                    it.seekToPreviousMediaItem()
                    val prevIndex = it.currentMediaItemIndex
                    if (prevIndex in _playlist.value.indices) {
                        _currentSong.value = _playlist.value[prevIndex]
                    }
                    play()
                } else {
                    it.seekTo(0L)
                }
            }
        }
    }

    fun seekTo(positionMs: Long) {
        mainHandler.post {
            exoPlayer?.let {
                it.seekTo(positionMs)
                _currentPosition.value = positionMs
            }
        }
    }

    fun toggleShuffle() {
        val nextMode = !_isShuffleMode.value
        _isShuffleMode.value = nextMode
        mainHandler.post {
            exoPlayer?.shuffleModeEnabled = nextMode
        }
    }

    fun toggleRepeat() {
        val nextMode = !_isRepeatMode.value
        _isRepeatMode.value = nextMode
        mainHandler.post {
            exoPlayer?.repeatMode = if (nextMode) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
        }
    }

    private fun requestAudioFocus(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener { focusChange ->
                    when (focusChange) {
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> pause()
                        AudioManager.AUDIOFOCUS_GAIN -> play()
                        AudioManager.AUDIOFOCUS_LOSS -> pause()
                    }
                }
                .build()
            return audioManager.requestAudioFocus(audioFocusRequest!!) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            return audioManager.requestAudioFocus(
                { focusChange ->
                    when (focusChange) {
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> pause()
                        AudioManager.AUDIOFOCUS_GAIN -> play()
                        AudioManager.AUDIOFOCUS_LOSS -> pause()
                    }
                },
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
        if (isPlaying) {
            mainHandler.post(progressRunnable)
            startService()
        } else {
            mainHandler.removeCallbacks(progressRunnable)
            updateServiceNotification()
        }
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        exoPlayer?.let {
            val currentIndex = it.currentMediaItemIndex
            if (currentIndex in _playlist.value.indices) {
                _currentSong.value = _playlist.value[currentIndex]
            }
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        error.printStackTrace()
    }

    private fun startService() {
        val intent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_START
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    private fun updateServiceNotification() {
        val intent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_UPDATE
        }
        context.startService(intent)
    }

    private fun stopService() {
        val intent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_STOP
        }
        context.startService(intent)
    }

    companion object {
        @Volatile
        private var INSTANCE: AudioPlayerManager? = null

        fun getInstance(context: Context): AudioPlayerManager {
            return INSTANCE ?: synchronized(this) {
                val instance = AudioPlayerManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
