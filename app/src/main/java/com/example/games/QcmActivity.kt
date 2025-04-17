package com.example.games

import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.Call
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// --- DATA CLASSES ---
data class TriviaResponse(val results: List<TriviaQuestion>)
data class TriviaQuestion(
    val question: String,
    val correct_answer: String,
    val incorrect_answers: List<String>
)

// --- RETROFIT INTERFACE ---
interface TriviaApiService {
    @GET("api.php")
    fun getQuestions(
        @Query("amount") amount: Int = 1,
        @Query("type") type: String = "multiple"
    ): Call<TriviaResponse>
}

class QcmActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null

    private lateinit var questionText: TextView
    private lateinit var scoreText: TextView
    private lateinit var feedbackText: TextView
    private lateinit var submitButton: Button
    private lateinit var multiplierText: TextView
    private lateinit var choiceButtons: List<Button>

    private var score = 0
    private var multiplier: Int = 1
    private lateinit var countDownTimer: CountDownTimer
    private var timeLeftInMillis: Long = 60000 // 60 seconds
    private var gameEnded = false

    private lateinit var api: TriviaApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_qcm)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        questionText = findViewById(R.id.question_text)
        scoreText = findViewById(R.id.score_text)
        submitButton = findViewById(R.id.submit_button)
        multiplierText = findViewById(R.id.multiplier_text)
        feedbackText = findViewById(R.id.feedback_text)
        choiceButtons = listOf(
            findViewById(R.id.choice1),
            findViewById(R.id.choice2),
            findViewById(R.id.choice3)
        )

        val retrofit = Retrofit.Builder()
            .baseUrl("https://opentdb.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(TriviaApiService::class.java)

        startTimer()
        loadNextQuestion()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        countDownTimer.cancel()
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
                gameEnded = true
                questionText.text = getString(R.string.time_up)
                submitButton.isEnabled = true
                choiceButtons.forEach { it.isEnabled = false }

                mediaPlayer = MediaPlayer.create(this@QcmActivity, R.raw.tada)
                mediaPlayer?.start()
            }
        }

        countDownTimer.start()
    }

    private fun loadNextQuestion() {
        api.getQuestions().enqueue(object : Callback<TriviaResponse> {
            override fun onResponse(
                call: Call<TriviaResponse>,
                response: Response<TriviaResponse>
            ) {
                val question = response.body()?.results?.firstOrNull()
                if (question != null) {
                    val allAnswers = question.incorrect_answers.toMutableList().apply {
                        add(question.correct_answer)
                        shuffle()
                    }

                    questionText.text =
                        HtmlCompat.fromHtml(question.question, HtmlCompat.FROM_HTML_MODE_LEGACY)
                            .toString()

                    choiceButtons.forEachIndexed { i, btn ->
                        btn.text =
                            HtmlCompat.fromHtml(allAnswers[i], HtmlCompat.FROM_HTML_MODE_LEGACY)
                                .toString()
                        val correctAnswer = HtmlCompat.fromHtml(
                            question.correct_answer,
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                            .toString()
                        btn.setOnClickListener {
                            if (btn.text == correctAnswer) {
                                score = score.plus(multiplier)
                                multiplier++
                                multiplierText.text = "x$multiplier"
                                feedbackText.text = getString(R.string.right)
                                mediaPlayer = MediaPlayer.create(this@QcmActivity, R.raw.success)
                                mediaPlayer?.start()
                            } else {
                                multiplier = 1
                                multiplierText.text = "x$multiplier"
                                feedbackText.text =
                                    "${getString(R.string.wrong)} It was : ${correctAnswer}"
                                mediaPlayer = MediaPlayer.create(this@QcmActivity, R.raw.fail)
                                mediaPlayer?.start()
                            }
                            scoreText.text = "$score"
                            loadNextQuestion()
                        }
                    }
                } else {
                    Toast.makeText(this@QcmActivity, "Aucune question reçue", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<TriviaResponse>, t: Throwable) {
                Toast.makeText(this@QcmActivity, "Erreur de connexion", Toast.LENGTH_SHORT).show()
            }
        })
    }
}