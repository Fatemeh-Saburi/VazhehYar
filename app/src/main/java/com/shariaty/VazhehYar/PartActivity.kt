package com.shariaty.VazhehYar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class PartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_part)

        val sharedPreferences = getSharedPreferences("GamePrefs", MODE_PRIVATE)
        val isMusicMuted = sharedPreferences.getBoolean("MUSIC_MUTED", false)

        if (!isMusicMuted && !MusicService.isPlaying) {
            startService(Intent(this, MusicService::class.java))
        }
        val partButtons: List<Pair<Button, String>> = listOf(
            Pair(findViewById(R.id.animals), "animals"),
            Pair(findViewById(R.id.books), "books"),
            Pair(findViewById(R.id.movie), "movie"),
            Pair(findViewById(R.id.music), "music"),
            Pair(findViewById(R.id.jobs), "jobs"),
            Pair(findViewById(R.id.flower), "flower"),
            Pair(findViewById(R.id.sport), "sport"),
            Pair(findViewById(R.id.tecno), "tecno"),
            Pair(findViewById(R.id.animation), "animation"),
            Pair(findViewById(R.id.food), "food")

        )

        for ((button, partName) in partButtons) {
            button.setOnClickListener {
                val intent = Intent(this, LevelsActivity::class.java)
                intent.putExtra("PART_NAME", partName)
                startActivity(intent)
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@PartActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        })
    }
}
