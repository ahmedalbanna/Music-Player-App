package com.example.player

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.Song

class MusicService : Service() {

    private lateinit var playerManager: AudioPlayerManager

    override fun onCreate() {
        super.onCreate()
        playerManager = AudioPlayerManager.getInstance(applicationContext)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            ACTION_START -> {
                showNotification()
            }
            ACTION_PLAY_PAUSE -> {
                if (playerManager.isPlaying.value) {
                    playerManager.pause()
                } else {
                    playerManager.play()
                }
            }
            ACTION_NEXT -> {
                playerManager.next()
            }
            ACTION_PREV -> {
                playerManager.previous()
            }
            ACTION_STOP -> {
                stopForegroundService()
            }
            ACTION_UPDATE -> {
                showNotification()
            }
        }
        return START_NOT_STICKY
    }

    private fun showNotification() {
        val song = playerManager.currentSong.value ?: return
        val isPlaying = playerManager.isPlaying.value

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val playPauseIcon = if (isPlaying) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }

        val playPauseAction = NotificationCompat.Action(
            playPauseIcon,
            if (isPlaying) "توقف" else "تشغيل",
            getServicePendingIntent(ACTION_PLAY_PAUSE)
        )

        val prevAction = NotificationCompat.Action(
            android.R.drawable.ic_media_previous,
            "السابق",
            getServicePendingIntent(ACTION_PREV)
        )

        val nextAction = NotificationCompat.Action(
            android.R.drawable.ic_media_next,
            "التالي",
            getServicePendingIntent(ACTION_NEXT)
        )

        val closeAction = NotificationCompat.Action(
            android.R.drawable.ic_menu_close_clear_cancel,
            "إغلاق",
            getServicePendingIntent(ACTION_STOP)
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(song.title)
            .setContentText(song.artist)
            .setContentIntent(openPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(prevAction)
            .addAction(playPauseAction)
            .addAction(nextAction)
            .addAction(closeAction)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setOngoing(isPlaying)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notificationBuilder.build(),
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notificationBuilder.build())
        }
    }

    private fun getServicePendingIntent(action: String): PendingIntent {
        val intent = Intent(this, MusicService::class.java).apply { this.action = action }
        return PendingIntent.getService(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "مشغل الموسيقى الخفي",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "التحكم في تشغيل ملفات الصوت والموسيقى"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun stopForegroundService() {
        playerManager.pause()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "music_playback_channel_v2"
        const val NOTIFICATION_ID = 48151

        const val ACTION_START = "com.example.player.ACTION_START"
        const val ACTION_PLAY_PAUSE = "com.example.player.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "com.example.player.ACTION_NEXT"
        const val ACTION_PREV = "com.example.player.ACTION_PREV"
        const val ACTION_STOP = "com.example.player.ACTION_STOP"
        const val ACTION_UPDATE = "com.example.player.ACTION_UPDATE"
    }
}
