package com.shariaty.VazhehYar

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import java.io.InputStreamReader

class GameActivity : AppCompatActivity() {
    private var lives = 5
    private var score = 0
    private lateinit var correctWord: String
    private lateinit var scrambledLetters: List<Char>
    private lateinit var heartImageViews: List<ImageView>
    private lateinit var underscoreLayout: LinearLayout
    private lateinit var lettersGrid: GridLayout
    private lateinit var correctSoundPlayer: MediaPlayer
    private lateinit var wrongSoundPlayer: MediaPlayer
    private lateinit var winSoundPlayer: MediaPlayer
    private lateinit var loseSoundPlayer: MediaPlayer
    private lateinit var scoreIcon: ImageView
    private var isSoundMuted = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        heartImageViews = listOf(
            findViewById(R.id.heart1),
            findViewById(R.id.heart2),
            findViewById(R.id.heart3),
            findViewById(R.id.heart4),
            findViewById(R.id.heart5)
        )

        underscoreLayout = findViewById(R.id.underscore_layout)
        lettersGrid = findViewById(R.id.letters_grid)

        val sharedPreferences = getSharedPreferences("GamePrefs", MODE_PRIVATE)
        isSoundMuted = sharedPreferences.getBoolean("SOUND_MUTED", false)
        score = sharedPreferences.getInt("SCORE", 0)
        val isMusicMuted = sharedPreferences.getBoolean("MUSIC_MUTED", false)
        if (!isMusicMuted && !MusicService.isPlaying) {
            startService(Intent(this, MusicService::class.java))
        }

        val scoreText: TextView = findViewById(R.id.score_text)
        scoreText.text = score.toString()
        scoreIcon = findViewById(R.id.score_logo)
        scoreIcon.setOnClickListener {
            showHintDialog()
        }

        correctSoundPlayer = MediaPlayer.create(this, R.raw.correct_sound)
        wrongSoundPlayer = MediaPlayer.create(this, R.raw.wrong_sound)
        winSoundPlayer = MediaPlayer.create(this, R.raw.correct_sound) // صدای برنده شدن
        loseSoundPlayer = MediaPlayer.create(this, R.raw.wrong_sound) // صدای باختن

        correctSoundPlayer.setVolume(if (isSoundMuted) 0f else 1f, if (isSoundMuted) 0f else 1f)
        wrongSoundPlayer.setVolume(if (isSoundMuted) 0f else 1f, if (isSoundMuted) 0f else 1f)
        winSoundPlayer.setVolume(if (isSoundMuted) 0f else 1f, if (isSoundMuted) 0f else 1f)
        loseSoundPlayer.setVolume(if (isSoundMuted) 0f else 1f, if (isSoundMuted) 0f else 1f)

        loadWords()
        setupUnderscoreLayout()
        setupScrambledLetters()

