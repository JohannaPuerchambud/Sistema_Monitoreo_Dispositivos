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
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var databaseManager: DatabaseManager
    private lateinit var deviceId: String

    private val NOTIFICATION_CHANNEL_ID = "LocationServiceChannel"
    private val NOTIFICATION_ID = 101

    override fun onCreate() {
        super.onCreate()
        Log.d("LocationService", "Servicio creado.")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        databaseManager = DatabaseManager(this)
        deviceId = DeviceUtils.getUniqueDeviceId(this)

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    saveLocationToDatabase(location)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("LocationService", "Servicio iniciado o reiniciado.")
        startLocationUpdates()
        sendServiceStatus(true)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        Log.d("LocationService", "Servicio destruido.")

        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(NOTIFICATION_ID)
        sendServiceStatus(false)
    }

    @Suppress("MissingPermission")
    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 30000)
                .setMinUpdateDistanceMeters(5f)
                .setWaitForAccurateLocation(true)
                .build()

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            ).addOnSuccessListener {
                Log.d("LocationService", "Solicitud de actualizaciones de ubicación iniciada.")
            }.addOnFailureListener { e ->
                Log.e("LocationService", "Error al iniciar actualizaciones de ubicación: ${e.message}")
            }
        } else {
            Log.e("LocationService", "Permiso de ubicación no concedido para iniciar actualizaciones.")
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
            .addOnSuccessListener {
                Log.d("LocationService", "Actualizaciones de ubicación detenidas.")
            }
            .addOnFailureListener { e ->
                Log.e("LocationService", "Error al detener actualizaciones de ubicación: ${e.message}")
            }
    }

    private fun saveLocationToDatabase(location: Location) {
        val sensorData = SensorData(
            id = 0L,
            deviceId = deviceId,
            latitude = location.latitude,
            longitude = location.longitude,
            timestamp = System.currentTimeMillis()

        )
        val rowId = databaseManager.insertSensorData(sensorData)
        Log.d("LocationService", "Ubicación guardada en BD: Lat ${location.latitude}, Lon ${location.longitude}, Row ID: $rowId")
    }



    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Servicio de Ubicación",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): android.app.Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Monitoreo de Dispositivos")
            .setContentText("Recolectando datos de ubicación en segundo plano...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)
            .build()
    }
    private fun sendServiceStatus(isRunning: Boolean) {
        val intent = Intent("LocationServiceStatus")
        intent.putExtra("isRunning", isRunning)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}