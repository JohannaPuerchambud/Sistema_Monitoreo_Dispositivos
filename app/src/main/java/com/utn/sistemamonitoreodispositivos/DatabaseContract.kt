package com.utn.sistemamonitoreodispositivos

import android.provider.BaseColumns

object DatabaseContract {


    const val DATABASE_NAME = "device_monitoring.db"
    const val DATABASE_VERSION = 1


    object SensorDataEntry : BaseColumns {
        const val TABLE_NAME = "sensor_data"
        const val COLUMN_NAME_DEVICE_ID = "device_id"
        const val COLUMN_NAME_LATITUDE = "latitude"
        const val COLUMN_NAME_LONGITUDE = "longitude"
        const val COLUMN_NAME_TIMESTAMP = "timestamp"


        const val SQL_CREATE_SENSOR_DATA_TABLE =
            "CREATE TABLE ${TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "${COLUMN_NAME_DEVICE_ID} TEXT NOT NULL," +
                    "${COLUMN_NAME_LATITUDE} REAL NOT NULL," +
                    "${COLUMN_NAME_LONGITUDE} REAL NOT NULL," +
                    "${COLUMN_NAME_TIMESTAMP} INTEGER NOT NULL)"


        const val SQL_DELETE_SENSOR_DATA_TABLE =
            "DROP TABLE IF EXISTS ${TABLE_NAME}"
    }


    object AuthCredentialsEntry : BaseColumns {
        const val TABLE_NAME = "auth_credentials"
        const val COLUMN_NAME_AUTH_TOKEN = "auth_token"


        const val SQL_CREATE_AUTH_CREDENTIALS_TABLE =
            "CREATE TABLE ${TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "${COLUMN_NAME_AUTH_TOKEN} TEXT NOT NULL UNIQUE)"


        const val SQL_DELETE_AUTH_CREDENTIALS_TABLE =
            "DROP TABLE IF EXISTS ${TABLE_NAME}"
    }
}