        val submitButton: Button = findViewById(R.id.submit_button)
        submitButton.setOnClickListener {
            checkWord()
        }
        val homeIcon: ImageView = findViewById(R.id.home_icon)
        homeIcon.setOnClickListener {
            showConfirmationDialog()
        }
        isSoundMuted = intent.getBooleanExtra("IS_SOUND_MUTED", false)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmationDialog()
            }
        })
    }

    private fun showExitConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("آیا مطمئن هستید که می‌خواهید از مرحله خارج شوید؟")
            .setPositiveButton("بله") { dialog, id ->
                val intent = Intent(this, LevelsActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("خیر") { dialog, id ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

    private fun showHintDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("آیا نیاز به راهنمایی دارید؟ با کسر 15 امتیاز می‌توانید یک راهنمایی دریافت کنید.")
            .setPositiveButton("بله") { dialog, id ->
                val sharedPreferences = getSharedPreferences("GamePrefs", MODE_PRIVATE)
                if (score >= 15) {
                    score -= 15
                    updateScoreText()
                    revealRandomLetter()
                    // ذخیره امتیاز جدید
                    saveScore(sharedPreferences.edit())
                } else {
                    Toast.makeText(this, "امتیاز کافی ندارید!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("خیر") { dialog, id ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }
    private fun revealRandomLetter() {
        val availableIndexes = mutableListOf<Int>()
        for (i in correctWord.indices) {
            val textView = underscoreLayout.getChildAt(i) as TextView
            if (textView.text == "  _  ") {
                availableIndexes.add(i)
            }
        }

        if (availableIndexes.isNotEmpty()) {
            // انتخاب یک ایندکس به صورت رندم
            val randomIndex = availableIndexes.random()

            // قرار دادن حرف رندم در جای درست از راست به چپ
            val textView = underscoreLayout.getChildAt(correctWord.length - 1 - randomIndex) as TextView
            textView.text = correctWord[randomIndex].toString()
        }
    }


    private fun loadWords() {
        val inputStream = assets.open("words.json")
        val json = InputStreamReader(inputStream).use { it.readText() }
        val gson = Gson()
        val partsData = gson.fromJson(json, PartsData::class.java)

        val currentPartName = intent.getStringExtra("PART_NAME") ?: ""
        val currentLevel = intent.getIntExtra("LEVEL", 1)

        val partData = partsData.parts.firstOrNull { it.name == currentPartName }
        val levelData = partData?.levels?.firstOrNull { it.level == currentLevel }

        if (levelData != null) {
            correctWord = levelData.words.random()
            scrambledLetters = correctWord.toList().shuffled()
        }
    }

    private fun setupUnderscoreLayout() {
        for (i in correctWord.indices) {
            val textView = TextView(this)
            textView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            textView.text = "  _  "
            textView.textSize = 30f
            underscoreLayout.addView(textView)
        }
    }

    private fun setupScrambledLetters() {
        for (letter in scrambledLetters) {
            val button = Button(this)
            button.layoutParams = GridLayout.LayoutParams().apply {
                width = GridLayout.LayoutParams.WRAP_CONTENT
                height = GridLayout.LayoutParams.WRAP_CONTENT
                setMargins(8, 8, 8, 8)
            }
            button.text = letter.toString()
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.fall))
            button.setOnClickListener {
                placeLetterInUnderscore(button.text[0])
                button.isEnabled = false
            }
            lettersGrid.addView(button)
        }
    }

    private fun placeLetterInUnderscore(letter: Char) {
        for (i in underscoreLayout.childCount - 1 downTo 0) {
            val textView = underscoreLayout.getChildAt(i) as TextView
            if (textView.text == "  _  ") {
                textView.text = letter.toString()
                break
            }
        }
    }

    private fun checkWord() {
        val builtWord = StringBuilder()
        for (i in 0 until underscoreLayout.childCount) {
            val textView = underscoreLayout.getChildAt(i) as TextView
            builtWord.append(textView.text)
        }
        val builtWordReversed = builtWord.toString().reversed()
        if (builtWordReversed == correctWord) {
            if (!isSoundMuted) correctSoundPlayer.start()
            val sharedPreferences = getSharedPreferences("GamePrefs", MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            val currentLevel = intent.getIntExtra("LEVEL", 1)
            val currentPartName = intent.getStringExtra("PART_NAME") ?: ""
            val levelKey = "LEVEL_WON_${currentPartName}_$currentLevel"
            val levelWon = sharedPreferences.getBoolean(levelKey, false)
            if (!levelWon) {
                score += 5
                editor.putBoolean(levelKey, true)
                unlockNextLevel(currentPartName, currentLevel)
            }
            saveScore(editor)
            updateScoreText()

            if (areAllLevelsComplete(currentPartName)) {

                val intent = Intent(this@GameActivity, PartActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                showWinDialog()
            }
        } else {
            if (!isSoundMuted) wrongSoundPlayer.start()
            lives--
            updateLives()
            if (lives == 0) {
                showLoseDialog()
            } else {
                resetGameForRetry()
                Toast.makeText(this, "کلمه اشتباه است، دوباره امتحان کنید.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun areAllLevelsComplete(partName: String): Boolean {
        val sharedPreferences = getSharedPreferences("GamePrefs", MODE_PRIVATE)
        val partsData = loadPartsData()
        val partData = partsData.parts.firstOrNull { it.name == partName }
        return partData?.levels?.all { level ->
            sharedPreferences.getBoolean("LEVEL_WON_${partName}_${level.level}", false)
        } ?: false
    }

    private fun loadPartsData(): PartsData {
        val inputStream = assets.open("words.json")
        val json = InputStreamReader(inputStream).use { it.readText() }
        return Gson().fromJson(json, PartsData::class.java)
    }




    private fun updateLives() {
        for (i in 0 until heartImageViews.size) {
            heartImageViews[i].setImageResource(if (i < lives) R.drawable.red_heart else R.drawable.black_heart)
        }
    }

    private fun resetGameForRetry() {
        underscoreLayout.removeAllViews()
        setupUnderscoreLayout()
        lettersGrid.removeAllViews()
        setupScrambledLetters()
    }

    private fun showLoseDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("متاسفانه بازی را باختید!")
            .setPositiveButton("امتحان دوباره") { dialog, id ->
                if (!isSoundMuted) loseSoundPlayer.start()
                resetGameForRetry()
                lives = 5
                updateLives()
            }
            .setNegativeButton("بازگشت به صفحه اصلی") { dialog, id ->
                val intent = Intent(this@GameActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        val alert = builder.create()
        alert.setCancelable(false)
        alert.show()
    }

    private fun showWinDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("آفرین! کلمه درست است.")
            .setPositiveButton("مرحله بعدی") { dialog, id ->
                if (!isSoundMuted) winSoundPlayer.start()
                val currentLevel = intent.getIntExtra("LEVEL", 1)
                val nextLevel = currentLevel + 1
                val partName = intent.getStringExtra("PART_NAME") ?: ""
                val intent = Intent(this@GameActivity, GameActivity::class.java)
                intent.putExtra("LEVEL", nextLevel)
                intent.putExtra("PART_NAME", partName)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("بازگشت به صفحه اصلی") { dialog, id ->
                val intent = Intent(this@GameActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        val alert = builder.create()
        alert.setCancelable(false)
        alert.show()
    }

    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("آیا مطمئن هستید که می‌خواهید به صفحه اول بازگردید؟")
            .setPositiveButton("بله") { dialog, id ->
                val intent = Intent(this@GameActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("خیر") { dialog, id ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

    private fun unlockNextLevel(partName: String, currentLevel: Int) {
        val sharedPreferences = getSharedPreferences("GamePrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val nextLevel = currentLevel + 1
        editor.putBoolean("LEVEL_${partName}_${nextLevel}", true)
        editor.apply()
    }

    private fun saveScore(editor: SharedPreferences.Editor) {
        editor.putInt("SCORE", score)
        editor.apply()
    }

    @SuppressLint("SetTextI18n")
    private fun updateScoreText() {
        val scoreText: TextView = findViewById(R.id.score_text)
        scoreText.text = score.toString()
    }
    override fun onDestroy() {
        super.onDestroy()
        // در صورت فعال بودن بی‌صدا، سرویس متوقف شود
        val sharedPreferences = getSharedPreferences("GamePrefs", MODE_PRIVATE)
        if (sharedPreferences.getBoolean("MUSIC_MUTED", false)) {
            stopService(Intent(this, MusicService::class.java))
        }
    }


}
