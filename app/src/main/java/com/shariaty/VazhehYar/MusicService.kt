package com.shariaty.VazhehYar

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder

class MusicService : Service() {

    companion object {
        var isPlaying: Boolean = false
    }

    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music)
        mediaPlayer.isLooping = true
        mediaPlayer.setVolume(0.1f, 0.1f)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mediaPlayer.start()
        isPlaying = true
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
        isPlaying = false
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    fun pauseMusic() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            isPlaying = false
        }
    }

    fun resumeMusic() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
            isPlaying = true
        }
    }
}
