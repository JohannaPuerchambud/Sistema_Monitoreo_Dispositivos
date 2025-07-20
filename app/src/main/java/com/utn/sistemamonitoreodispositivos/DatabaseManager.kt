package com.utn.sistemamonitoreodispositivos

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log
import android.provider.BaseColumns
import com.utn.sistemamonitoreodispositivos.DatabaseContract.AuthCredentialsEntry
import com.utn.sistemamonitoreodispositivos.DatabaseContract.SensorDataEntry


class DatabaseManager(context: Context) {

    private val dbHelper: DatabaseHelper = DatabaseHelper(context)

    // --- Métodos para SensorData ---

    /**
     * Inserta un nuevo registro de SensorData en la base de datos.
     * @param sensorData El objeto SensorData a insertar.
     * @return El ID de la fila recién insertada, o -1 si ocurrió un error.
     */
    fun insertSensorData(sensorData: SensorData): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(SensorDataEntry.COLUMN_NAME_DEVICE_ID, sensorData.deviceId)
            put(SensorDataEntry.COLUMN_NAME_LATITUDE, sensorData.latitude)
            put(SensorDataEntry.COLUMN_NAME_LONGITUDE, sensorData.longitude)
            put(SensorDataEntry.COLUMN_NAME_TIMESTAMP, sensorData.timestamp)
        }
        val newRowId = db.insert(SensorDataEntry.TABLE_NAME, null, values)
        db.close()
        if (newRowId == -1L) {
            Log.e("DatabaseManager", "Error al insertar datos del sensor.")
        } else {
            Log.d("DatabaseManager", "Datos del sensor insertados con ID: $newRowId")
        }
        return newRowId
    }

    /**
     * Recupera datos del sensor dentro de un rango de tiempo específico.
     * @param startTime El timestamp de inicio (milisegundos).
     * @param endTime El timestamp final (milisegundos).
     * @return Una lista de objetos SensorData.
     */
    fun getSensorDataByTimeRange(startTime: Long, endTime: Long): List<SensorData> {
        val sensorDataList = mutableListOf<SensorData>()
        val db = dbHelper.readableDatabase

        val projection = arrayOf(
            BaseColumns._ID,
            SensorDataEntry.COLUMN_NAME_DEVICE_ID,
            SensorDataEntry.COLUMN_NAME_LATITUDE,
            SensorDataEntry.COLUMN_NAME_LONGITUDE,
            SensorDataEntry.COLUMN_NAME_TIMESTAMP
        )

        val selection = "${SensorDataEntry.COLUMN_NAME_TIMESTAMP} BETWEEN ? AND ?"
        val selectionArgs = arrayOf(startTime.toString(), endTime.toString())

        val sortOrder = "${SensorDataEntry.COLUMN_NAME_TIMESTAMP} ASC"

        val cursor: Cursor? = db.query(
            SensorDataEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder
        )

        cursor?.use {
            val idColumnIndex = it.getColumnIndexOrThrow(BaseColumns._ID)
            val deviceIdColumnIndex = it.getColumnIndexOrThrow(SensorDataEntry.COLUMN_NAME_DEVICE_ID)
            val latColumnIndex = it.getColumnIndexOrThrow(SensorDataEntry.COLUMN_NAME_LATITUDE)
            val lonColumnIndex = it.getColumnIndexOrThrow(SensorDataEntry.COLUMN_NAME_LONGITUDE)
            val timestampColumnIndex = it.getColumnIndexOrThrow(SensorDataEntry.COLUMN_NAME_TIMESTAMP)

            while (it.moveToNext()) {
                val itemId = it.getLong(idColumnIndex)
                val deviceId = it.getString(deviceIdColumnIndex)
                val latitude = it.getDouble(latColumnIndex)
                val longitude = it.getDouble(lonColumnIndex)
                val timestamp = it.getLong(timestampColumnIndex)

                sensorDataList.add(SensorData(itemId, deviceId, latitude, longitude, timestamp))
            }
        }
        db.close()
        return sensorDataList
    }

    /**
     * Recupera los últimos N registros de SensorData de la base de datos.
     * @param limit El número máximo de registros a devolver.
     * @return Una lista de objetos SensorData.
     */
    fun getLatestSensorData(limit: Int): List<SensorData> {
        val sensorDataList = mutableListOf<SensorData>()
        val db = dbHelper.readableDatabase

        val projection = arrayOf(
            BaseColumns._ID,
            SensorDataEntry.COLUMN_NAME_DEVICE_ID,
            SensorDataEntry.COLUMN_NAME_LATITUDE,
            SensorDataEntry.COLUMN_NAME_LONGITUDE,
            SensorDataEntry.COLUMN_NAME_TIMESTAMP
        )

        val sortOrder = "${SensorDataEntry.COLUMN_NAME_TIMESTAMP} DESC"

        val cursor: Cursor? = db.query(
            SensorDataEntry.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            sortOrder,
            limit.toString()
        )

        cursor?.use {
            val idColumnIndex = it.getColumnIndexOrThrow(BaseColumns._ID)
            val deviceIdColumnIndex = it.getColumnIndexOrThrow(SensorDataEntry.COLUMN_NAME_DEVICE_ID)
            val latColumnIndex = it.getColumnIndexOrThrow(SensorDataEntry.COLUMN_NAME_LATITUDE)
            val lonColumnIndex = it.getColumnIndexOrThrow(SensorDataEntry.COLUMN_NAME_LONGITUDE)
            val timestampColumnIndex = it.getColumnIndexOrThrow(SensorDataEntry.COLUMN_NAME_TIMESTAMP)

            while (it.moveToNext()) {
                val itemId = it.getLong(idColumnIndex)
                val deviceId = it.getString(deviceIdColumnIndex)
                val latitude = it.getDouble(latColumnIndex)
                val longitude = it.getDouble(lonColumnIndex)
                val timestamp = it.getLong(timestampColumnIndex)

                sensorDataList.add(SensorData(itemId, deviceId, latitude, longitude, timestamp))
            }
        }
        db.close()
        return sensorDataList
    }


    /**
     * Recupera el último registro de SensorData de la base de datos.
     * @return El último objeto SensorData si existe, o null si no hay datos.
     */
    fun getLastSensorData(): SensorData? {
        val db = dbHelper.readableDatabase
        var sensorData: SensorData? = null
        val cursor = db.query(
            SensorDataEntry.TABLE_NAME,
            arrayOf(
                BaseColumns._ID,
                SensorDataEntry.COLUMN_NAME_DEVICE_ID,
                SensorDataEntry.COLUMN_NAME_LATITUDE,
                SensorDataEntry.COLUMN_NAME_LONGITUDE,
                SensorDataEntry.COLUMN_NAME_TIMESTAMP
            ),
            null,
            null,
            null,
            null,
            "${SensorDataEntry.COLUMN_NAME_TIMESTAMP} DESC",
            "1"
        )

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID))
                    val deviceId = cursor.getString(cursor.getColumnIndexOrThrow(SensorDataEntry.COLUMN_NAME_DEVICE_ID))
                    val latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(SensorDataEntry.COLUMN_NAME_LATITUDE))
                    val longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(SensorDataEntry.COLUMN_NAME_LONGITUDE))
                    val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(SensorDataEntry.COLUMN_NAME_TIMESTAMP))
                    sensorData = SensorData(id, deviceId, latitude, longitude, timestamp)
                }
            } finally {
                cursor.close()
            }
        }
        db.close()
        return sensorData
    }


    // --- Métodos para AuthCredentials ---

    /**
     * Inserta o actualiza el token de autenticación.
     * Dado que solo debe haber un token, siempre intentamos reemplazarlo.
     * @param token El token de autenticación a guardar.
     * @return El ID de la fila insertada/actualizada, o -1 si ocurrió un error.
     */
    fun saveAuthToken(token: String): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(AuthCredentialsEntry.COLUMN_NAME_AUTH_TOKEN, token)
        }

        val newRowId = db.insertWithOnConflict(
            AuthCredentialsEntry.TABLE_NAME,
            null,
            values,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
        db.close()
        if (newRowId == -1L) {
            Log.e("DatabaseManager", "Error al guardar/actualizar el token de autenticación.")
        } else {
            Log.d("DatabaseManager", "Token de autenticación guardado/actualizado con ID: $newRowId")
        }
        return newRowId
    }


    fun getAuthToken(): String? {
        val db = dbHelper.readableDatabase
        var authToken: String? = null

        val projection = arrayOf(AuthCredentialsEntry.COLUMN_NAME_AUTH_TOKEN)
        val cursor: Cursor? = db.query(
            AuthCredentialsEntry.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val tokenColumnIndex = it.getColumnIndexOrThrow(AuthCredentialsEntry.COLUMN_NAME_AUTH_TOKEN)
                authToken = it.getString(tokenColumnIndex)
            }
        }
        db.close()
        return authToken
    }


    fun deleteAuthToken(): Int {
        val db = dbHelper.writableDatabase
        val deletedRows = db.delete(AuthCredentialsEntry.TABLE_NAME, null, null)
        db.close()
        if (deletedRows > 0) {
            Log.d("DatabaseManager", "Token de autenticación eliminado.")
        } else {
            Log.d("DatabaseManager", "No se encontró token de autenticación para eliminar.")
        }
        return deletedRows
    }
}