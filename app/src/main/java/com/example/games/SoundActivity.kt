package com.example.games

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File

class SoundActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null

    private lateinit var startButton: Button
    private lateinit var resultText: TextView
    private lateinit var countdownText: TextView
    private lateinit var scoreText: TextView
    private lateinit var multiplierText: TextView
    private lateinit var timeText: TextView

    private var multiplier = 1
    private var score = 0

    private lateinit var countDownTimer: CountDownTimer
    private var timeLeftInMillis: Long = 60000 // 60 seconds

    private var mediaRecorder: MediaRecorder? = null
    private var maxAmplitude = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sound)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        startButton = findViewById(R.id.start_button)
        resultText = findViewById(R.id.result_text)
        countdownText = findViewById(R.id.countdown_text)
        scoreText = findViewById(R.id.score_text)
        multiplierText = findViewById(R.id.multiplier_text)
        timeText = findViewById(R.id.time_text)

        startTimer()
        startButton.setOnClickListener {
            if (checkAudioPermission()) {
                startRecording()
            } else {
                requestAudioPermission()
            }
        }
    }

    private fun checkAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAudioPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1002)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1002 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startRecording()
        } else {
            Toast.makeText(this, "Permission micro refusée", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startRecording() {
        resultText.text = ""
        maxAmplitude = 0

        if (mediaRecorder != null) {
            mediaRecorder?.release()
            mediaRecorder = null
        }

        val tempFile = File.createTempFile("temp", ".3gp", cacheDir)

        @Suppress("DEPRECATION")
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(tempFile.absolutePath)

            try {
                prepare()
                start()
            } catch (e: Exception) {
                Toast.makeText(this@SoundActivity, "Erreur micro : ${e.message}", Toast.LENGTH_LONG)
                    .show()
                e.printStackTrace()
                release()
                mediaRecorder = null
                return
            }
        }

        object : CountDownTimer(4000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                countdownText.text = if (secondsLeft > 0) "$secondsLeft" else "GO !"
                try {
                    val amplitude = mediaRecorder?.maxAmplitude ?: 0
                    if (amplitude > maxAmplitude) {
                        maxAmplitude = amplitude
                    }
                } catch (_: Exception) {
                }
            }

            override fun onFinish() {
                stopRecording()
            }
        }.start()
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
                startButton.isEnabled = false
                mediaPlayer = MediaPlayer.create(this@SoundActivity, R.raw.tada)
                mediaPlayer?.start()
            }
        }

        countDownTimer.start()
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
        } catch (_: Exception) {
        }
        mediaRecorder = null

        val currentScore = (maxAmplitude / 100.0).toInt()
        resultText.text = "$currentScore / 100"
        countdownText.text = "Finish !"
        if (currentScore <= 100) {
            score += currentScore*multiplier
            multiplier++
            multiplierText.text = "x$multiplier"
            scoreText.text = "$score"
        } else {
            score += (100-currentScore)*multiplier
            scoreText.text = "$score"
            multiplier = 1
            multiplierText.text = "x$multiplier"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder?.release()
        mediaPlayer?.release()
        countDownTimer.cancel()

    }
}