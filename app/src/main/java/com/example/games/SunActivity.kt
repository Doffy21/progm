package com.example.games


import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.sqrt
import kotlin.random.Random

class SunActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var statusText: TextView
    private lateinit var distanceText: TextView
    private lateinit var scoreText: TextView

    private var score = 0
    private var totalSteps = 0
    private var targetSteps = 100 // objectif de "distance" en nombre de pas simulés

    private var isStopped = false
    private var isGameRunning = false
    private var moveDetectedWhileStopped = false

    private var lastAccel = 0f
    private var lastAccelFiltered = 0f

    private lateinit var stopTimer: CountDownTimer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sun)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        statusText = findViewById(R.id.status_text)
        distanceText = findViewById(R.id.distance_text)
        scoreText = findViewById(R.id.score_text)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        startGame()
    }

    private fun startGame() {
        isGameRunning = true
        moveDetectedWhileStopped = false
        statusText.text = getString(R.string.move)
        distanceText.text = "$totalSteps / $targetSteps"
        nextStop()
    }

    private fun nextStop() {
        val delay = Random.nextLong(3000L, 5000L) // entre 3 et 5 sec
        stopTimer = object : CountDownTimer(delay, 1000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                triggerStopPhase()
            }
        }
        stopTimer.start()
    }

    private fun triggerStopPhase() {
        isStopped = true
        statusText.text = getString(R.string.stop)
        moveDetectedWhileStopped = false

        object : CountDownTimer(3000, 1000) { // phase d'arrêt : 3 sec
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                if (moveDetectedWhileStopped) {
                    statusText.text = "You're moved !"
                    totalSteps -= 5
                    score -=5
                    scoreText.text = "$score"
                    distanceText.text = "$totalSteps / $targetSteps"
                } else {
                    statusText.text = getString(R.string.move)
                }

                isStopped = false

                // Empêche la relance si le joueur a gagné
                if (isGameRunning) {
                    nextStop()
                }
            }

        }.start()
    }

    private fun stopGame() {
        isGameRunning = false
        sensorManager.unregisterListener(this)
        stopTimer.cancel()
    }


    override fun onResume() {
        super.onResume()
        accelerometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }


    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!isGameRunning || event == null) return

        // Accéléromètre pour détecter le mouvement pendant STOP
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val acceleration = sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH
            val delta = acceleration - lastAccel
            lastAccel = acceleration
            lastAccelFiltered = lastAccelFiltered * 0.9f + delta

            if (lastAccelFiltered > 1.2f) {
                if (isStopped) {
                    moveDetectedWhileStopped = true
                } else {
                    totalSteps++
                    distanceText.text = "$totalSteps / $targetSteps"
                    if (totalSteps >= targetSteps) {
                        statusText.text = getString(R.string.win)
                        stopGame()
                        score += 100
                        scoreText.text = "$score"
                    }
                }
            }
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}