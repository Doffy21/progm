package com.example.games

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class FinalActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_final)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mediaPlayer = MediaPlayer.create(this@FinalActivity, R.raw.finish)
        mediaPlayer?.start()

        val finalScore = intent.getIntExtra("finalScore", 0)
        findViewById<TextView>(R.id.final_score_text).text = "$finalScore"
        findViewById<Button>(R.id.restart_button).setOnClickListener {
            allGames.shuffle()
            val selectedGames = ArrayList(allGames.take(3))
            val firstGame = selectedGames[0]
            val intent = Intent(this, firstGame)
            intent.putExtra("currentScore", 0)
            intent.putExtra("gameIndex", 1) // 1 car on va à la 2e après
            intent.putExtra("gameList", selectedGames)
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.home_button).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}