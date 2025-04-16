package com.example.games

import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null

    private lateinit var questionText: TextView
    private lateinit var answerInput: EditText
    private lateinit var submitButton: Button
    private lateinit var feedbackText: TextView
    private lateinit var scoreText: TextView
    private lateinit var multiplierText: TextView

    private var currentAnswer: Int = 0
    private var score: Int = 0
    private var multiplier: Int = 1
    private lateinit var countDownTimer: CountDownTimer
    private var timeLeftInMillis: Long = 60000 // 60 seconds
    private var gameEnded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_layout)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        questionText = findViewById(R.id.question_text)
        answerInput = findViewById(R.id.answer_input)
        submitButton = findViewById(R.id.submit_button)
        feedbackText = findViewById(R.id.feedback_text)
        scoreText = findViewById(R.id.score_text)
        multiplierText = findViewById(R.id.multiplier_text)

        startTimer()
        generateQuestion()

        answerInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (!gameEnded) validateAnswer()
                true
            } else {
                false
            }
        }

        submitButton.setOnClickListener {

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun validateAnswer() {
        val userInput = answerInput.text.toString().toIntOrNull()

        if (userInput == currentAnswer) {
            score = score.plus(multiplier)
            multiplier++
            multiplierText.text = "x$multiplier"
            feedbackText.text = getString(R.string.right)
            mediaPlayer = MediaPlayer.create(this@MainActivity, R.raw.success)
            mediaPlayer?.start()
        } else {
            multiplier = 1
            multiplierText.text = "x$multiplier"
            feedbackText.text = "${getString(R.string.wrong)} It was : $currentAnswer"
            mediaPlayer = MediaPlayer.create(this@MainActivity, R.raw.fail)
            mediaPlayer?.start()
        }

        scoreText.text = "$score"
        answerInput.text.clear()
        generateQuestion()
    }

    private fun generateQuestion() {
        val operators = listOf("+", "-", "*", "/")
        val op = operators.random()
        var a = Random.nextInt(1, 20)
        val b = Random.nextInt(1, 20)

        if (op == "/") {
            a *= b // pour éviter les divisions non entières
        }

        val question = "$a $op $b"
        currentAnswer = when (op) {
            "+" -> a + b
            "-" -> a - b
            "*" -> a * b
            "/" -> a / b
            else -> 0
        }

        questionText.text = question
    }

    private fun startTimer() {
        val timerText = findViewById<TextView>(R.id.time_text)

        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                val secondsLeft = millisUntilFinished / 1000
                timerText.text =  "$secondsLeft"
            }

            override fun onFinish() {
                gameEnded = true
                questionText.text = getString(R.string.time_up)
                submitButton.isEnabled = true
                answerInput.isEnabled = false

                mediaPlayer = MediaPlayer.create(this@MainActivity, R.raw.tada)
                mediaPlayer?.start()
            }
        }

        countDownTimer.start()
    }

}