package com.example.games

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class TrainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_train)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.swipe_button).setOnClickListener {
            val intent = Intent(this, allGames[0])
            intent.putExtra("currentScore", 0)
            intent.putExtra("gameIndex", 1)
            intent.putExtra("gameList", allGames[0])
            intent.putExtra("role", "train")
            startActivity(intent)
        }

        findViewById<Button>(R.id.sound_button).setOnClickListener {
            val intent = Intent(this, allGames[1])
            intent.putExtra("currentScore", 0)
            intent.putExtra("gameIndex", 2)
            intent.putExtra("gameList", allGames[1])
            intent.putExtra("role", "train")
            startActivity(intent)
        }

        findViewById<Button>(R.id.hold_button).setOnClickListener {
            val intent = Intent(this, allGames[2])
            intent.putExtra("currentScore", 0)
            intent.putExtra("gameIndex", 3)
            intent.putExtra("gameList", allGames[2])
            intent.putExtra("role", "train")
            startActivity(intent)
        }

        findViewById<Button>(R.id.sunny_button).setOnClickListener {
            val intent = Intent(this, allGames[3])
            intent.putExtra("currentScore", 0)
            intent.putExtra("gameIndex", 4)
            intent.putExtra("gameList", allGames[3])
            intent.putExtra("role", "train")
            startActivity(intent)
        }

        findViewById<Button>(R.id.qr_button).setOnClickListener {
            val intent = Intent(this, allGames[4])
            intent.putExtra("currentScore", 0)
            intent.putExtra("gameIndex", 5)
            intent.putExtra("gameList", allGames[4])
            intent.putExtra("role", "train")
            startActivity(intent)
        }

        findViewById<Button>(R.id.qcm_button).setOnClickListener {
            val intent = Intent(this, allGames[5])
            intent.putExtra("currentScore", 0)
            intent.putExtra("gameIndex", 6)
            intent.putExtra("gameList", allGames[5])
            intent.putExtra("role", "train")
            startActivity(intent)
        }

    }
}