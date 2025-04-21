package com.example.games

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.games.bluetooth.LobbyActivity

val allGames = arrayListOf(
    SwipeActivity::class.java,
    SoundActivity::class.java,
    HoldActivity::class.java,
    SunActivity::class.java,
    QrActivity::class.java,
    QcmActivity::class.java
)

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_layout)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.solo_button).setOnClickListener {
            allGames.shuffle()
            val selectedGames = ArrayList(allGames.take(3))
            val firstGame = selectedGames[0]
            val intent = Intent(this, firstGame)
            intent.putExtra("currentScore", 0)
            intent.putExtra("gameIndex", 1) // 1 car on va à la 2e après
            intent.putExtra("gameList", selectedGames)
            startActivity(intent)
        }

        findViewById<Button>(R.id.multi_button).setOnClickListener {
            val intent = Intent(this, LobbyActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.train_button).setOnClickListener {
            val intent = Intent(this, TrainActivity::class.java)
            startActivity(intent)
        }
    }

}