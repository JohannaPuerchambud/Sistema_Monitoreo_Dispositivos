package com.utn.sistemamonitoreodispositivos

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.util.Log
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Collections


object DeviceStatusUtils {


    fun getBatteryLevel(context: Context): Int {
        return try {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } catch (e: Exception) {
            Log.e("DeviceStatusUtils", "Error al obtener nivel de batería: ${e.message}")
            -1
        }
    }


    fun getNetworkStatus(context: Context): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork ?: return "Sin conexión"
                val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return "Sin conexión"

                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Datos Móviles"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                    else -> "Desconocido"
                }
            } else {
                @Suppress("DEPRECATION")
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                    @Suppress("DEPRECATION")
                    when (activeNetworkInfo.type) {
                        ConnectivityManager.TYPE_WIFI -> "Wi-Fi"
                        ConnectivityManager.TYPE_MOBILE -> "Datos Móviles"
                        ConnectivityManager.TYPE_ETHERNET -> "Ethernet"
                        else -> "Desconocido"
                    }
                } else {
                    "Sin conexión"
                }
            }
        } catch (e: Exception) {
            Log.e("DeviceStatusUtils", "Error al obtener estado de red: ${e.message}")
            "Error"
        }
    }


    fun getAvailableInternalStorage(): String {
        return try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val bytesAvailable = stat.blockSizeLong * stat.availableBlocksLong
            val gigabytesAvailable = bytesAvailable / (1024.0 * 1024.0 * 1024.0)
            String.format("%.2f", gigabytesAvailable)
        } catch (e: Exception) {
            Log.e("DeviceStatusUtils", "Error al obtener almacenamiento disponible: ${e.message}")
            "N/A"
        }
    }


    fun getOSVersion(): String {
        return "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})"
    }


    fun getDeviceModel(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}"
    }


    fun getLocalIpAddress(): String {
        try {
            val networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in networkInterfaces) {
                val addrs = Collections.list(intf.getInetAddresses())
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr = addr.hostAddress
                        val isIPv4 = sAddr.indexOf(':') < 0 // Check if it's IPv4 or IPv6
                        if (isIPv4) {
                            return sAddr
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e("DeviceStatusUtils", "Error obteniendo IP: ${ex.message}")
        }
        return "N/A"
    }
}