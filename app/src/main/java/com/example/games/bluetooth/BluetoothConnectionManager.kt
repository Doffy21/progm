package com.example.games.bluetooth

import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object BluetoothConnectionManager {
    private var socket: BluetoothSocket? = null
    private var reader: BufferedReader? = null
    private var writer: PrintWriter? = null
    private val writeExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var readExecutor: ExecutorService? = null

    @Volatile
    private var isListening = false
    private var messageListener: ((String) -> Unit)? = null
    private var isClosed = false

    fun init(socket: BluetoothSocket) {
        this.socket = socket
        reader = BufferedReader(InputStreamReader(socket.inputStream))
        writer = PrintWriter(OutputStreamWriter(socket.outputStream), true)
        isClosed = false
    }

    fun sendMessage(message: String) {
        writeExecutor.execute {
            Log.d("BT_SEND", "sendMessage : $message")
            try {
                writer?.println(message)
                Log.d("BT_SEND", "Sent: $message")
            } catch (e: Exception) {
                Log.e("BT_SEND", "Erreur d'envoi", e)
            }
        }
    }

    fun listen(callback: (String) -> Unit) {
        if (isListening) {
            Log.d("BT_RECEIVE", "Already listening, use updateListener instead")
            messageListener = callback
            return
        }
        messageListener = callback
        isListening = true
        readExecutor = Executors.newSingleThreadExecutor()
        readExecutor?.execute {
            Log.d("BT_RECEIVE", "Listening started")
            try {
                while (true) {
                    val line = reader?.readLine() ?: break
                    Log.d("BT_RECEIVE", "Received: $line")
                    messageListener?.invoke(line)
                }
            } catch (e: Exception) {
                Log.e("BT_RECEIVE", "Erreur de réception", e)
            } finally {
                isListening = false
                Log.d("BT_RECEIVE", "Listening stopped")
            }
        }
    }

    fun stopListening() {
        try {
            readExecutor?.shutdownNow()
            readExecutor = null
            isListening = false
        } catch (e: Exception) {
            Log.e("BT_RECEIVE", "Erreur d'arrêt de l'écoute", e)
        }
    }

    fun close() {
        if (isClosed) return
        isClosed = true

        // Étape 1 : arrêter le listening proprement
        stopListening()

        // Étape 2 : fermer d'abord le socket (pour débloquer readLine)
        try {
            socket?.close()
            socket = null
        } catch (e: Exception) {
            Log.w("BT_CLOSE", "Socket déjà fermé ou erreur bénigne", e)
        }

        // Étape 3 : maintenant que le readLine() est débloqué, on peut fermer reader
        try {
            reader?.close()
            reader = null
        } catch (e: Exception) {
            Log.w("BT_CLOSE", "Reader déjà fermé ou erreur bénigne", e)
        }

        try {
            writer?.close()
            writer = null
        } catch (e: Exception) {
            Log.w("BT_CLOSE", "Writer déjà fermé ou erreur bénigne", e)
        }

        Log.d("BT_CLOSE", "Déconnexion Bluetooth terminée")
    }


    fun isConnected(): Boolean {
        return socket?.isConnected == true
    }
}
