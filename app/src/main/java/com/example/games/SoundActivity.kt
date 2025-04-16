package com.example.games

import android.Manifest
import android.content.pm.PackageManager
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

    private lateinit var startButton: Button
    private lateinit var resultText: TextView
    private lateinit var countdownText: TextView
    private lateinit var scoreText: TextView
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

    private fun stopRecording() {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
        } catch (_: Exception) {
        }
        mediaRecorder = null

        val score = (maxAmplitude / 100.0).toInt()
        resultText.text = "$score / 100"
        countdownText.text = "Finish !"
        if (score <= 100) {
            scoreText.text = "$score"
        } else {
            scoreText.text = "${100-score}"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder?.release()
        mediaRecorder = null
    }
}