package com.example.games

import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.abs

class SwipeActivity : BaseActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var numberGameText: TextView
    private lateinit var directionText: TextView
    private lateinit var scoreText: TextView
    private lateinit var gestureDetector: GestureDetector
    private lateinit var descriptionText: TextView
    private lateinit var multiplierText: TextView
    private lateinit var timeText: TextView
    private lateinit var submitButton: Button

    private lateinit var countDownTimer: CountDownTimer
    private var timeLeftInMillis: Long = 60000 // 60 seconds

    private var isFinished = false
    private var score = 0
    private var multiplier = 1
    private var currentDirection = ""
    private var inputAllowed = false
    private var swipeTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_swipe)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        directionText = findViewById(R.id.direction_text)
        scoreText = findViewById(R.id.score_text)
        descriptionText = findViewById(R.id.description_text)
        multiplierText = findViewById(R.id.multiplier_text)
        timeText = findViewById(R.id.time_text)
        submitButton = findViewById(R.id.submit_button)
        numberGameText = findViewById(R.id.number_game_text)
        numberGameText.text = "Game $gameIndex"

        gestureDetector = GestureDetector(this, gestureListener)

        submitButton.setOnClickListener {
            goToNextGame(globalScore + score)
        }

        nextChallenge()
        startTimer()
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
                submitButton.isEnabled = true
                isFinished = true
                inputAllowed = false
                descriptionText.text = "Finish !"
                mediaPlayer = MediaPlayer.create(this@SwipeActivity, R.raw.tada)
                mediaPlayer?.start()
                swipeTimer?.cancel()
            }
        }.start()
    }

    private fun nextChallenge() {
        if (isFinished) return

        inputAllowed = true
        currentDirection = listOf("UP", "DOWN", "LEFT", "RIGHT").random()
        directionText.text = currentDirection

        swipeTimer?.cancel()
        swipeTimer = object : CountDownTimer(2000, 2000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                inputAllowed = false
                descriptionText.text = "Late !"
                multiplier = 1
                multiplierText.text = "x$multiplier"
                nextChallenge()
            }
        }.start()
    }

    private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (!inputAllowed || e1 == null) return false

            val diffX = e2.x - e1.x
            val diffY = e2.y - e1.y

            val result = when {
                abs(diffX) > abs(diffY) && abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD -> {
                    if (diffX > 0) checkSwipe("RIGHT") else checkSwipe("LEFT")
                    true
                }
                abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD -> {
                    if (diffY > 0) checkSwipe("DOWN") else checkSwipe("UP")
                    true
                }
                else -> false
            }
            return result
        }
    }

    private fun checkSwipe(direction: String) {
        if (isFinished) return

        swipeTimer?.cancel()
        inputAllowed = false
        if (direction == currentDirection) {
            score = score.plus(multiplier)
            multiplier++
            multiplierText.text = "x$multiplier"
            descriptionText.text = "Good !"
            mediaPlayer = MediaPlayer.create(this@SwipeActivity, R.raw.success)
            mediaPlayer?.start()
        } else {
            score = score.minus(multiplier)
            multiplier = 1
            multiplierText.text = "x$multiplier"
            descriptionText.text = "Bad !"
            mediaPlayer = MediaPlayer.create(this@SwipeActivity, R.raw.fail)
            mediaPlayer?.start()
        }
        scoreText.text = "$score"
        nextChallenge()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    override fun onDestroy() {
        super.onDestroy()
        swipeTimer?.cancel()
        countDownTimer.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
