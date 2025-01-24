package com.shariaty.VazhehYar

import android.content.Context

object GameData {
    var currentLevel: Int = 1
    var lives: Int = 5

    fun saveProgress(context: Context) {
        val sharedPreferences = context.getSharedPreferences("game_data", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("current_level", currentLevel)
        editor.putInt("lives", lives)
        editor.apply()
    }

    fun loadProgress(context: Context) {
        val sharedPreferences = context.getSharedPreferences("game_data", Context.MODE_PRIVATE)
        currentLevel = sharedPreferences.getInt("current_level", 1)
        lives = sharedPreferences.getInt("lives", 5)
    }
}
