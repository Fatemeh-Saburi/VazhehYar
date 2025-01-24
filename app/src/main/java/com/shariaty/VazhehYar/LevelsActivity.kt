package com.shariaty.VazhehYar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class LevelsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_levels)

        val sharedPreferences = getSharedPreferences("GamePrefs", MODE_PRIVATE)
        val partName = intent.getStringExtra("PART_NAME") ?: ""
        val isMusicMuted = sharedPreferences.getBoolean("MUSIC_MUTED", false)

        if (!isMusicMuted && !MusicService.isPlaying) {
            startService(Intent(this, MusicService::class.java))
        }
        val levelButtons: List<Button> = listOf(
            findViewById(R.id.level1),
            findViewById(R.id.level2),
            findViewById(R.id.level3),
            findViewById(R.id.level4),
            findViewById(R.id.level5),
            findViewById(R.id.level6),
            findViewById(R.id.level7),
            findViewById(R.id.level8),
            findViewById(R.id.level9),
            findViewById(R.id.level10)
        )

        for ((index, button) in levelButtons.withIndex()) {
            val level = index + 1
            val isUnlocked = sharedPreferences.getBoolean("LEVEL_${partName}_$level", level == 1)
            button.isEnabled = isUnlocked

            button.setOnClickListener {
                if (isUnlocked) {
                    val intent = Intent(this, GameActivity::class.java)
                    intent.putExtra("PART_NAME", partName)
                    intent.putExtra("LEVEL", level)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "این مرحله هنوز باز نشده است!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@LevelsActivity, PartActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        })
    }
}
