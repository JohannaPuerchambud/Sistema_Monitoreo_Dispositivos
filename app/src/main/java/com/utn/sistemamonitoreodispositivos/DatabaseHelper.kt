package com.utn.sistemamonitoreodispositivos

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

import com.utn.sistemamonitoreodispositivos.DatabaseContract.DATABASE_NAME
import com.utn.sistemamonitoreodispositivos.DatabaseContract.DATABASE_VERSION
import com.utn.sistemamonitoreodispositivos.DatabaseContract.SensorDataEntry
import com.utn.sistemamonitoreodispositivos.DatabaseContract.AuthCredentialsEntry

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SensorDataEntry.SQL_CREATE_SENSOR_DATA_TABLE)
        Log.d("DatabaseHelper", "Tabla '${SensorDataEntry.TABLE_NAME}' creada.")

        db.execSQL(AuthCredentialsEntry.SQL_CREATE_AUTH_CREDENTIALS_TABLE)
        Log.d("DatabaseHelper", "Tabla '${AuthCredentialsEntry.TABLE_NAME}' creada.")


        val initialToken = "JAPASPUELS"
        insertApiToken(db, initialToken)
        Log.d("DatabaseHelper", "Token de API inicial insertado en la base de datos.")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SensorDataEntry.SQL_DELETE_SENSOR_DATA_TABLE)
        Log.d("DatabaseHelper", "Tabla '${SensorDataEntry.TABLE_NAME}' eliminada para actualización.")

        db.execSQL(AuthCredentialsEntry.SQL_DELETE_AUTH_CREDENTIALS_TABLE)
        Log.d("DatabaseHelper", "Tabla '${AuthCredentialsEntry.TABLE_NAME}' eliminada para actualización.")

        onCreate(db)
        Log.d("DatabaseHelper", "Base de datos actualizada. Tablas recreadas.")
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.w("DatabaseHelper", "Degradando la base de datos de la versión $oldVersion a $newVersion. Esto eliminará todos los datos.")
        onUpgrade(db, oldVersion, newVersion)
    }


    private fun insertApiToken(db: SQLiteDatabase, token: String) {
        val values = ContentValues().apply {
            put(AuthCredentialsEntry.COLUMN_NAME_AUTH_TOKEN, token)
        }

        val newRowId = db.insertWithOnConflict(
            AuthCredentialsEntry.TABLE_NAME,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )

        if (newRowId == -1L) {
            Log.e("DatabaseHelper", "Error al insertar o actualizar el token de API.")
        } else {
            Log.d("DatabaseHelper", "Token de API insertado/actualizado correctamente (ID: $newRowId).")
        }
    }


    fun getApiToken(): String? {
        val db = this.readableDatabase
        var apiToken: String? = null
        var cursor: android.database.Cursor? = null

        try {
            cursor = db.query(
                AuthCredentialsEntry.TABLE_NAME,
                arrayOf(AuthCredentialsEntry.COLUMN_NAME_AUTH_TOKEN),
                null,
                null,
                null,
                null,
                null,
                "1"
            )

            if (cursor != null && cursor.moveToFirst()) {
                val tokenColumnIndex = cursor.getColumnIndex(AuthCredentialsEntry.COLUMN_NAME_AUTH_TOKEN)
                if (tokenColumnIndex != -1) {
                    apiToken = cursor.getString(tokenColumnIndex)
                } else {
                    Log.e("DatabaseHelper", "Columna '${AuthCredentialsEntry.COLUMN_NAME_AUTH_TOKEN}' no encontrada en el cursor.")
                }
            }
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error al obtener el token de API: ${e.message}", e)
        } finally {
            cursor?.close()
        }
        return apiToken
    }
}