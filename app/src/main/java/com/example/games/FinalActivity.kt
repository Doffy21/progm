package com.example.games

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.games.bluetooth.BluetoothConnectionManager
import com.example.games.bluetooth.LobbyActivity
import java.io.IOException

class FinalActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var scoreText: TextView
    private lateinit var opponentScoreText: TextView
    private lateinit var resultText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_final)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        scoreText = findViewById(R.id.final_score_text)
        opponentScoreText = findViewById(R.id.opponent_score_text)
        resultText = findViewById(R.id.result_text)

        val myScore = intent.getIntExtra("finalScore", 0)
        val isMultiplayer = intent.hasExtra("role")
        val role = intent.getStringExtra("role")

        scoreText.text = "$myScore"

        if (!isMultiplayer || role == "train") {
            mediaPlayer = MediaPlayer.create(this@FinalActivity, R.raw.finish)
            mediaPlayer?.start()
            opponentScoreText.text = ""
            resultText.text = ""
        } else {
            if (!BluetoothConnectionManager.isConnected()) {
                resultText.text = "ERROR"
            }
            if (role == "client") {
                BluetoothConnectionManager.sendMessage("SCORE|$myScore")
                listenForOpponentScore(myScore)
            } else if (role == "host") {
                listenForClientScoreThenRespond(myScore)
            }
        }

        findViewById<Button>(R.id.restart_button).setOnClickListener {
            if (role == "client" || role == "host") {
                BluetoothConnectionManager.close()
                val intent = Intent(this, LobbyActivity::class.java)
                startActivity(intent)
                finish()
            } else if (role == "train") {
                val index = intent.getIntExtra("gameIndex", 0)
                val intent = Intent(this, allGames[index-1])
                intent.putExtra("currentScore", 0)
                intent.putExtra("gameIndex", index)
                intent.putExtra("gameList", allGames[index-1])
                intent.putExtra("role", "train")
                startActivity(intent)
                finish()
            }
            else {
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
        }

        findViewById<Button>(R.id.home_button).setOnClickListener {
            Log.d("FinalActivity", "Go Home")
            if (role == "client" || role == "host") {
                BluetoothConnectionManager.close()
            }
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun listenForOpponentScore(myScore: Int) {
        BluetoothConnectionManager.stopListening()
        BluetoothConnectionManager.listen { message ->
            if (message.startsWith("SCORE|")) {
                val opponentScore = message.removePrefix("SCORE|").toIntOrNull() ?: return@listen
                runOnUiThread {
                    displayMultiplayerResult(myScore, opponentScore)
                }
            }
        }
    }

    private fun listenForClientScoreThenRespond(myScore: Int) {
        BluetoothConnectionManager.stopListening()
        BluetoothConnectionManager.listen { message ->
            if (message.startsWith("SCORE|")) {
                val opponentScore = message.removePrefix("SCORE|").toIntOrNull() ?: return@listen
                BluetoothConnectionManager.sendMessage("SCORE|$myScore")
                runOnUiThread {
                    displayMultiplayerResult(myScore, opponentScore)
                }
            }
        }
    }

    private fun displayMultiplayerResult(myScore: Int, opponentScore: Int) {
        opponentScoreText.text = "Opponent : $opponentScore"
        if (myScore > opponentScore) {
            mediaPlayer = MediaPlayer.create(this@FinalActivity, R.raw.win)
            mediaPlayer?.start()
            resultText.text = "WIN"
        } else if (myScore < opponentScore) {
            mediaPlayer = MediaPlayer.create(this@FinalActivity, R.raw.lose)
            mediaPlayer?.start()
            resultText.text = "LOSE"
        } else {
            mediaPlayer = MediaPlayer.create(this@FinalActivity, R.raw.finish)
            mediaPlayer?.start()
            resultText.text = "DRAW"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    companion object {
        fun launch(activity: AppCompatActivity, finalScore: Int, role: String?, gameIndex: Int) {
            val intent = Intent(activity, FinalActivity::class.java)
            intent.putExtra("finalScore", finalScore)
            if (role != null) intent.putExtra("role", role)
            if (role == "train") intent.putExtra("gameIndex", gameIndex)
            activity.startActivity(intent)
            activity.finish()
        }
    }
}