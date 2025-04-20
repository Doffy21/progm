package com.example.games.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.games.R
import com.example.games.allGames
import java.io.IOException
import java.util.UUID

class LobbyActivity : AppCompatActivity() {
    private val APP_UUID: UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")

    @Suppress("DEPRECATION")
    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val discoveredDevices = mutableListOf<BluetoothDevice>()
    private lateinit var deviceAdapter: ArrayAdapter<String>

    private val discoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (BluetoothDevice.ACTION_FOUND == intent?.action) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    try {
                        val name = it.name
                        if (!discoveredDevices.contains(it)) {
                            if (name == null) return@let
                            discoveredDevices.add(it)
                            deviceAdapter.add("$name\n${it.address}")
                        }
                    } catch (e: SecurityException) {
                        deviceAdapter.add("(Accès refusé)\n${it.address}")
                    }
                }
            }
        }
    }

    private val requestBluetoothPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all { it.value }
            if (!granted) {
                Toast.makeText(this, "Les permissions Bluetooth sont requises", Toast.LENGTH_LONG).show()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lobby)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val listView = findViewById<ListView>(R.id.device_list)
        val discoverButton = findViewById<Button>(R.id.discover_button)
        val hostButton = findViewById<Button>(R.id.host_button)

        deviceAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        listView.adapter = deviceAdapter

        if (adapter == null) {
            Toast.makeText(this, "Bluetooth non supporté", Toast.LENGTH_LONG).show()
            finish()
        }

        if (!hasBluetoothPermissions()) {
            requestBluetoothPermissions()
        }

        discoverButton.setOnClickListener {
            if (!hasBluetoothPermissions()) {
                Toast.makeText(this, "Permissions Bluetooth manquantes", Toast.LENGTH_SHORT).show()
                requestBluetoothPermissions()
                return@setOnClickListener
            }
            startDiscovery()
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val device = discoveredDevices[position]
            connectToDevice(device)
        }

        hostButton.setOnClickListener {
            if (!hasBluetoothPermissions()) {
                Toast.makeText(this, "Permissions Bluetooth manquantes", Toast.LENGTH_SHORT).show()
                requestBluetoothPermissions()
                return@setOnClickListener
            }
            makeDeviceDiscoverable()
            hostGame()
        }
    }


    private fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestBluetoothPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        requestBluetoothPermissionsLauncher.launch(permissions)
    }


    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(discoveryReceiver)
        } catch (_: Exception) {}
    }

    @SuppressLint("MissingPermission")
    private fun makeDeviceDiscoverable() {
        if (adapter?.scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120)
            }
            startActivity(discoverableIntent)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startDiscovery() {
        deviceAdapter.clear()
        discoveredDevices.clear()

        if (adapter?.isDiscovering == true) {
            adapter.cancelDiscovery()
        }

        registerReceiver(discoveryReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        adapter?.startDiscovery()
        Log.d("LobbyActivity", "startDiscovery called")
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        Thread {
            try {
                val socket = try {
                    device.createRfcommSocketToServiceRecord(APP_UUID)
                } catch (e: SecurityException) {
                    runOnUiThread {
                        Toast.makeText(this, "Permission refusée (Bluetooth)", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }
                adapter?.cancelDiscovery()
                socket.connect()
                BluetoothConnectionManager.init(socket)

                BluetoothConnectionManager.listen { message ->
                    if (message.startsWith("START|")) {
                        val classNames = message.removePrefix("START|").split(",")
                        val gameClasses = classNames.mapNotNull {
                            try {
                                Class.forName(it) as? Class<out AppCompatActivity>
                            } catch (e: Exception) {
                                null
                            }
                        }
                        runOnUiThread {
                            if (gameClasses.size == 3) {
                                val intent = Intent(this, gameClasses[0])
                                intent.putExtra("currentScore", 0)
                                intent.putExtra("gameIndex", 1)
                                intent.putExtra("gameList", ArrayList(gameClasses))
                                intent.putExtra("role", "client")
                                startActivity(intent)
                                finish()
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Échec de la connexion", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    @SuppressLint("MissingPermission")
    private fun hostGame() {
        Thread {
            try {
                val serverSocket = adapter!!.listenUsingRfcommWithServiceRecord("MyGame", APP_UUID)
                val socket: BluetoothSocket = serverSocket.accept()
                serverSocket.close()
                BluetoothConnectionManager.init(socket)

                runOnUiThread {
                    Toast.makeText(this, "Un joueur a rejoint", Toast.LENGTH_SHORT).show()

                    val selectedGames = ArrayList(allGames.shuffled().take(3))
                    val classNames = selectedGames.map { it.name }
                    val message = "START|" + classNames.joinToString(",")
                    BluetoothConnectionManager.sendMessage(message)

                    val intent = Intent(this, selectedGames[0])
                    intent.putExtra("currentScore", 0)
                    intent.putExtra("gameIndex", 1)
                    intent.putExtra("gameList", selectedGames)
                    intent.putExtra("role", "host")
                    startActivity(intent)
                    finish()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
    }
}
