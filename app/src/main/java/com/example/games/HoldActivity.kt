package com.example.games

import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.MotionEvent
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.random.Random
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager


class HoldActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var scoreText: TextView
    private lateinit var multiplierText: TextView
    private lateinit var timeText: TextView

    private var bonusTimer: CountDownTimer? = null
    private var countDownTimer: CountDownTimer? = null
    private var vibratorTimer: CountDownTimer? = null
    private var mediaPlayer: MediaPlayer? = null
    private var timeLeftInMillis: Long = 60000 // 60 seconds
    private var score = 0
    private var fingerDownTime: Long = 0L
    private var fingerDown = false
    private var malusActive = false
    private var multiplier = 1
    private var downIsPossible = true
    private var isHolding = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_hold)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        statusText = findViewById(R.id.status_text)
        scoreText = findViewById(R.id.score_text)
        multiplierText = findViewById(R.id.multiplier_text)
        timeText = findViewById(R.id.time_text)

        startTimer()
    }

    private fun startBonusPhase() {
        val duration = Random.nextLong(1500, 5000) // Durée aléatoire entre 1s et 5s
        val alertTime = Random.nextLong(500, duration - 500)
        malusActive = false

        vibratorTimer = object : CountDownTimer(alertTime, alertTime) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                vibrateWarning()
            }
        }.start()

        bonusTimer = object : CountDownTimer(duration, 500) {
            override fun onTick(millisUntilFinished: Long) {
                val elapsed = System.currentTimeMillis() - fingerDownTime
                if (fingerDown && elapsed >= 1200) {
                    score = score.plus(multiplier)
                    scoreText.text = "$score"
                    isHolding = true
                } else {
                    isHolding = false
                }
            }

            override fun onFinish() {
                downIsPossible = false
                malusActive = true
                score -= 10 * multiplier
                scoreText.text = "$score"
                multiplier = 1
                stopGame()
            }
        }.start()
    }

    private fun vibrateWarning() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(VibratorManager::class.java)
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }


    private fun startTimer() {
        val timerText = findViewById<TextView>(R.id.time_text)

        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                val secondsLeft = millisUntilFinished / 1000
                timerText.text = "$secondsLeft"
            }

            override fun onFinish() {
                downIsPossible = false
                mediaPlayer = MediaPlayer.create(this@HoldActivity, R.raw.tada)
                mediaPlayer?.start()
                stopGame()
            }
        }.start()
    }

    private fun stopGame() {
        vibratorTimer?.cancel()
        bonusTimer?.cancel()
        val message: String
        if (malusActive) {
            countDownTimer?.cancel()
            message = "Late !"
            mediaPlayer = MediaPlayer.create(this@HoldActivity, R.raw.fail)
            mediaPlayer?.start()
        } else if (downIsPossible) {
            message = "Good !"
            mediaPlayer = MediaPlayer.create(this@HoldActivity, R.raw.success)
            mediaPlayer?.start()
        } else {
            message = "Finish !"
        }
        statusText.text = message
        multiplierText.text = "x$multiplier"
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                if (downIsPossible) {
                    if (!fingerDown && !malusActive) {
                        fingerDown = true
                        fingerDownTime = System.currentTimeMillis()
                        statusText.text = "Holding !"
                        startBonusPhase()
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                if (downIsPossible && isHolding) {
                    fingerDown = false
                    multiplier++
                    stopGame()
                } else {
                    fingerDown = false
                    statusText.text = "Hold !"
                    vibratorTimer?.cancel()
                    bonusTimer?.cancel()
                }
            }
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        countDownTimer?.cancel()
        vibratorTimer?.cancel()
        bonusTimer?.cancel()
    }
}
