package com.utn.sistemamonitoreodispositivos

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import fi.iki.elonen.NanoHTTPD
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.IOException

class ApiService : Service() {

    private lateinit var httpServer: MyHttpServer
    private lateinit var databaseManager: DatabaseManager
    private var authToken: String? = null
    private lateinit var deviceId: String
    private val API_PORT = 8080

    private val NOTIFICATION_CHANNEL_ID = "ApiServiceChannel"
    private val NOTIFICATION_ID = 102

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    override fun onCreate() {
        super.onCreate()
        Log.d("ApiService", "Servicio API creado.")
        databaseManager = DatabaseManager(this)
        deviceId = DeviceUtils.getUniqueDeviceId(this)
        Log.d("ApiService", "ID del Dispositivo: $deviceId")

        authToken = databaseManager.getAuthToken()

        if (authToken == null) {
            val generatedToken = "JAPASPUELS"
            databaseManager.saveAuthToken(generatedToken)
            authToken = generatedToken
            Log.w("ApiService", "No se encontró token de autenticación. Generado y guardado uno nuevo: $authToken")
        } else {
            Log.d("ApiService", "Token de autenticación cargado: $authToken")
        }


        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())


        httpServer = MyHttpServer(API_PORT)
        try {
            httpServer.start()
            Log.d("ApiService", "Servidor API iniciado en el puerto $API_PORT")
        } catch (e: IOException) {
            Log.e("ApiService", "Error al iniciar el servidor API: ${e.message}", e)

            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("ApiService", "Servicio API iniciado o reiniciado.")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        httpServer.stop()
        Log.d("ApiService", "Servidor API detenido.")

    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Servicio API Monitoreo",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): android.app.Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Monitoreo de Dispositivos")
            .setContentText("Servidor API en ejecución...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }


    inner class MyHttpServer(port: Int) : NanoHTTPD(port) {
        override fun serve(session: IHTTPSession): Response {
            val uri = session.uri
            val method = session.method

            Log.d("MyHttpServer", "Solicitud recibida: $method $uri")

            if (authToken != null) {
                val authHeader = session.headers["authorization"]
                if (authHeader == null || !authHeader.startsWith("Bearer ") || authHeader.substring(7) != authToken) {
                    Log.w("MyHttpServer", "Acceso no autorizado a $uri. Token esperado: $authToken, Recibido: $authHeader")
                    return newFixedLengthResponse(
                        Response.Status.UNAUTHORIZED,
                        NanoHTTPD.MIME_PLAINTEXT,
                        "Unauthorized. Missing or invalid Bearer Token."
                    )
                }
            } else {
                Log.w("MyHttpServer", "No hay token de autenticación configurado en el servidor API. Todas las solicitudes serán permitidas.")
            }

            return when (uri) {
                "/api/sensor_data" -> handleSensorData(session)
                "/api/device_status" -> handleDeviceStatus(session)
                "/" -> newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, "API Service is running.")
                else -> newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Not Found")
            }
        }

        private fun handleSensorData(session: IHTTPSession): Response {
            val params = session.parameters
            val startTimeParam = params["start_time"]?.get(0)
            val endTimeParam = params["end_time"]?.get(0)

            var startTime: Long? = null
            var endTime: Long? = null

            try {
                startTime = startTimeParam?.toLongOrNull()
                endTime = endTimeParam?.toLongOrNull()
            } catch (e: NumberFormatException) {
                return newFixedLengthResponse(
                    Response.Status.BAD_REQUEST,
                    NanoHTTPD.MIME_PLAINTEXT,
                    "Invalid timestamp format for start_time or end_time."
                )
            }

            val sensorDataList: List<SensorData> = if (startTime != null && endTime != null) {
                databaseManager.getSensorDataByTimeRange(startTime, endTime)
            } else {
                Log.w("MyHttpServer", "No se proporcionaron start_time o end_time. Devolviendo los últimos 100 registros.")
                databaseManager.getLatestSensorData(100)
            }

            val jsonResponse = gson.toJson(sensorDataList)
            return newFixedLengthResponse(Response.Status.OK, "application/json", jsonResponse)
        }

        private fun handleDeviceStatus(session: IHTTPSession): Response {
            val deviceStatus = mapOf(
                "deviceId" to deviceId,
                "batteryLevel" to DeviceStatusUtils.getBatteryLevel(this@ApiService),
                "networkStatus" to DeviceStatusUtils.getNetworkStatus(this@ApiService),
                "availableStorageGB" to DeviceStatusUtils.getAvailableInternalStorage(),
                "osVersion" to DeviceStatusUtils.getOSVersion(),
                "deviceModel" to DeviceStatusUtils.getDeviceModel(),
                "localIpAddress" to DeviceStatusUtils.getLocalIpAddress(),
                "timestamp" to System.currentTimeMillis()
            )
            val jsonResponse = gson.toJson(deviceStatus)
            return newFixedLengthResponse(Response.Status.OK, "application/json", jsonResponse)
        }
    }
}