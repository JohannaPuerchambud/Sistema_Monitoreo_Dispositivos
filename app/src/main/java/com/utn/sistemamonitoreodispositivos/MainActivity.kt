package com.utn.sistemamonitoreodispositivos

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.os.Bundle

class MainActivity : AppCompatActivity() {


    private lateinit var btnStartCollection: Button
    private lateinit var btnStopCollection: Button
    private lateinit var tvCollectionStatus: TextView
    private lateinit var tvLocalIp: TextView
    private lateinit var tvDeviceStatus: TextView
    private lateinit var tvLastGpsData: TextView


    private lateinit var databaseManager: DatabaseManager
    private lateinit var apiServiceIntent: Intent


    private val locationStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val isRunning = it.getBooleanExtra("isRunning", false)
                tvCollectionStatus.text = "Estado: ${if (isRunning) "Activo" else "Inactivo"}"
                btnStartCollection.isEnabled = !isRunning
                btnStopCollection.isEnabled = isRunning
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        btnStartCollection = findViewById(R.id.btnStartCollection)
        btnStopCollection = findViewById(R.id.btnStopCollection)
        tvCollectionStatus = findViewById(R.id.tvCollectionStatus)
        tvLocalIp = findViewById(R.id.tvLocalIp)
        tvDeviceStatus = findViewById(R.id.tvDeviceStatus)
        tvLastGpsData = findViewById(R.id.tvLastGpsData)


        databaseManager = DatabaseManager(this)


        apiServiceIntent = Intent(this, ApiService::class.java)


        btnStartCollection.setOnClickListener {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                val serviceIntent = Intent(this, LocationService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(apiServiceIntent)
                } else {
                    startService(apiServiceIntent)
                }

                tvCollectionStatus.text = "Estado: Iniciando..."
                btnStartCollection.isEnabled = false
                btnStopCollection.isEnabled = true
            } else {

                Log.w("MainActivity", "Permiso de ubicación no concedido. No se puede iniciar el servicio de recolección.")

            }
        }

        btnStopCollection.setOnClickListener {

            stopService(Intent(this, LocationService::class.java))

            stopService(apiServiceIntent)

            tvCollectionStatus.text = "Estado: Detenido"
            btnStartCollection.isEnabled = true
            btnStopCollection.isEnabled = false
        }


        LocalBroadcastManager.getInstance(this)
            .registerReceiver(locationStatusReceiver, IntentFilter("LocationServiceStatus"))


        updateUI()
    }

    override fun onResume() {
        super.onResume()
        updateDeviceInfo()
        displayLastGpsData()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationStatusReceiver)
    }

    private fun updateUI() {
        tvCollectionStatus.text = "Estado: Inactivo"
        btnStartCollection.isEnabled = true
        btnStopCollection.isEnabled = false

        updateDeviceInfo()
        displayLastGpsData()
    }


    private fun updateDeviceInfo() {
        val ipAddress = DeviceStatusUtils.getLocalIpAddress()
        tvLocalIp.text = "IP Local: $ipAddress"

        val batteryLevel = DeviceStatusUtils.getBatteryLevel(this)
        val networkStatus = DeviceStatusUtils.getNetworkStatus(this)
        val availableStorage = DeviceStatusUtils.getAvailableInternalStorage()
        val osVersion = DeviceStatusUtils.getOSVersion()
        val deviceModel = DeviceStatusUtils.getDeviceModel()

        val deviceStatusText = """
            Batería: $batteryLevel%
            Red: $networkStatus
            Almacenamiento Libre: ${availableStorage} GB
            OS: $osVersion
            Modelo: $deviceModel
        """.trimIndent()
        tvDeviceStatus.text = deviceStatusText
    }


    private fun displayLastGpsData() {
        val latestData = databaseManager.getLastSensorData()
        if (latestData != null) {
            tvLastGpsData.text = "Último GPS:\nLat: ${latestData.latitude}\nLon: ${latestData.longitude}\nFecha: ${latestData.timestamp}"
        } else {
            tvLastGpsData.text = "Último GPS: Sin datos"
        }
    }
}