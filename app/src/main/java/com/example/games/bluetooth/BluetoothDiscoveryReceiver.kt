package com.example.games.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BluetoothDiscoveryReceiver(private val onDeviceFound: (BluetoothDevice) -> Unit) : BroadcastReceiver() {
    @Suppress("DEPRECATION")
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == BluetoothDevice.ACTION_FOUND) {
            val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
            device?.let { onDeviceFound(it) }
        }
    }
}
