package com.shariaty.VazhehYar

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private var popupWindow: PopupWindow? = null
    private var isMusicMuted = false
    private var isSoundMuted = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreferences = getSharedPreferences("GamePrefs", MODE_PRIVATE)
        isMusicMuted = sharedPreferences.getBoolean("MUSIC_MUTED", false)
        isSoundMuted = sharedPreferences.getBoolean("SOUND_MUTED", false)

        val startButton: Button = findViewById(R.id.start_button)
        startButton.setOnClickListener {
            val intent = Intent(this, PartActivity::class.java)
            startActivity(intent)
        }

        // پخش موسیقی پس‌زمینه با صدای کم
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music)
        mediaPlayer.setVolume(0.1f, 0.1f) // تنظیم صدای کم
        mediaPlayer.isLooping = true // پخش در حالت تکرار
        if (!isMusicMuted && !MusicService.isPlaying) {
            startService(Intent(this, MusicService::class.java))
        }

        // تنظیمات آیکون
        val settingsLogo: ImageView = findViewById(R.id.settingsLogo)
        settingsLogo.setOnClickListener {
            if (popupWindow == null || !popupWindow!!.isShowing) {
                showPopupWindow(it)
            } else {
                popupWindow!!.dismiss()
            }
        }

        // مدیریت دکمه Back
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // غیرفعال کردن عملکرد پیش‌فرض دکمه Back
            }
        })

        val score = sharedPreferences.getInt("SCORE", 0)

        // نمایش امتیازات در صفحه اصلی
        val scoreText: TextView = findViewById(R.id.score_text)
        scoreText.text = score.toString()
    }

    @SuppressLint("InflateParams")
    private fun showPopupWindow(anchorView: View) {
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.main_menu, null)

        popupWindow = PopupWindow(popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT)

        popupWindow!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow!!.isOutsideTouchable = true
        popupWindow!!.isFocusable = true

        popupWindow!!.showAsDropDown(anchorView, 0, 0)

        val sharedPreferences = getSharedPreferences("GamePrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val soundIcon: ImageView = popupView.findViewById(R.id.sound)
        soundIcon.setImageResource(if (isSoundMuted) R.drawable.mute else R.drawable.unmute)
        soundIcon.setOnClickListener {
            isSoundMuted = !isSoundMuted
            soundIcon.setImageResource(if (isSoundMuted) R.drawable.mute else R.drawable.unmute)
            editor.putBoolean("SOUND_MUTED", isSoundMuted)
            editor.apply()
        }

        val musicIcon: ImageView = popupView.findViewById(R.id.music)
        musicIcon.setImageResource(if (isMusicMuted) R.drawable.music_off else R.drawable.music_on)
        musicIcon.setOnClickListener {
            isMusicMuted = !isMusicMuted
            musicIcon.setImageResource(if (isMusicMuted) R.drawable.music_off else R.drawable.music_on)
            val intent = Intent(this@MainActivity, MusicService::class.java)
            if (isMusicMuted) {
                stopService(intent) // توقف سرویس موسیقی
            } else {
                startService(intent) // شروع سرویس موسیقی
            }
            editor.putBoolean("MUSIC_MUTED", isMusicMuted)
            editor.apply()
        }

        // تنظیمات مربوط به آیکون‌های دیگر را اضافه کنید
    }

    override fun onDestroy() {
        super.onDestroy()
        val sharedPreferences = getSharedPreferences("GamePrefs", MODE_PRIVATE)
        if (sharedPreferences.getBoolean("MUSIC_MUTED", false)) {
            val intent = Intent(this, MusicService::class.java)
            stopService(intent) // توقف سرویس موسیقی
        }
    }
}
