package com.example.games

import android.content.Intent
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_layout)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.qr_button).setOnClickListener {
            val intent = Intent(this, QrActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.qcm_button).setOnClickListener {
            val intent = Intent(this, QcmActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.sun_button).setOnClickListener {
            val intent = Intent(this, SunActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.sound_button).setOnClickListener {
            val intent = Intent(this, SoundActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.hold_button).setOnClickListener {
            val intent = Intent(this, HoldActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.swipe_button).setOnClickListener {
            val intent = Intent(this, SwipeActivity::class.java)
            startActivity(intent)
        }

    }

}