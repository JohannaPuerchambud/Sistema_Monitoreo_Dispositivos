package com.utn.sistemamonitoreodispositivos

import android.content.Context
import java.util.UUID

object DeviceUtils {

    private const val PREFS_NAME = "device_prefs"
    private const val PREF_DEVICE_ID = "device_id"


    fun getUniqueDeviceId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var deviceId = prefs.getString(PREF_DEVICE_ID, null)

        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString()
            prefs.edit().putString(PREF_DEVICE_ID, deviceId).apply()
        }
        return deviceId
    }
}