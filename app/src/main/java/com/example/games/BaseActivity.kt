package com.example.games

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.games.bluetooth.BluetoothConnectionManager

abstract class BaseActivity : AppCompatActivity() {
    protected var globalScore: Int = 0
    protected var gameIndex: Int = 0
    protected var role: String? = null
    protected lateinit var gameList: ArrayList<Class<out AppCompatActivity>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        globalScore = intent.getIntExtra("currentScore", 0)
        gameIndex = intent.getIntExtra("gameIndex", 0)
        role = intent.getStringExtra("role")
        @Suppress("UNCHECKED_CAST", "DEPRECATION")
        gameList =
            intent.getSerializableExtra("gameList") as? ArrayList<Class<out AppCompatActivity>>
                ?: arrayListOf()
    }

    protected fun goToNextGame(finalScore: Int) {
        if (gameIndex >= gameList.size) {
            FinalActivity.launch(this, finalScore, role, gameIndex)
        } else {
            val nextIntent = Intent(this, gameList[gameIndex])
            nextIntent.putExtra("currentScore", finalScore)
            nextIntent.putExtra("gameIndex", gameIndex + 1)
            nextIntent.putExtra("gameList", gameList)
            role?.let { nextIntent.putExtra("role", it) }
            startActivity(nextIntent)
            finish()
        }
    }
}