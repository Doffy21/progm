package com.example.games

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {
    protected var globalScore: Int = 0
    protected var gameIndex: Int = 0
    protected lateinit var gameList: ArrayList<Class<out AppCompatActivity>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        globalScore = intent.getIntExtra("currentScore", 0)
        gameIndex = intent.getIntExtra("gameIndex", 0)
        @Suppress("UNCHECKED_CAST", "DEPRECATION")
        gameList =
            intent.getSerializableExtra("gameList") as? ArrayList<Class<out AppCompatActivity>>
                ?: arrayListOf()
    }

    protected fun goToNextGame(finalScore: Int) {
        if (gameIndex >= gameList.size) {
            val intent = Intent(this, FinalActivity::class.java)
            intent.putExtra("finalScore", finalScore)
            startActivity(intent)
            finish()
        } else {
            val nextIntent = Intent(this, gameList[gameIndex])
            nextIntent.putExtra("currentScore", finalScore)
            nextIntent.putExtra("gameIndex", gameIndex + 1)
            nextIntent.putExtra("gameList", gameList)
            startActivity(nextIntent)
            finish()
        }
    }
}